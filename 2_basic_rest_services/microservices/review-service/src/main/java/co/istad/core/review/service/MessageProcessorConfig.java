package co.istad.core.review.service;

import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.core.review.ReviewDto;
import co.istad.api.core.review.ReviewService;
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

	private final ReviewService reviewService;

	@Bean
	Consumer<Event<Long, ReviewDto>> messageProcessor() {
		return new Consumer<Event<Long, ReviewDto>>() {
			@Override
			public void accept(Event<Long, ReviewDto> event) {
				log.info("Process message created at {}...",  event);

				switch (event.getEventType()) {
					case CREATE -> {
						ReviewDto reviewDto = event.getData();
						log.info("Create recommendation with ID: {}", reviewDto.productId());
					}
					case DELETE -> {
						Long recommendationId = event.getKey();
						log.info("Delete recommendation with RecommendationID: {}", recommendationId);
						reviewService.deleteReviews(recommendationId).block();
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
