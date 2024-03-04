package co.istad.composite.product;

import co.istad.api.core.product.ProductDto;
import co.istad.api.core.product.ProductService;
import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.recommendation.RecommendationService;
import co.istad.api.core.review.ReviewDto;
import co.istad.api.core.review.ReviewService;
import co.istad.api.exception.InvalidInputException;
import co.istad.api.exception.NotFoundException;
import co.istad.util.http.HttpErrorInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.springframework.http.HttpMethod.GET;

@Component
public class ProductCompositeIntegration implements ProductService, RecommendationService, ReviewService {

	private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeIntegration.class);

	private final RestTemplate restTemplate;
	private final ObjectMapper mapper;

	private final String productServiceUrl;
	private final String recommendationServiceUrl;
	private final String reviewServiceUrl;

	public ProductCompositeIntegration(
			RestTemplate restTemplate,
			ObjectMapper mapper,
			@Value("${app.product-service.host}") String productServiceHost,
			@Value("${app.product-service.port}") int productServicePort,
			@Value("${app.recommendation-service.host}") String recommendationServiceHost,
			@Value("${app.recommendation-service.port}") int recommendationServicePort,
			@Value("${app.review-service.host}") String reviewServiceHost,
			@Value("${app.review-service.port}") int reviewServicePort) {

		this.restTemplate = restTemplate;
		this.mapper = mapper;

		productServiceUrl = "http://" + productServiceHost + ":" + productServicePort + "/products/";
		recommendationServiceUrl = "http://" + recommendationServiceHost + ":" + recommendationServicePort + "/recommendations?productId=";
		reviewServiceUrl = "http://" + reviewServiceHost + ":" + reviewServicePort + "/reviews?productId=";
	}

	@Override
	public ProductDto createProduct(ProductDto body) {
		try {
			String url = productServiceUrl;
			LOG.debug("Will post a new product to URL: {}", url);

			ProductDto product = restTemplate.postForObject(url, body, ProductDto.class);
			LOG.debug("Created a product with id: {}", product.getProductId());

			return product;

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public ProductDto findProductById(Long productId) {

		try {
			String url = productServiceUrl + productId;
			LOG.debug("Will call getProduct API on URL: {}", url);

			ProductDto productDto = restTemplate.getForObject(url, ProductDto.class);
			LOG.debug("Found a product with id: {}", productDto.getProductId());

			return productDto;
		} catch (HttpClientErrorException ex) {
			switch (HttpStatus.resolve(ex.getStatusCode().value())) {
				case NOT_FOUND -> throw new NotFoundException(getErrorMessage(ex));
				case UNPROCESSABLE_ENTITY -> throw new InvalidInputException(getErrorMessage(ex));
				default -> {
					LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
					LOG.warn("Error body: {}", ex.getResponseBodyAsString());
					throw ex;
				}
			}
		}

	}

	@Override
	public void deleteProduct(Long productId) {
		try {
			String url = productServiceUrl + "/" + productId;
			LOG.debug("Will call the deleteProduct API on URL: {}", url);

			restTemplate.delete(url);

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public ReviewDto createReview(ReviewDto body) {
		try {
			String url = reviewServiceUrl;
			LOG.debug("Will post a new review to URL: {}", url);

			ReviewDto review = restTemplate.postForObject(url, body, ReviewDto.class);
			LOG.debug("Created a review with id: {}", review.getProductId());

			return review;

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public List<ReviewDto> getReviews(Long productId) {

		try {
			String url = reviewServiceUrl + productId;

			LOG.debug("Will call getReviews API on URL: {}", url);
			List<ReviewDto> reviewDtos = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<ReviewDto>>() {
					})
					.getBody();

			LOG.debug("Found {} reviews for a product with id: {}", reviewDtos.size(), productId);
			return reviewDtos;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
			return new ArrayList<>();
		}
	}

	@Override
	public void deleteReviews(Long productId) {
		try {
			String url = reviewServiceUrl + "?productId=" + productId;
			LOG.debug("Will call the deleteReviews API on URL: {}", url);

			restTemplate.delete(url);

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public RecommendationDto createRecommendation(RecommendationDto body) {
		try {
			String url = recommendationServiceUrl;
			LOG.debug("Will post a new recommendation to URL: {}", url);

			RecommendationDto recommendation = restTemplate.postForObject(url, body, RecommendationDto.class);
			LOG.debug("Created a recommendation with id: {}", recommendation.getProductId());

			return recommendation;

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	@Override
	public List<RecommendationDto> getRecommendations(Long productId) {

		try {
			String url = recommendationServiceUrl + productId;

			LOG.debug("Will call getRecommendations API on URL: {}", url);
			List<RecommendationDto> recommendationDtos = restTemplate
					.exchange(url, GET, null, new ParameterizedTypeReference<List<RecommendationDto>>() {
					})
					.getBody();

			LOG.debug("Found {} recommendations for a product with id: {}", recommendationDtos.size(), productId);
			return recommendationDtos;

		} catch (Exception ex) {
			LOG.warn("Got an exception while requesting recommendations, return zero recommendations: {}", ex.getMessage());
			return new ArrayList<>();
		}
	}

	@Override
	public void deleteRecommendations(Long productId) {
		try {
			String url = recommendationServiceUrl + "?productId=" + productId;
			LOG.debug("Will call the deleteRecommendations API on URL: {}", url);

			restTemplate.delete(url);

		} catch (HttpClientErrorException ex) {
			throw handleHttpClientException(ex);
		}
	}

	private String getErrorMessage(HttpClientErrorException ex) {
		try {
			return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
		} catch (IOException ioex) {
			return ex.getMessage();
		}
	}

	private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
		switch (Objects.requireNonNull(HttpStatus.resolve(ex.getStatusCode().value()))) {
			case NOT_FOUND -> {
				return new NotFoundException(getErrorMessage(ex));
			}
			case UNPROCESSABLE_ENTITY -> {
				return new InvalidInputException(getErrorMessage(ex));
			}
			default -> {
				LOG.warn("Got an unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
				LOG.warn("Error body: {}", ex.getResponseBodyAsString());
				return ex;
			}
		}
	}

}
