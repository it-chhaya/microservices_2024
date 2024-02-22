package co.istad.composite.product;

import co.istad.api.core.product.Product;
import co.istad.api.core.product.ProductService;
import co.istad.api.core.recommendation.Recommendation;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.core.review.Review;
import co.istad.api.core.review.ReviewService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {
	@Override
	public Product findProductById(Long productId) {
		return null;
	}

	@Override
	public List<Review> getReviews(int productId) {
		return null;
	}

	@Override
	public List<Recommendation> getRecommendations(int productId) {
		return null;
	}
}
