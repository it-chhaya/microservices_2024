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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.io.IOException;
import java.util.logging.Level;

@Component
@Slf4j
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private final WebClient webClient;
	private final ObjectMapper mapper;
	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;

	private final Scheduler publishEventScheduler;
	private final StreamBridge streamBridge;

	public ProductCompositeIntegration(
			Scheduler publishEventScheduler,
			StreamBridge streamBridge,
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
		this.publishEventScheduler = publishEventScheduler;
		this.streamBridge = streamBridge;

		productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/products";
		recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendations";
		reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/reviews";
	}

	@Override
	public Mono<ProductDto> createProduct(ProductDto body) {

		return Mono.fromCallable(() -> {
			sendMessage("products-out-0",
					new Event<>(Event.Type.CREATE, body.getProductId(), body));
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
				sendMessage("products-out-0", new Event<>(Event.Type.CREATE, productId, null)))
				.subscribeOn(publishEventScheduler)
				.then();
	}

	@Override
	public Mono<ReviewDto> createReview(ReviewDto body) {
		return Mono.fromCallable(() -> {
			sendMessage("reviews-out-0", new Event<>(Event.Type.CREATE, body.productId(), body));
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
				sendMessage("reviews-out-0", new Event<>(Event.Type.DELETE, productId, null)))
				.subscribeOn(publishEventScheduler)
				.then();
	}

	@Override
	public Mono<RecommendationDto> createRecommendation(RecommendationDto body) {

		return Mono.fromCallable(() -> {
			sendMessage("recommendations-out-0", new Event<>(Event.Type.CREATE, body.productId(), body));
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
						new Event<>(Event.Type.DELETE, productId, null)))
				.subscribeOn(publishEventScheduler).then();
	}

	private void sendMessage(String bindingName, Event event) {
		log.debug("Sending a {} message to {}", event.getEventType(), bindingName);
		Message message = MessageBuilder.withPayload(event)
				.setHeader("partitionKey", event.getKey())
				.build();
		streamBridge.send(bindingName, message);
	}


	public Mono<Health> getProductHealth() {
		return getHealth(productServiceUrl);
	}

	public Mono<Health> getRecommendationHealth() {
		return getHealth(recommendationServiceUrl);
	}

	public Mono<Health> getReviewHealth() {
		return getHealth(reviewServiceUrl);
	}

	private Mono<Health> getHealth(String url) {
		url += "/actuator/health";
		log.debug("Will call the Health API on URL: {}", url);
		return webClient.get().uri(url).retrieve().bodyToMono(String.class)
				.map(s -> new Health.Builder().up().build())
				.onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
				.log(log.getName(), Level.FINE);
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
