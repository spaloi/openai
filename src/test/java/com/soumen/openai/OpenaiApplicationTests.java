package com.soumen.openai;

import com.soumen.openai.controller.ChatController;
import org.junit.jupiter.api.*;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.evaluation.FactCheckingEvaluator;
import org.springframework.ai.chat.evaluation.RelevancyEvaluator;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.evaluation.EvaluationRequest;
import org.springframework.ai.evaluation.EvaluationResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(properties ={
		"spring.ai.openai.api-key=${OPENAI_API_KEY}",
		"logging.level.org.springframework.ai.chat.client.advisor=DEBUG"
       } )
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OpenaiApplicationTests {

	@Autowired
	ChatController chatController;

	@Autowired
    ChatModel chatModel;
	ChatClient chatClient;
	RelevancyEvaluator relevancyEvaluator;
	FactCheckingEvaluator factCheckingEvaluator;

	@BeforeEach
	public void setUp() {
		ChatClient.Builder chatClientBuilder = ChatClient.builder(chatModel)
				       .defaultAdvisors(new SimpleLoggerAdvisor());
		this.relevancyEvaluator=RelevancyEvaluator.builder()
				       .chatClientBuilder(chatClientBuilder).build();
		this.chatClient=chatClientBuilder.build();
		this.factCheckingEvaluator= FactCheckingEvaluator.builder(chatClientBuilder).build();
	}

	@Test
	@DisplayName("Should return relevant response for basic geography question")
	@Timeout(value = 30)
	void testChatController() {
		String message = "What is the capital of India?";
		String response = chatController.chat(message);
        EvaluationRequest rvaluationRequest = new EvaluationRequest(message,response);
		EvaluationResponse evaluationResponse = relevancyEvaluator.evaluate(rvaluationRequest);
		Assertions.assertAll(() -> Assertions.assertTrue(evaluationResponse.isPass()),
				()->Assertions.assertTrue(evaluationResponse.getScore() > 0.5,
						"Relevancy score should be greater than 0.5"));
	}

	@Test
	void contextLoads() {
	}

}
