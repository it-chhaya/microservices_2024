package co.istad.core.recommendation.service;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.core.recommendation.persistence.Recommendation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecommendationMapper {

	@Mappings({
			@Mapping(target = "serviceAddress", ignore = true)
	})
	RecommendationDto toRecommendationDto(Recommendation entity);

	@Mappings({
			@Mapping(target = "id", ignore = true),
			@Mapping(target = "version", ignore = true)
	})
	Recommendation fromRecommendationDto(RecommendationDto dto);

	List<RecommendationDto> toRecommendationDtoList(List<Recommendation> entity);

	List<Recommendation> fromRecommendationDtoList(List<Recommendation> dto);

}
