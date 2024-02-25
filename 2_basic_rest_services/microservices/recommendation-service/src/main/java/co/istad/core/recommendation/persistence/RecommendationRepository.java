package co.istad.core.recommendation.persistence;

import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface RecommendationRepository extends CrudRepository<Recommendation, String> {

    List<Recommendation> findByProductId(Long productId);

}
