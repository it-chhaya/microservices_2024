package co.istad.api.composite.product;

import lombok.Builder;

@Builder
public record RecommendationSummary(
		Long recommendationId,
		String author,
		Integer rate,
		String content) {
}
