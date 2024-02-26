package co.istad.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

	/**
	 * Sample usage, see below.
	 *
	 * curl -X POST $HOST:$PORT/reviews \
	 *   -H "Content-Type: application/json" --data \
	 *   '{"productId":123,"reviewId":456,"author":"me","subject":"yada, yada, yada","content":"yada, yada, yada"}'
	 *
	 * @param body A JSON representation of the new review
	 * @return A JSON representation of the newly created review
	 */
	@PostMapping(
			value    = "/reviews",
			consumes = "application/json",
			produces = "application/json")
	ReviewDto createReview(@RequestBody ReviewDto body);

	@GetMapping(
			value = "/reviews",
			produces = "application/json")
	List<ReviewDto> getReviews(@RequestParam(value = "productId") Long productId);

	/**
	 * Sample usage: "curl -X DELETE $HOST:$PORT/reviews?productId=1".
	 *
	 * @param productId ID of the product
	 */
	@DeleteMapping(value = "/reviews")
	void deleteReviews(@RequestParam(value = "productId")  Long productId);

}
