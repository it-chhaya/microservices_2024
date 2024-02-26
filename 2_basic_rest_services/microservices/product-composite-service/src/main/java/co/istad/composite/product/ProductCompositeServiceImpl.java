package co.istad.composite.product;

import co.istad.api.composite.product.*;
import co.istad.api.core.product.ProductDto;
import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.review.ReviewDto;
import co.istad.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {

	private final ServiceUtil serviceUtil;
	private final ProductCompositeIntegration integration;

	public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
		this.serviceUtil = serviceUtil;
		this.integration = integration;
	}

	@Override
	public void createProduct(ProductAggregate body) {
		try {
			log.debug("createCompositeProduct: create new composite entity for productId: {}", body.getProductId());

			ProductDto product = new ProductDto(body.getProductId(), body.getName(), body.getWeight(), null);
			integration.createProduct(product);

			if (body.getRecommendations() != null) {
				body.getRecommendations().forEach(r -> {
					RecommendationDto recommendation = new RecommendationDto(body.getProductId(), r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent(), null);
					integration.createRecommendation(recommendation);
				});
			}

			if (body.getReviews() != null) {
				body.getReviews().forEach(r -> {
					ReviewDto review = new ReviewDto(body.getProductId(), r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent(), null);
					integration.createReview(review);
				});
			}

			log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());
		} catch (RuntimeException e) {
			log.warn("createCompositeProduct failed", e);
			throw e;
		}
	}

	@Override
	public ProductAggregate getProduct(Long productId) {

		ProductDto productDto = integration.findProductById(productId);
		List<RecommendationDto> recommendationDtos = integration.getRecommendations(productId);
		List<ReviewDto> reviewDtos = integration.getReviews(productId);

		return createProductAggregate(productDto, recommendationDtos, reviewDtos, serviceUtil.getServiceAddress());
	}

	@Override
	public void deleteProduct(Long productId) {
		log.debug("deleteCompositeProduct: Deletes a product aggregate for productId: {}", productId);

		integration.deleteProduct(productId);

		integration.deleteRecommendations(productId);

		integration.deleteReviews(productId);

		log.debug("deleteCompositeProduct: aggregate entities deleted for productId: {}", productId);
	}

	private ProductAggregate createProductAggregate(
			ProductDto productDto,
			List<RecommendationDto> recommendationDtos,
			List<ReviewDto> reviewDtos,
			String serviceAddress) {

		// 1. Setup product info
		Long productId = productDto.getProductId();
		String name = productDto.getName();
		int weight = productDto.getWeight();

		// 2. Copy summary recommendation info, if available
		List<RecommendationSummary> recommendationSummaries =
				(recommendationDtos == null) ? null : recommendationDtos.stream()
						.map(r -> new RecommendationSummary(r.getRecommendationId(), r.getAuthor(), r.getRate(), r.getContent()))
						.collect(Collectors.toList());

		// 3. Copy summary review info, if available
		List<ReviewSummary> reviewSummaries =
				(reviewDtos == null) ? null : reviewDtos.stream()
						.map(r -> new ReviewSummary(r.getReviewId(), r.getAuthor(), r.getSubject(), r.getContent()))
						.collect(Collectors.toList());

		// 4. Create info regarding the involved microservices addresses
		String productAddress = productDto.getServiceAddress();
		String reviewAddress = (reviewDtos != null && !reviewDtos.isEmpty()) ? reviewDtos.get(0).getServiceAddress() : "";
		String recommendationAddress = (recommendationDtos != null && !recommendationDtos.isEmpty()) ? recommendationDtos.get(0).getServiceAddress() : "";
		ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

		return new ProductAggregate(productId, name, weight, recommendationSummaries, reviewSummaries, serviceAddresses);
	}
}
