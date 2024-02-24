package co.istad.api.composite.product;

public class ReviewSummary {

	private final Long reviewId;
	private final String author;
	private final String subject;

	public ReviewSummary(Long reviewId, String author, String subject) {
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
	}

	public Long getReviewId() {
		return reviewId;
	}

	public String getAuthor() {
		return author;
	}

	public String getSubject() {
		return subject;
	}

}
