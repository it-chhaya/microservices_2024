package co.istad.api.composite.product;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class ProductAggregate {
	private Long productId;
	private String name;
	private Integer weight;
	private List<RecommendationSummary> recommendations;
	private List<ReviewSummary> reviews;
	private ServiceAddresses serviceAddresses;
}
