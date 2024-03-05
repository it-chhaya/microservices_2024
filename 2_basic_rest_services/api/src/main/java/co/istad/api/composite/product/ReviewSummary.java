package co.istad.api.composite.product;

import lombok.Builder;

@Builder
public record ReviewSummary(
		Long reviewId,
		String author,
		String subject,
		String content) {
}
