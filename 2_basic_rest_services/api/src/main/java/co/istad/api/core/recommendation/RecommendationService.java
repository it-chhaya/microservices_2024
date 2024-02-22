package co.istad.api.core.recommendation;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

public interface RecommendationService {

	@GetMapping(
			value = "/recommendation",
			produces = "application/json")
	List<Recommendation> getRecommendations(
			@RequestParam(value = "productId", required = true) int productId);

}
