package co.istad.core.review.service;

import co.istad.api.core.review.ReviewDto;
import co.istad.api.core.review.ReviewService;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.review.persistence.Review;
import co.istad.core.review.persistence.ReviewRepository;
import co.istad.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ReviewServiceImpl implements ReviewService {

	private final ServiceUtil serviceUtil;
	private final ReviewRepository reviewRepository;
	private final ReviewMapper reviewMapper;

	@Override
	public ReviewDto createReview(ReviewDto body) {
		try {
			Review entity = reviewMapper.apiToEntity(body);
			Review newEntity = reviewRepository.save(entity);

			log.debug("createReview: created a review entity: {}/{}", body.getProductId(), body.getReviewId());
			return reviewMapper.entityToApi(newEntity);

		} catch (DataIntegrityViolationException dive) {
			throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Review Id:" + body.getReviewId());
		}
	}

	@Override
	public List<ReviewDto> getReviews(Long productId) {
		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 213) {
			log.debug("No reviews found for productId: {}", productId);
			return new ArrayList<>();
		}

		List<ReviewDto> list = new ArrayList<>();
		list.add(new ReviewDto(productId, 1L, "Author 1", "Subject 1", "Content 1", serviceUtil.getServiceAddress()));
		list.add(new ReviewDto(productId, 2L, "Author 2", "Subject 2", "Content 2", serviceUtil.getServiceAddress()));
		list.add(new ReviewDto(productId, 3L, "Author 3", "Subject 3", "Content 3", serviceUtil.getServiceAddress()));

		log.debug("/reviews response size: {}", list.size());

		return list;
	}

	@Override
	public void deleteReviews(Long productId) {
		log.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);
		reviewRepository.deleteAll(reviewRepository.findByProductId(productId));
	}

}
