package com.soumen.openai.config;

import com.soumen.openai.custom.advisor.TokenUsageAdvisor;
import com.soumen.openai.rag.PIIMaskingDocumentPostProcessor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.TranslationQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class MemoryChatClientConfig {

    //@Bean //chatMemory bean for jdbc
    MessageWindowChatMemory chatMemory(JdbcChatMemoryRepository jdbcChatMemoryRepository){
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(jdbcChatMemoryRepository)
                .maxMessages(10)
                .build();
    }

    @Bean("memoryChatClient")
    public ChatClient memoryChatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory
                                       ,RetrievalAugmentationAdvisor retrievalAugmentationAdvisor){
       // Advisor loggingAdvisor = SimpleLoggerAdvisor.builder().build();
        Advisor memoryAdvisor =  MessageChatMemoryAdvisor.builder(chatMemory).build();
        Advisor tokerUsaAdvisor = new TokenUsageAdvisor();
        return chatClientBuilder
                .defaultAdvisors(List.of(memoryAdvisor,tokerUsaAdvisor,retrievalAugmentationAdvisor))
                .build();
    }

    @Bean
    public RetrievalAugmentationAdvisor getRetrievalAugmentationAdvisor(VectorStore vectorStore,
                                            ChatClient.Builder chatClientBuilder){
       return  RetrievalAugmentationAdvisor.builder()
               .queryTransformers(TranslationQueryTransformer.builder()
                       .chatClientBuilder(chatClientBuilder.clone())
                       .targetLanguage("english").build())
               .documentRetriever(
                VectorStoreDocumentRetriever.builder().vectorStore(vectorStore)
                        .topK(3).similarityThreshold(0.5)
                        .build())
               .documentPostProcessors(PIIMaskingDocumentPostProcessor.builder())
               .build();

    }
}
