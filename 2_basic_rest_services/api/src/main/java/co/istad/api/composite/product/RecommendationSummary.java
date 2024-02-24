package co.istad.api.composite.product;

public class RecommendationSummary {

	private final Long recommendationId;
	private final String author;
	private final Integer rate;

	public RecommendationSummary(Long recommendationId, String author, Integer rate) {
		this.recommendationId = recommendationId;
		this.author = author;
		this.rate = rate;
	}

	public Long getRecommendationId() {
		return recommendationId;
	}

	public String getAuthor() {
		return author;
	}

	public Integer getRate() {
		return rate;
	}

}
