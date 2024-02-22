package co.istad.api.composite.product;

import java.util.List;

public class ProductAggregate {

	private Long productId;
	private String name;
	private Integer weight;
	private List<RecommendationSummary> recommendations;
	private List<ReviewSummary> reviews;
	private ServiceAddresses serviceAddresses;

}
