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

@RestController
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {

	private final ServiceUtil serviceUtil;
	private final ProductRepository productRepository;
	private final ProductMapper productMapper;

	@Override
	public ProductDto createProduct(ProductDto body) {
		try {
			Product product = productMapper.fromProductDto(body);
			Product newProduct = productRepository.save(product);

			log.debug("createProduct: entity created for productId: {}", body.productId());
			return productMapper.toProductDto(newProduct);
		} catch (DuplicateKeyException e) {
			throw new InvalidInputException("Duplicate key, Product ID: " + body.productId());
		}
	}

	@Override
	public ProductDto findProductById(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 13) {
			throw new NotFoundException("No product found for productId: " + productId);
		}

		return new ProductDto(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
	}

	@Override
	public void deleteProduct(Long productId) {
		log.debug("deleteProduct: tries to delete an entity with productId: {}", productId);
		productRepository.findByProductId(productId)
				.ifPresent(productRepository::delete);
	}
}
