package co.istad.core.recommendation.persistence;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface RecommendationRepository extends ReactiveCrudRepository<Recommendation, String> {

    Flux<Recommendation> findByProductId(Long productId);

}
