package co.istad.api.core.review;

public class ReviewDto {
	private final Long productId;
	private final Long reviewId;
	private final String author;
	private final String subject;
	private final String content;
	private final String serviceAddress;

	public ReviewDto() {
		productId = 0L;
		reviewId = 0L;
		author = null;
		subject = null;
		content = null;
		serviceAddress = null;
	}

	public ReviewDto(Long productId, Long reviewId, String author, String subject, String content, String serviceAddress) {
		this.productId = productId;
		this.reviewId = reviewId;
		this.author = author;
		this.subject = subject;
		this.content = content;
		this.serviceAddress = serviceAddress;
	}

	public Long getProductId() {
		return productId;
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

	public String getServiceAddress() {
		return serviceAddress;
	}
}
