package co.istad.api.core.review;

import lombok.Builder;

@Builder
public record ReviewDto(
		Long productId,
		Long reviewId,
		String author,
		String subject,
		String content,
		String serviceAddress) {
}
