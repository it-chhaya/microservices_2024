package co.istad.core.review;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.review.ReviewDto;
import co.istad.api.event.Event;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
		"spring.cloud.stream.default-binder=rabbit",
		"logging.level.istad.co=DEBUG"
})
class ReviewServiceApplicationTests extends PostgresqlTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Long, ReviewDto>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getReviewsByProductId() {

		Long productId = 1L;

		assertEquals(0, repository.findByProductId(productId).size());

		sendCreateReviewEvent(productId, 1L);
		sendCreateReviewEvent(productId, 2L);
		sendCreateReviewEvent(productId, 3L);

		assertEquals(3, repository.findByProductId(productId).size());

		getAndVerifyReviewsByProductId(productId, HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(3)
				.jsonPath("$[2].productId").isEqualTo(productId)
				.jsonPath("$[2].reviewId").isEqualTo(3);
	}

	@Test
	void duplicateError() {

		Long productId = 1L;
		Long reviewId = 1L;

		assertEquals(0, repository.count());

		sendCreateReviewEvent(productId, reviewId);

		assertEquals(1, repository.count());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateReviewEvent(productId, reviewId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Product Id: 1, Review Id:1", thrown.getMessage());

		assertEquals(1, repository.count());
	}

	@Test
	void deleteReviews() {

		Long productId = 1L;
		Long reviewId = 1L;

		sendCreateReviewEvent(productId, reviewId);
		assertEquals(1, repository.findByProductId(productId).size());

		sendDeleteReviewEvent(productId);
		assertEquals(0, repository.findByProductId(productId).size());

		sendDeleteReviewEvent(productId);
	}

	@Test
	void getReviewsMissingParameter() {

		getAndVerifyReviewsByProductId("", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void getReviewsInvalidParameter() {

		getAndVerifyReviewsByProductId("?productId=no-integer", HttpStatus.BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getReviewsNotFound() {

		getAndVerifyReviewsByProductId("?productId=213", HttpStatus.OK)
				.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getReviewsInvalidParameterNegativeValue() {

		int productIdInvalid = -1;

		getAndVerifyReviewsByProductId("?productId=" + productIdInvalid, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(Long productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productIdQuery, HttpStatus expectedStatus) {
		return client.get()
				.uri("/reviews" + productIdQuery)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateReviewEvent(Long productId, Long reviewId) {
		ReviewDto review = new ReviewDto(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
		Event<Long, ReviewDto> event = Event.<Long, ReviewDto>builder()
				.eventType(Event.Type.CREATE)
				.key(productId)
				.data(review)
				.build();
		messageProcessor.accept(event);
	}

	private void sendDeleteReviewEvent(Long productId) {
		Event<Long, ReviewDto> event = Event.<Long, ReviewDto>builder()
				.eventType(Event.Type.CREATE)
				.key(productId)
				.build();
		messageProcessor.accept(event);
	}

}
