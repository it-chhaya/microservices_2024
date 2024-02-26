package co.istad.api.composite.product;

public class ReviewSummary {

	private final Long reviewId;
	private final String author;
	private final String subject;
	private final String content;

	public ReviewSummary(Long reviewId, String author, String subject, String content) {
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content = content;
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

	public String getContent() {
		return content;
	}
}
