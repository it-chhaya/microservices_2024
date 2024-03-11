package co.istad.composite.product;

import co.istad.api.composite.product.*;
import co.istad.api.core.product.ProductDto;
import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.review.ReviewDto;
import co.istad.util.http.ServiceUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class ProductCompositeServiceImpl implements ProductCompositeService {

    private final ServiceUtil serviceUtil;
    private final ProductCompositeIntegration integration;

    public ProductCompositeServiceImpl(ServiceUtil serviceUtil, ProductCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }

    @Override
    public Mono<Void> createProduct(ProductAggregate body) {

        try {

            List<Mono> monoList = new ArrayList<>();

            log.info("Will create a new composite entity for product.id: {}", body.getProductId());

            ProductDto productDto = ProductDto.builder()
                    .productId(body.getProductId())
                    .name(body.getName())
                    .weight(body.getWeight())
                    .build();

            monoList.add(integration.createProduct(productDto));

            if (body.getRecommendations() != null) {
                body.getRecommendations().forEach(r -> {
                    RecommendationDto recommendationDto = RecommendationDto.builder()
                            .productId(body.getProductId())
                            .recommendationId(r.recommendationId())
                            .author(r.author())
                            .rate(r.rate())
                            .content(r.content())
                            .build();
                    monoList.add(integration.createRecommendation(recommendationDto));
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    ReviewDto review = ReviewDto.builder()
                            .productId(body.getProductId())
                            .reviewId(r.reviewId())
                            .author(r.author())
                            .subject(r.subject())
                            .content(r.content())
                            .build();
                    monoList.add(integration.createReview(review));
                });
            }

            log.debug("createCompositeProduct: composite entities created for productId: {}", body.getProductId());

            return Mono.zip(r -> "", monoList.toArray(new Mono[0]))
                    .doOnError(ex -> log.warn("createCompositeProduct failed: {}", ex.toString()))
                    .then();
        } catch (RuntimeException ex) {
            log.warn("createCompositeProduct failed: {}", ex.toString());
            throw ex;
        }
    }

    @Override
    public Mono<ProductAggregate> getProduct(Long productId) {

        log.info("Will get composite product info for product.id={}", productId);

        return Mono.zip(objects ->
                                createProductAggregate((ProductDto) objects[0], (List<RecommendationDto>) objects[1], (List<ReviewDto>) objects[2], serviceUtil.getServiceAddress()),
                        integration.findProductById(productId),
                        integration.getRecommendations(productId).collectList(),
                        integration.getReviews(productId).collectList()
                )
                .doOnError(ex -> log.warn("getCompositeProduct failed: {}", ex.toString()))
                .log();
    }

    @Override
    public Mono<Void> deleteProduct(Long productId) {

        try {

            log.info("Will delete a product aggregate for product.id: {}", productId);

            return Mono.zip(
                            r -> "",
                            integration.deleteProduct(productId),
                            integration.deleteRecommendations(productId),
                            integration.deleteReviews(productId)
                    )
                    .doOnError(ex -> log.warn("delete failed: {}", ex.toString()))
                    .log(log.getName(), Level.FINE)
                    .then();
        } catch (RuntimeException ex) {
            log.warn("deleteCompositeProduct failed: {}", ex.toString());
            throw ex;
        }
    }

    private ProductAggregate createProductAggregate(
            ProductDto productDto,
            List<RecommendationDto> recommendationDtoList,
            List<ReviewDto> reviewDtoList,
            String serviceAddress) {

        // 1. Setup product info
        Long productId = productDto.getProductId();
        String name = productDto.getName();
        int weight = productDto.getWeight();

        // 2. Copy summary recommendation info, if available
        List<RecommendationSummary> recommendationSummaries =
                (recommendationDtoList == null) ? null : recommendationDtoList.stream()
                        .map(r -> new RecommendationSummary(r.recommendationId(), r.author(), r.rate(), r.content()))
                        .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries =
                (reviewDtoList == null) ? null : reviewDtoList.stream()
                        .map(r -> new ReviewSummary(r.reviewId(), r.author(), r.subject(), r.content()))
                        .collect(Collectors.toList());

        // 4. Create info regarding the involved microservices addresses
        String productAddress = productDto.getServiceAddress();
        String reviewAddress = (reviewDtoList != null && !reviewDtoList.isEmpty()) ? reviewDtoList.get(0).serviceAddress() : "";
        String recommendationAddress = (recommendationDtoList != null && !recommendationDtoList.isEmpty()) ? recommendationDtoList.get(0).serviceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, productAddress, reviewAddress, recommendationAddress);

        return ProductAggregate.builder()
                .productId(productId)
                .name(name)
                .weight(weight)
                .recommendations(recommendationSummaries)
                .reviews(reviewSummaries)
                .serviceAddresses(serviceAddresses)
                .build();
    }
}
