package co.istad.core.recommendation.service;

import co.istad.api.core.recommendation.Recommendation;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.exception.InvalidInputException;
import co.istad.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {

	private static final Logger LOG = LoggerFactory.getLogger(RecommendationServiceImpl.class);

	private final ServiceUtil serviceUtil;

	public RecommendationServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}

	@Override
	public List<Recommendation> getRecommendations(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 113) {
			LOG.debug("No recommendations found for productId: {}", productId);
			return new ArrayList<>();
		}

		List<Recommendation> list = new ArrayList<>();
		list.add(new Recommendation(productId, 1L, "Author 1", 1, "Content 1", serviceUtil.getServiceAddress()));
		list.add(new Recommendation(productId, 2L, "Author 2", 2, "Content 2", serviceUtil.getServiceAddress()));
		list.add(new Recommendation(productId, 3L, "Author 3", 3, "Content 3", serviceUtil.getServiceAddress()));

		LOG.debug("/recommendation response size: {}", list.size());

		return list;
	}

}
