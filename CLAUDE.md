# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## What this is

A single-module Spring Boot app that demonstrates each major Spring AI capability as its own REST
endpoint: plain chat, streaming, prompt templating, structured output, persistent chat memory, tool
calling, RAG (vector store + live web search), and semantic caching. It is a hands-on learning
project, not a product — expect commented-out alternatives left in place deliberately (Redis-backed
semantic cache, local Docker Model Runner base URL, `RandomDataLoader`).

Spring Boot 4.1.0 / Spring AI 2.0.0 / Java 26. Not a git repository.

## Commands

```powershell
.\mvnw.cmd spring-boot:run                 # run the app (port 8088)
.\mvnw.cmd package                         # build
.\mvnw.cmd -DskipTests package             # build without tests
.\mvnw.cmd test                            # all tests
.\mvnw.cmd test -Dtest=OpenaiApplicationTests#contextLoads   # single test
```

Add `-o` for offline builds; all current dependencies are already in the local `~/.m2` repository.

The only test is `@SpringBootTest` `contextLoads`, so it needs the full context to start — which means
Qdrant reachable and a valid OpenAI key. It is not a hermetic unit test.

## Build environment gotchas

These two cost real debugging time; check them before assuming a code defect.

**Lombok does not run under the Maven build on JDK 26.** `.\mvnw.cmd package` currently fails with
`cannot find symbol: method builder()` / `getId()` on `HelpDeskTicket`, even though the class is
annotated `@Getter`/`@Builder`. IntelliJ compiles it fine because the IDE runs Lombok itself. Since
JDK 23, javac no longer runs annotation processors discovered on the plain classpath, so Lombok needs
to be declared explicitly via the compiler plugin's `annotationProcessorPaths` (or `-proc:full`).
Resolved Lombok is 1.18.46. **This is unresolved — the Maven build does not currently succeed.**

**Spring Boot 4 split `spring-boot-autoconfigure` into per-technology modules.** Auto-configuration
you expect "for free" from a starter may simply be absent, and the corresponding `spring.*` property
becomes silently inert rather than erroring. The H2 console was one such case: `spring.h2.console.enabled=true`
does nothing without an explicit `org.springframework.boot:spring-boot-h2console` dependency (now
present in the pom). When a property appears ignored, verify the autoconfig class is actually on the
classpath — inspect `BOOT-INF/lib` of the built jar rather than trusting the starter.

## Runtime dependencies

`spring-boot-docker-compose` is on the runtime classpath, so **starting the app starts `compose.yml`
automatically** (and `spring.docker.compose.stop.command=down` tears it down on shutdown):

- **Qdrant** — required. gRPC on 6334 (not the 6333 HTTP port). Two collections: `spring-ai` for RAG
  documents, `spring-ai-cache` for the semantic cache.
- **Prometheus** — on 9090, scraping via `prometheus-config.yml`. Actuator exposes only
  `health,metrics,prometheus`.
- **Redis** — commented out; the Redis semantic-cache path in `SemanticCacheConfig` is also commented out.

Chat memory and helpdesk tickets persist to a **file-based H2 database outside the repo**
(`C:\Users\soume\OneDrive\Documents\chatmemory`, `AUTO_SERVER=true`). Data therefore survives restarts
and is shared across runs; the H2 console is at http://localhost:8088/h2-console using the JDBC URL
and credentials from `application.properties`.

`WebSearchDocumentRetriever` calls the external Tavily search API.

## Architecture

### Named ChatClient beans are the central pattern

There is no single `ChatClient`. Each `@Configuration` in `config/` builds a distinct `ChatClient`
bean with its own advisor stack, and controllers select one by `@Qualifier`. To understand any
endpoint's behavior you must read its controller *and* the config class that builds its client —
the advisors, default system prompt, and default tools all live in the config, not the controller.

| Bean | Built in | Advisor stack / defaults |
|---|---|---|
| `chatClient` (primary) | `ChatClientConfig` | token usage + logging only |
| `memoryChatClient` | `MemoryChatClientConfig` | memory + token usage + full RAG advisor |
| `webSearchRAGChatClient` | `WebSearchRAGChatClientConfig` | logging + memory + token usage + web-search RAG |
| `timeChatClient` | `ToolChatClientConfig` | memory + token usage + logging, `defaultTools(TimeTool)` |
| `helpDesktimeChatClient` | `HelpDekToolChatClientConfig` | same, plus `defaultSystem(helpDeskSystemPromptTemplate)` and `defaultTools(HelpDeskTool)` |
| `cacheChatClint` | `SemanticCacheConfig` | logging + token usage + `SemanticCacheAdvisor` |

Adding a feature usually means adding a config class with a new named bean rather than modifying an
existing client — changing a shared client silently changes every endpoint using it.

### Endpoints

All under `/api`. Conversation-scoped endpoints take the conversation id from a `userName` request
header and pass it as the `CONVERSATION_ID` advisor param; the helpdesk additionally passes it
through `toolContext` so tools can resolve the caller.

- `/chat`, `/stream` — basic and streaming (`Flux<String>`)
- `/prompt-stuffing`, `/email` — system/user prompt templates from `resources/prompt-templates/*.st`
- `/structureData`, `/structureDataList` — `.entity(...)` structured output into `CountryCity`
- `/chat-memory` — JDBC-backed `MessageWindowChatMemory`
- `/local-time` — `TimeTool` tool calling
- `/help-desk` — agentic: `HelpDeskTool` creates and queries JPA `HelpDeskTicket` rows
- `/rag/random-chat`, `/rag/hrpolicy-chat` — RAG over Qdrant
- `/rag/webserch-chat` — RAG over Tavily web search (note the spelling of the path)
- `/open-chat` — semantic cache

### RAG pipeline

`MemoryChatClientConfig#getRetrievalAugmentationAdvisor` is the most complete pipeline and the best
reference for the RAG extension points: a `TranslationQueryTransformer` (normalizes queries to
English, using a *cloned* builder to avoid advisor recursion) → `VectorStoreDocumentRetriever`
(topK 3, similarity 0.5) → `PIIMaskingDocumentPostProcessor` (regex-redacts emails and phone numbers
from retrieved chunks and stamps `pii_masked` metadata).

Custom extension points implemented here, useful as templates:
- `rag/WebSearchDocumentRetriever` — a `DocumentRetriever` over an external HTTP API, with a builder
- `rag/PIIMaskingDocumentPostProcessor` — a `DocumentPostProcessor`
- `custom/advisor/TokenUsageAdvisor` — a `CallAdvisor` logging per-call token usage; applied to nearly
  every client. Note it only implements the call path, not `StreamAdvisor`.

### Vector store ingestion

`HrPolicyDataLoader` ingests `resources/Eazybytes_HR_Policies.pdf` via Tika + `TokenTextSplitter`
(chunk 100) in `@PostConstruct` — meaning **on every startup, with no idempotency check**, so Qdrant
accumulates duplicate chunks across runs. Clear the `spring-ai` collection if retrieval quality
degrades. `RandomDataLoader` (55 trivia sentences) is disabled by a commented-out `@Component`.

## Known defects in the current code

Do not "fix" these incidentally without being asked, but be aware they exist:

- API keys now come from the environment: `application.properties` uses `${OPENAI_API_KEY}` and
  `WebSearchDocumentRetriever.java:22` uses `System.getenv("TAVILY_API_KEY")`. Keep it that way — a
  hardcoded Tavily key was previously committed to the public GitHub remote and is still in history.
- `WebSearchDocumentRetriever.java:31` has its `Assert.hasText` guard commented out, so an unset
  `TAVILY_API_KEY` yields a `Bearer null` header and fails at request time instead of startup. The
  assert's message also interpolates the key's *value*, not the variable name.
- `application.properties:13-14` hold plaintext H2 credentials, though for a local file DB.
- `ChatController.java:21` — `@RequestParam(" ")`, a single space as the parameter name; almost
  certainly meant to be `"message"`, so `/api/chat` cannot be called normally.
- `ChatClientConfig` calls `.defaultAdvisors(...)` twice in a chain; the second call may replace
  rather than append.
- `pom.xml` has empty `<name>`, `<description>`, `<license>`, `<developers>`, `<scm>` elements — these
  are intentional Initializr overrides to block parent-POM inheritance (see `HELP.md`), not oversights.
