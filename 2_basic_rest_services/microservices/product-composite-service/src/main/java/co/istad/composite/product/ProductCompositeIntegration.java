package co.istad.composite.product;

import co.istad.api.core.product.ProductDto;
import co.istad.api.core.product.ProductService;
import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.core.review.ReviewDto;
import co.istad.api.core.review.ReviewService;
import co.istad.api.event.Event;
import co.istad.api.exception.InvalidInputException;
import co.istad.api.exception.NotFoundException;
import co.istad.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

import static org.springframework.http.HttpMethod.GET;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private final WebClient webClient;
	private final ObjectMapper mapper;
	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;

	private final Scheduler publishEventScheduler;

	public ProductCompositeIntegration(
			Scheduler publishEventScheduler,
			WebClient.Builder webClient,
			ObjectMapper mapper,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort) {

		this.webClient = webClient.build();
		this.mapper = mapper;

		productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/products";
		recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendations";
		reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/reviews";
	}

	@Override
	public Mono<ProductDto> createProduct(ProductDto body) {

		return Mono.fromCallable(() -> {
			sendMessage("products-out-0",
					Event.builder()
					.eventType(Event.Type.CREATE)
					.key(body.productId())
					.data(body)
					.build());
			return body;
		}).subscribeOn(publishEventScheduler);

	}

	@Override
	public Mono<ProductDto> findProductById(Long productId) {

		String url = productServiceUrl + "/products/" + productId;
		log.debug("Will call the findProductById API on URL: {}", url);

		return webClient.get()
				.uri(url)
				.retrieve()
				.bodyToMono(ProductDto.class)
				.log(log.getName(), Level.FINE)
				.onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
	}

	@Override
	public Mono<Void> deleteProduct(Long productId) {
		return Mono.fromRunnable(() ->
				sendMessage("products-out-0",
						Event.builder()
								.eventType(Event.Type.DELETE)
								.key(productId)
								.build()))
				.subscribeOn(publishEventScheduler)
				.then();
	}

	@Override
	public Mono<ReviewDto> createReview(ReviewDto body) {
		return Mono.fromCallable(() -> {
			sendMessage("reviews-out-0",
					Event.builder()
							.eventType(Event.Type.CREATE)
							.key(body.productId())
							.data(body)
							.build());
			return body;
		}).subscribeOn(publishEventScheduler);
	}

	@Override
	public Flux<ReviewDto> getReviews(Long productId) {

		String url = reviewServiceUrl + "/reviews?productId=" + productId;

		log.debug("Will call the getReviews API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return webClient.get()
				.uri(url)
				.retrieve()
				.bodyToFlux(ReviewDto.class)
				.log(log.getName(), Level.FINE)
				.onErrorResume(error -> Flux.empty());
	}

	@Override
	public Mono<Void> deleteReviews(Long productId) {
		return Mono.fromRunnable(() ->
				sendMessage("reviews-out-0",
						Event.builder()
								.eventType(Event.Type.DELETE)
								.key(productId)
								.build()))
				.subscribeOn(publishEventScheduler)
				.then();
	}

	@Override
	public Mono<RecommendationDto> createRecommendation(RecommendationDto body) {

		return Mono.fromCallable(() -> {
			sendMessage("recommendations-out-0", Event.builder()
					.eventType(Event.Type.CREATE)
					.key(body.productId())
					.data(body)
					.build());
			return body;
		}).subscribeOn(publishEventScheduler);

	}

	@Override
	public Flux<RecommendationDto> getRecommendations(Long productId) {

		String url = recommendationServiceUrl + "/recommendations?productId=" + productId;

		log.debug("Will call the getRecommendations API on URL: {}", url);

		// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
		return webClient.get()
				.uri(url)
				.retrieve()
				.bodyToFlux(RecommendationDto.class)
				.log(log.getName(), Level.FINE)
				.onErrorResume(error -> Flux.empty());
	}

	@Override
	public Mono<Void> deleteRecommendations(Long productId) {
		return Mono.fromRunnable(() -> sendMessage("reviews-out-0",
				Event.builder()
						.eventType(Event.Type.DELETE)
						.key(productId)
						.build()))
				.subscribeOn(publishEventScheduler).then();
	}

	private void sendMessage(String bindingName, Event event) {
		/*log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
		Message message = MessageBuilder.withPayload(event)
				.setHeader("partitionKey", event.getKey())
				.build();
		streamBridge.send(bindingName, message);*/
	}

	private Throwable handleException(Throwable ex) {

		if (!(ex instanceof WebClientResponseException wcre)) {
			log.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
			return ex;
		}

		switch (HttpStatus.resolve(wcre.getStatusCode().value())) {
			case NOT_FOUND -> {
				return new NotFoundException(getErrorMessage(wcre));
			}
			case UNPROCESSABLE_ENTITY -> {
				return new InvalidInputException(getErrorMessage(wcre));
			}
			default -> {
				log.warn("Got an unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
				log.warn("Error body: {}", wcre.getResponseBodyAsString());
				return ex;
			}
		}
	}

	private String getErrorMessage(WebClientResponseException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}

}
