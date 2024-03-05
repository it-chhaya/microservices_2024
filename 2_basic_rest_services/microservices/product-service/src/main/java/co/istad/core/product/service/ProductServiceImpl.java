package co.istad.core.product.service;

import co.istad.api.core.product.ProductDto;
import co.istad.api.core.product.ProductService;
import co.istad.api.exception.InvalidInputException;
import co.istad.api.exception.NotFoundException;
import co.istad.core.product.persistence.Product;
import co.istad.core.product.persistence.ProductRepository;
import co.istad.util.http.ServiceUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.logging.Level;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

	private final ServiceUtil serviceUtil;
	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	@Override
	public Mono<ProductDto> createProduct(ProductDto body) {

		if (body.productId() < 1) {
			throw new InvalidInputException("Invalid productID: " + body.productId());
		}

		Product product = productMapper.fromProductDto(body);

		return productRepository.save(product)
				.log(log.getName(), Level.FINE)
				.onErrorMap(
						DuplicateKeyException.class,
						ex -> new InvalidInputException("Duplicate key, Product ID: " + body.productId())
				)
				.map(productMapper::toProductDto);
	}

	@Override
	public Mono<ProductDto> findProductById(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		log.info("Will get product info for ID={}", productId);

		return productRepository.findByProductId(productId)
				.switchIfEmpty(Mono.error(new NotFoundException("No product found for productId: " + productId)))
				.log(log.getName(), Level.FINE)
				.map(productMapper::toProductDto);
	}

	@Override
	public Mono<Void> deleteProduct(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
		return productRepository.findByProductId(productId)
				.log(log.getName(), Level.FINE)
				.map(productRepository::delete)
				.flatMap(voidMono -> voidMono);
	}

}
