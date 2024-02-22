package co.istad.api.core.product;

public class Product {

	private Long productId;
	private String name;
	private Integer weight;
	private String serviceAddress;

	public Product() {
		productId = 0L;
		name = null;
		weight = 0;
		serviceAddress = null;
	}

	public Product(Long productId, String name, Integer weight, String serviceAddress) {
		this.productId = productId;
		this.name = name;
		this.weight = weight;
		this.serviceAddress = serviceAddress;
	}

	public Long getProductId() {
		return productId;
	}

	public void setProductId(Long productId) {
		this.productId = productId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getWeight() {
		return weight;
	}

	public void setWeight(Integer weight) {
		this.weight = weight;
	}

	public String getServiceAddress() {
		return serviceAddress;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}
