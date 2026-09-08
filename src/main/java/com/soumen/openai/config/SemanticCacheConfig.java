package com.soumen.openai.config;

import com.soumen.openai.custom.advisor.TokenUsageAdvisor;
import io.qdrant.client.QdrantClient;
import org.springframework.ai.chat.cache.semantic.SemanticCache;
import org.springframework.ai.chat.cache.semantic.SemanticCacheAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.qdrant.QdrantVectorStore;
import org.springframework.ai.vectorstore.redis.cache.semantic.DefaultSemanticCache;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.RedisClient;

import java.util.List;

@Configuration
public class SemanticCacheConfig {

    /*@Bean
    RedisClient getRedisChache(@Value("${spring.data.redis.host:localhost}") String host,
                              @Value("${spring.data.redis.port:6379}") Integer port){

        return RedisClient.builder().hostAndPort(host,port).build();
    }*/

    @Bean("cacheVectorStore")
    VectorStore cacheVectorStore(QdrantClient qdrantClient,EmbeddingModel embeddingModel){
            return
                    QdrantVectorStore.builder(qdrantClient,embeddingModel)
                            .collectionName("spring-ai-cache")
                            .initializeSchema(true)
                            .build();
    }
    @Bean
    public SemanticCache semanticCache(@Qualifier("cacheVectorStore") VectorStore vectorStore,
                                       EmbeddingModel embeddingModel){
        return DefaultSemanticCache.builder()
                .vectorStore(vectorStore)
                .embeddingModel(embeddingModel)
                .similarityThreshold(0.8)
                .build();

    }
    /*@Bean
    public SemanticCache semanticCache(RedisClient redisClient, EmbeddingModel embeddingModel){
        return DefaultSemanticCache.builder()
                 .jedisClient(redisClient)
                .similarityThreshold(0.9)
                .embeddingModel(embeddingModel)
                .indexName("spring-ai-cache")
                .prefix("cache:")
                .build();

    }*/

    @Bean
    public SemanticCacheAdvisor semanticCacheAdvisor(SemanticCache cache){
        return SemanticCacheAdvisor.builder().cache(cache).build();
    }

    @Bean("cacheChatClint")
    public ChatClient semanticChatClient(ChatClient.Builder chatCleintBuilder,
                                         SemanticCacheAdvisor semanticCacheAdvisor, ChatClient chatClient){
        return chatCleintBuilder
                .defaultAdvisors(List.of(new SimpleLoggerAdvisor(),
                        new TokenUsageAdvisor(),semanticCacheAdvisor)).build();

    }



}
