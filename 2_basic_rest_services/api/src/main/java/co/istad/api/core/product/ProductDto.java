package co.istad.api.core.product;

import lombok.Builder;

@Builder
public record ProductDto(
		Long productId,
		String name,
		Integer weight,
		String serviceAddress) {
}
