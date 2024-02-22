package co.istad.api.core.review;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface ReviewService {

	@GetMapping(
			value = "/review",
			produces = "application/json")
	List<Review> getReviews(@RequestParam(value = "productId", required = true) int productId);

}
