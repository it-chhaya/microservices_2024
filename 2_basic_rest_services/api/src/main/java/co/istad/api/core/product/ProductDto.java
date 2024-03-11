package co.istad.api.core.product;

import lombok.Builder;

@Builder
public class ProductDto {
	private final Long productId;
	private final String name;
	private final Integer weight;
	private final String serviceAddress;

	public ProductDto() {
		productId = 0L;
		name = null;
		weight = 0;
		serviceAddress = null;
	}

	public ProductDto(Long productId, String name, Integer weight, String serviceAddress) {
		this.productId = productId;
		this.name = name;
		this.weight = weight;
		this.serviceAddress = serviceAddress;
	}

	public Long getProductId() {
		return productId;
	}

	public String getName() {
		return name;
	}

	public int getWeight() {
		return weight;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}
}
