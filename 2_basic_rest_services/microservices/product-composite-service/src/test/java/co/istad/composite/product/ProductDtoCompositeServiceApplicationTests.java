package co.istad.composite.product;

import co.istad.api.core.product.ProductDto;
import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.review.ReviewDto;
import co.istad.api.exception.InvalidInputException;
import co.istad.api.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.Mockito.when;
import static java.util.Collections.singletonList;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProductDtoCompositeServiceApplicationTests {

	private static final Long PRODUCT_ID_OK = 1L;
	private static final Long PRODUCT_ID_NOT_FOUND = 2L;
	private static final Long PRODUCT_ID_INVALID = 3L;

	@Autowired
	private WebTestClient client;

	@MockBean
	private ProductCompositeIntegration compositeIntegration;

	@BeforeEach
	void setUp() {

		when(compositeIntegration.findProductById(PRODUCT_ID_OK))
				.thenReturn(Mono.just(ProductDto.builder()
						.productId(PRODUCT_ID_OK)
						.name("name")
						.weight(1)
						.serviceAddress("mock-address")
						.build()));

		when(compositeIntegration.getRecommendations(PRODUCT_ID_OK))
				.thenReturn(Flux.fromIterable(singletonList(
						RecommendationDto.builder()
								.productId(PRODUCT_ID_OK)
								.recommendationId(1L)
								.author("author")
								.rate(1)
								.content("content")
								.serviceAddress("mock address")
								.build()
				)));

		when(compositeIntegration.getReviews(PRODUCT_ID_OK))
				.thenReturn(Flux.fromIterable(singletonList(
						ReviewDto.builder()
								.productId(PRODUCT_ID_OK)
								.reviewId(1L)
								.author("author")
								.subject("subject")
								.content("content")
								.serviceAddress("mock address")
								.build()
				)));

		when(compositeIntegration.findProductById(PRODUCT_ID_NOT_FOUND))
				.thenThrow(new NotFoundException("NOT FOUND: " + PRODUCT_ID_NOT_FOUND));

		when(compositeIntegration.findProductById(PRODUCT_ID_INVALID))
				.thenThrow(new InvalidInputException("INVALID: " + PRODUCT_ID_INVALID));

	}

	@Test
	void getProductById() {
		getAndVerifyProduct(PRODUCT_ID_OK, HttpStatus.OK)
				.jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK)
				.jsonPath("$.recommendations.length()").isEqualTo(1)
				.jsonPath("$.reviews.length()").isEqualTo(1);
	}

	@Test
	void getProductNotFound() {
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, HttpStatus.NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_NOT_FOUND)
				.jsonPath("$.message").isEqualTo("NOT FOUND: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidInput() {
		getAndVerifyProduct(PRODUCT_ID_INVALID, HttpStatus.UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/product-composite/" + PRODUCT_ID_INVALID)
				.jsonPath("$.message").isEqualTo("INVALID: " + PRODUCT_ID_INVALID);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
		return client.get()
				.uri("/product-composite/" + productId)
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(MediaType.APPLICATION_JSON)
				.expectBody();
	}

}
