package co.istad.core.product.persistence;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface ProductRepository extends PagingAndSortingRepository<Product, String>,
        CrudRepository<Product, String> {

    Optional<Product> findByProductId(Long productId);

}
