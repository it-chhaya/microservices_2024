package co.istad.core.product;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import co.istad.api.core.product.ProductDto;
import co.istad.api.event.Event;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase {

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductRepository repository;

	@Autowired
	@Qualifier("messageProcessor")
	private Consumer<Event<Long, ProductDto>> messageProcessor;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();
	}

	@Test
	void getProductById() {

		Long productId = 1L;

		assertNull(repository.findByProductId(productId).block());
		assertEquals(0, repository.count().block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());
		assertEquals(1, (long) repository.count().block());

		getAndVerifyProduct(productId, OK)
				.jsonPath("$.productId").isEqualTo(productId);
	}

	@Test
	void duplicateError() {

		Long productId = 1L;

		assertNull(repository.findByProductId(productId).block());

		sendCreateProductEvent(productId);

		assertNotNull(repository.findByProductId(productId).block());

		InvalidInputException thrown = assertThrows(
				InvalidInputException.class,
				() -> sendCreateProductEvent(productId),
				"Expected a InvalidInputException here!");
		assertEquals("Duplicate key, Product Id: " + productId, thrown.getMessage());
	}

	@Test
	void deleteProduct() {

		Long productId = 1L;

		sendDeleteProductEvent(productId);
		assertNotNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
		assertNull(repository.findByProductId(productId).block());

		sendDeleteProductEvent(productId);
	}

	@Test
	void getProductInvalidParameterString() {
		getAndVerifyProduct("/no-integer", BAD_REQUEST)
				.jsonPath("$.path").isEqualTo("/products/no-integer")
				.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getProductNotFound() {
		Long productIdNotFound = 13L;
		getAndVerifyProduct(productIdNotFound, NOT_FOUND)
				.jsonPath("$.path").isEqualTo("/products/" + productIdNotFound)
				.jsonPath("$.message").isEqualTo("No product found for productId: " + productIdNotFound);
	}

	@Test
	void getProductInvalidParameterNegativeValue() {

		Long productIdInvalid = -1L;

		getAndVerifyProduct(productIdInvalid, UNPROCESSABLE_ENTITY)
				.jsonPath("$.path").isEqualTo("/products/" + productIdInvalid)
				.jsonPath("$.message").isEqualTo("Invalid productId: " + productIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(Long productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
				.uri("/products" + productIdPath)
				.accept(APPLICATION_JSON)
				.exchange()
				.expectStatus().isEqualTo(expectedStatus)
				.expectHeader().contentType(APPLICATION_JSON)
				.expectBody();
	}

	private void sendCreateProductEvent(Long productId) {
		ProductDto product = ProductDto.builder()
				.productId(productId)
				.name("Name " + productId)
				.weight(Math.toIntExact(productId))
				.serviceAddress("SA")
				.build();
		Event<Long, ProductDto> event = Event.<Long, ProductDto>builder()
				.eventType(Event.Type.CREATE)
				.key(productId)
				.data(product)
				.eventCreatedAt(ZonedDateTime.now())
				.build();
		messageProcessor.accept(event);
	}

	private void sendDeleteProductEvent(Long productId) {
		Event<Long, ProductDto> event = Event.<Long, ProductDto>builder()
				.eventType(Event.Type.DELETE)
				.key(productId)
				.eventCreatedAt(ZonedDateTime.now())
				.build();
		messageProcessor.accept(event);
	}
}