package co.istad.core.recommendation.service;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.recommendation.persistence.Recommendation;
import co.istad.core.recommendation.persistence.RecommendationRepository;
import co.istad.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

	private final ServiceUtil serviceUtil;
	private final RecommendationRepository recommendationRepository;
	private final RecommendationMapper recommendationMapper;

	@Override
	public Mono<RecommendationDto> createRecommendation(RecommendationDto body) {

		if (body.productId() < 1) {
			throw new InvalidInputException("Invalid productId: " + body.getProductId());
		}

		Recommendation recommendation = recommendationMapper.fromRecommendationDto(body);

		return recommendationRepository.save(recommendation)
				.log(log.getName(), Level.FINE)
				.onErrorMap(DuplicateKeyException.class,
						ex -> new InvalidInputException("Duplicate key, Product Id: " + body.productId() + ", Recommendation Id:" + body.recommendationId())
				)
				.map(recommendationMapper::toRecommendationDto);
	}

	@Override
	public Flux<RecommendationDto> getRecommendations(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		log.info("Will get recommendations for product with id={}", productId);

		return recommendationRepository.findByProductId(productId)
				.log(log.getName(), Level.FINE)
				.map(recommendationMapper::toRecommendationDto);
	}

	@Override
	public Mono<Void> deleteRecommendations(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
		return recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId));
	}

}
