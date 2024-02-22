package co.istad.core.product;

import co.istad.api.core.product.Product;
import co.istad.api.core.product.ProductService;
import co.istad.api.exception.InvalidInputException;
import co.istad.api.exception.NotFoundException;
import co.istad.util.http.ServiceUtil;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {

	private final ServiceUtil serviceUtil;

	public ProductServiceImpl(ServiceUtil serviceUtil) {
		this.serviceUtil = serviceUtil;
	}

	@Override
	public Product findProductById(Long productId) {

		if (productId < 1) {
			throw new InvalidInputException("Invalid productId: " + productId);
		}

		if (productId == 13) {
			throw new NotFoundException("No product found for productId: " + productId);
		}

		return new Product(productId, "name-" + productId, 123, serviceUtil.getServiceAddress());
	}
}
