package co.istad.api.core.product;

public record ProductDto(
		Long productId,
		String name,
		Integer weight,
		String serviceAddress) {
}
