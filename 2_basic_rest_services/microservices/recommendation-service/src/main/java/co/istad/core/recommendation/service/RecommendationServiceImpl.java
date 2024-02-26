package co.istad.core.recommendation.service;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.exception.InvalidInputException;
import co.istad.core.recommendation.persistence.Recommendation;
import co.istad.core.recommendation.persistence.RecommendationRepository;
import co.istad.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

	private final ServiceUtil serviceUtil;
	private final RecommendationRepository recommendationRepository;
	private final RecommendationMapper recommendationMapper;

	@Override
	public RecommendationDto createRecommendation(RecommendationDto body) {
		try {
			Recommendation recommendation = recommendationMapper.fromRecommendationDto(body);
			Recommendation newRecommendation = recommendationRepository.save(recommendation);

			log.debug("createRecommendation: created a recommendation entity: {}/{}", body.getProductId(), body.getRecommendationId());
			return recommendationMapper.toRecommendationDto(newRecommendation);
		} catch (DuplicateKeyException e) {
			throw new InvalidInputException("Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
		}
	}

	@Override
	public List<RecommendationDto> getRecommendations(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 113) {
			log.debug("No recommendations found for productId: {}", productId);
			return new ArrayList<>();
		}

		List<RecommendationDto> list = new ArrayList<>();
		list.add(new RecommendationDto(productId, 1L, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
		list.add(new RecommendationDto(productId, 2L, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
		list.add(new RecommendationDto(productId, 3L, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

		log.debug("/recommendation response size: {}", list.size());

		return list;
	}

	@Override
	public void deleteRecommendations(Long productId) {
		log.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);
		recommendationRepository.deleteAll(recommendationRepository.findByProductId(productId));
	}

}
