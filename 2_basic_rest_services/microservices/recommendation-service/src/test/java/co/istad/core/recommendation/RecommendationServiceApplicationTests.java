package co.istad.core.recommendation;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.function.Consumer;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.event.Event;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class RecommendationServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Long, RecommendationDto>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void getRecommendationsByProductId() {

		Long productId = 1L;

		sendCreateRecommendationEvent(productId, 1L);
		sendCreateRecommendationEvent(productId, 2L);
		sendCreateRecommendationEvent(productId, 3L);

		assertEquals(3, (long) repository.findByProductId(productId).count().block());

		getAndVerifyRecommendationsByProductId(productId, OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	@Test
	void duplicateError() {

		Long productId = 1L;
		Long recommendationId = 1L;

		sendCreateRecommendationEvent(productId, recommendationId);

		assertEquals(1, (long) repository.count().block());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateRecommendationEvent(productId, recommendationId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Product Id: 1, Recommendation Id:1", thrown.getMessage());

		assertEquals(1, (long) repository.count().block());
	}

	@Test
	void deleteRecommendations() {

		Long productId = 1L;
		Long recommendationId = 1L;

		sendCreateRecommendationEvent(productId, recommendationId);
		assertEquals(1, (long) repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
		assertEquals(0, (long) repository.findByProductId(productId).count().block());

		sendDeleteRecommendationEvent(productId);
	}

	@Test
	void getRecommendationsMissingParameter() {

		getAndVerifyRecommendationsByProductId("", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void getRecommendationsInvalidParameter() {

		getAndVerifyRecommendationsByProductId("?productId=no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsNotFound() {

		getAndVerifyRecommendationsByProductId("?productId=113", OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyRecommendationsByProductId("?productId=" + productIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/recommendation")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(Long productId, HttpStatus expectedStatus) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/recommendation" + productIdQuery)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateRecommendationEvent(Long productId, Long recommendationId) {
		RecommendationDto recommendation = new RecommendationDto(productId, recommendationId, "Author " + recommendationId, 1, "Content " + recommendationId, "SA");
		Event<Long, RecommendationDto> event = Event.<Long, RecommendationDto>builder()
				.eventType(Event.Type.CREATE)
				.key(productId)
				.data(recommendation)
				.build();
		messageProcessor.accept(event);
	}

	private void sendDeleteRecommendationEvent(Long productId) {
		Event<Long, RecommendationDto> event = Event.<Long, RecommendationDto>builder()
				.eventType(Event.Type.DELETE)
				.key(productId)
				.build();
		messageProcessor.accept(event);
	}
}
