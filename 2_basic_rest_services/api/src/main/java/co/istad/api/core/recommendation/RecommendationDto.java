package co.istad.api.core.recommendation;

public record RecommendationDto(
		Long productId,
		Long recommendationId,
		String author,
		Integer rate,
		String content,
		String serviceAddress) {


}
