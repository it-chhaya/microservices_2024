package co.istad.core.review.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ReviewRepository extends CrudRepository<Review, Long> {

    @Transactional(readOnly = true)
    List<Review> findByProductId(Long productId);

}
