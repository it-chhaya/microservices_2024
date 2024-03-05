package co.istad.core.review.service;

import co.istad.api.core.review.ReviewDto;
import co.istad.core.review.persistence.Review;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

	@Mappings({
			@Mapping(target = "serviceAddress", ignore = true)
	})
	ReviewDto entityToApi(Review entity);

	@Mappings({
			@Mapping(target = "id", ignore = true),
			@Mapping(target = "version", ignore = true)
	})
	Review apiToEntity(ReviewDto api);

	List<ReviewDto> entityListToApiList(List<Review> entity);

	List<Review> apiListToEntityList(List<Review> api);

}
