package co.istad.core.review.service;

import co.istad.api.core.review.ReviewDto;
import co.istad.api.core.review.ReviewService;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.review.persistence.Review;
import co.istad.core.review.persistence.ReviewRepository;
import co.istad.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

	private final Scheduler jdbcScheduler;
	private final ServiceUtil serviceUtil;
	private final ReviewRepository reviewRepository;
	private final ReviewMapper reviewMapper;

	@Override
	public Mono<ReviewDto> createReview(ReviewDto body) {

		if (body.reviewId() < 1) {
			throw new InvalidInputException("Invalid productId: " + body.productId());
		}

		return Mono.fromCallable(() -> internalCreateReview(body));
	}

	private ReviewDto internalCreateReview(ReviewDto body) {
		try {
			Review review = reviewMapper.apiToEntity(body);
			Review newReview = reviewRepository.save(review);

			log.debug("createReview: created a review entity: {}/{}", body.productId(), body.reviewId());
			return reviewMapper.entityToApi(newReview);
		} catch (DataIntegrityViolationException exception) {
			throw new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Review Id:" + body.reviewId());
		}
	}

	@Override
	public Flux<ReviewDto> getReviews(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		log.info("Will get reviews for product with id={}", productId);

		return Mono.fromCallable(() -> internalGetReview(productId))
				.flatMapMany(Flux::fromIterable)
				.log(log.getName(), Level.FINE)
				.subscribeOn(jdbcScheduler);
	}

	private List<ReviewDto> internalGetReview(Long productId) {
		List<Review> reviews = reviewRepository.findByProductId(productId);
		List<ReviewDto> reviewDtoList = reviewMapper.entityListToApiList(reviews);
		log.debug("Response size: {}", reviewDtoList.size());
		return reviewDtoList;
	}

	@Override
	public Mono<Void> deleteReviews(Long productId) {
		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		return Mono.fromRunnable(() -> internalDeleteReviews(productId))
				.subscribeOn(jdbcScheduler).then();
	}

	private void internalDeleteReviews(Long productId) {

		log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);

		reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
	}

}
