package co.istad.core.recommendation.service;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.event.Event;
import co.istad.api.exception.EventProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class MessageProcessorConfig {

	private final RecommendationService recommendationService;

	@Bean
	Consumer<Event<Long, RecommendationDto>> messageProcessor() {
		return new Consumer<Event<Long, RecommendationDto>>() {
			@Override
			public void accept(Event<Long, RecommendationDto> event) {
				log.info("Process message created at {}...",  event);

				switch (event.getEventType()) {
					case CREATE -> {
						RecommendationDto recommendationDto = event.getData();
						log.info("Create recommendation with ID: {}", recommendationDto.productId());
					}
					case DELETE -> {
						Long recommendationId = event.getKey();
						log.info("Delete recommendation with RecommendationID: {}", recommendationId);
						recommendationService.deleteRecommendations(recommendationId).block();
					}
					default -> {
						String errorMessage = "Incorrect event type: " + event.getEventType() + ", expected a CREATE or DELETE event";
						log.warn(errorMessage);
						throw new EventProcessingException(errorMessage);
					}
				}

				log.info("Message processing done!");
			}
		};
	}

}
