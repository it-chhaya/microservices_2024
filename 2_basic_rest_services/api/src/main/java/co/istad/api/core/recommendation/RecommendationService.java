package co.istad.api.core.recommendation;

import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface RecommendationService {

	/**
	 * Sample usage, see below.
	 *
	 * curl -X POST $HOST:$PORT/recommendations \
	 *   -H "Content-Type: application/json" --data \
	 *   '{"productId":123,"recommendationId":456,"author":"me","rate":5,"content":"yada, yada, yada"}'
	 *
	 * @param body A JSON representation of the new recommendation
	 * @return A JSON representation of the newly created recommendation
	 */
	@PostMapping(
			value    = "/recommendations",
			consumes = "application/json",
			produces = "application/json")
	Mono<RecommendationDto> createRecommendation(@RequestBody RecommendationDto body);

	@GetMapping(
			value = "/recommendations",
			produces = "application/json")
	Flux<RecommendationDto> getRecommendations(
			@RequestParam(value = "productId") Long productId);

	/**
	 * Sample usage: "curl -X DELETE $HOST:$PORT/recommendations?productId=1".
	 *
	 * @param productId ID of the product
	 */
	@DeleteMapping(value = "/recommendations")
	Mono<Void> deleteRecommendations(@RequestParam(value = "productId", required = true)  Long productId);

}
