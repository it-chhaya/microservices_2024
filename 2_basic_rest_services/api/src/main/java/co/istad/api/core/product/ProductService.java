package co.istad.api.core.product;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

public interface ProductService {

	/**
	 * Sample usage, see below.
	 *
	 * curl -X POST $HOST:$PORT/products \
	 *   -H "Content-Type: application/json" --data \
	 *   '{"productId":123,"name":"product 123","weight":123}'
	 *
	 * @param body A JSON representation of the new product
	 * @return A JSON representation of the newly created product
	 */
	@PostMapping(
			value    = "/products",
			consumes = "application/json",
			produces = "application/json")
	Mono<ProductDto> createProduct(@RequestBody ProductDto body);

	/**
	 * Sample usage: "curl $HOST:$PORT/products/1".
	 *
	 * @param productId ID of the product
	 * @return the product, if found, else null
	 */
	@GetMapping(value = "/products/{productId}", produces = MediaType.APPLICATION_JSON_VALUE)
	Mono<ProductDto> findProductById(@PathVariable Long productId);

	/**
	 * Sample usage: "curl -X DELETE $HOST:$PORT/products/1".
	 *
	 * @param productId ID of the product
	 */
	@DeleteMapping(value = "/products/{productId}")
	Mono<Void> deleteProduct(@PathVariable Long productId);

}
