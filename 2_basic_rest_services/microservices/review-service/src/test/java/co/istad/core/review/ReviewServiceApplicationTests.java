package co.istad.core.review;

import co.istad.api.core.review.ReviewDto;
import co.istad.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ReviewRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getReviewsByProductId() {

		Long productId = 1L;

		assertEquals(0, repository.findByProductId(productId).size());

		postAndVerifyReview(productId, 1L, HttpStatus.OK);
		postAndVerifyReview(productId, 2L, HttpStatus.OK);
		postAndVerifyReview(productId, 3L, HttpStatus.OK);

		assertEquals(3L, repository.findByProductId(productId).size());

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

		postAndVerifyReview(productId, reviewId, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(productId)
				.jsonPath("$.reviewId").isEqualTo(reviewId);

		assertEquals(1, repository.count());

		postAndVerifyReview(productId, reviewId, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/reviews")
				.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id: 1");

		assertEquals(1, repository.count());

	}

	@Test
	void deleteReviews() {

		Long productId = 1L;
		Long reviewId = 1L;

		postAndVerifyReview(productId, reviewId, HttpStatus.OK);
		assertEquals(1, repository.findByProductId(productId).size());

		deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK);
		assertEquals(0, repository.findByProductId(productId).size());

		deleteAndVerifyReviewsByProductId(productId, HttpStatus.OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyReview(Long productId, Long reviewId, HttpStatus expectedStatus) {
		ReviewDto review = new ReviewDto(productId, reviewId, "Author " + reviewId, "Subject " + reviewId, "Content " + reviewId, "SA");
		return client.post()
				.uri("/reviews")
				.body(Mono.just(review), ReviewDto.class)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(Long productId, HttpStatus expectedStatus) {
		return client.delete()
				.uri("/reviews?productId=" + productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectBody();
	}

}
