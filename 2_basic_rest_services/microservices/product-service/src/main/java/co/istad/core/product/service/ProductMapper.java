package co.istad.core.product.service;

import co.istad.api.core.product.ProductDto;
import co.istad.core.product.persistence.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ProductMapper {

	@Mappings({
			@Mapping(target = "serviceAddress", ignore = true)
	})
	ProductDto toProductDto(Product product);

	@Mappings({
			@Mapping(target = "id", ignore = true),
			@Mapping(target = "version", ignore = true)
	})
	Product fromProductDto(ProductDto productDto);

}
