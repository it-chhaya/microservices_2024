package co.istad.api.composite.product;

public class RecommendationSummary {

	private final Long recommendationId;
	private final String author;
	private final Integer rate;
	private final String content;

	public RecommendationSummary(Long recommendationId, String author, Integer rate, String content) {
		this.recommendationId = recommendationId;
		this.author = author;
		this.rate = rate;
		this.content = content;
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

	public String getContent() {
		return content;
	}
}
