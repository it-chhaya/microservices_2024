package co.istad.composite.product;

import co.istad.api.composite.product.ProductAggregate;
import co.istad.api.composite.product.RecommendationSummary;
import co.istad.api.composite.product.ReviewSummary;
import co.istad.api.core.product.ProductDto;
import co.istad.api.core.recommendation.RecommendationDto;
import co.istad.api.core.review.ReviewDto;
import co.istad.api.event.Event;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.ArrayList;
import java.util.List;

import static co.istad.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.ACCEPTED;
import static reactor.core.publisher.Mono.just;

/*@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        properties = {"spring.main.allow-bean-definition-overriding=true"})
@Import(TestChannelBinderConfiguration.class)*/
class MessagingTests {

    /*private static final Logger LOG = LoggerFactory.getLogger(MessagingTests.class);

    @Autowired
    private WebTestClient client;

    @Autowired
    private OutputDestination target;

    @BeforeEach
    void setUp() {
        purgeMessages("products");
        purgeMessages("recommendations");
        purgeMessages("reviews");
    }

    @Test
    void createCompositeProductDto1() {

        ProductAggregate composite = new ProductAggregate(1L, "name", 1, null, null, null);
        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assert one expected new ProductDto event queued up
        assertEquals(1, productMessages.size());

        Event<Long, ProductDto> expectedEvent = new Event<>(Event.Type.CREATE, composite.getProductId(), new ProductDto(composite.getProductId(), composite.getName(), composite.getWeight(), null));
        System.out.println(productMessages.get(0));
        System.out.println(is(sameEventExceptCreatedAt(expectedEvent)));
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedEvent)));

        // Assert no recommendation and review events
        assertEquals(0, recommendationMessages.size());
        assertEquals(0, reviewMessages.size());
    }

    @Test
    void createCompositeProductDto2() {

        ProductAggregate composite = new ProductAggregate(1L, "name", 1,
                singletonList(new RecommendationSummary(1L, "a", 1, "c")),
                singletonList(new ReviewSummary(1L, "a", "s", "c")), null);

        postAndVerifyProduct(composite, ACCEPTED);

        final List<String> productMessages = getMessages("products");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assert one create ProductDto event queued up
        assertEquals(1, productMessages.size());

        Event<Long, ProductDto> expectedProductEvent = new Event<>(Event.Type.CREATE, composite.getProductId(),
                ProductDto.builder()
                        .productId(1L)
                        .name("name")
                        .weight(1)
                        .build());
        assertThat(productMessages.get(0), is(sameEventExceptCreatedAt(expectedProductEvent)));

        // Assert one create recommendation event queued up
        assertEquals(1, recommendationMessages.size());

        RecommendationSummary rec = composite.getRecommendations().get(0);
        Event<Long, RecommendationDto> expectedRecommendationEvent =
                new Event<>(Event.Type.CREATE, composite.getProductId(),
                        new RecommendationDto(composite.getProductId(), rec.recommendationId(), rec.author(), rec.rate(), rec.content(), null));
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Assert one create review event queued up
        assertEquals(1, reviewMessages.size());

        ReviewSummary rev = composite.getReviews().get(0);
        Event<Long, ReviewDto> expectedReviewEvent =
                new Event<>(Event.Type.CREATE, composite.getProductId(),
                        ReviewDto.builder()
                                .productId(composite.getProductId())
                                .reviewId(rev.reviewId())
                                .author(rev.author())
                                .subject(rev.subject())
                                .content(rev.content())
                                .build());
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    @Test
    void deleteCompositeProductDto() {
        deleteAndVerifyProductDto(1L, ACCEPTED);

        final List<String> ProductDtoMessages = getMessages("ProductDtos");
        final List<String> recommendationMessages = getMessages("recommendations");
        final List<String> reviewMessages = getMessages("reviews");

        // Assert one delete ProductDto event queued up
        assertEquals(1, ProductDtoMessages.size());

        Event<Long, ProductDto> expectedProductDtoEvent = new Event<>(Event.Type.DELETE, 1L, null);
        assertThat(ProductDtoMessages.get(0), is(sameEventExceptCreatedAt(expectedProductDtoEvent)));

        // Assert one delete recommendation event queued up
        assertEquals(1, recommendationMessages.size());

        Event<Long, ProductDto> expectedRecommendationEvent = new Event<>(Event.Type.DELETE, 1L, null);
        assertThat(recommendationMessages.get(0), is(sameEventExceptCreatedAt(expectedRecommendationEvent)));

        // Assert one delete review event queued up
        assertEquals(1, reviewMessages.size());

        Event<Long, ProductDto> expectedReviewEvent =  new Event<>(Event.Type.DELETE, 1L, null);
        assertThat(reviewMessages.get(0), is(sameEventExceptCreatedAt(expectedReviewEvent)));
    }

    private void purgeMessages(String bindingName) {
        getMessages(bindingName);
    }

    private List<String> getMessages(String bindingName) {
        List<String> messages = new ArrayList<>();
        boolean anyMoreMessages = true;

        while (anyMoreMessages) {
            Message<byte[]> message = getMessage(bindingName);

            if (message == null) {
                anyMoreMessages = false;

            } else {
                messages.add(new String(message.getPayload()));
            }
        }
        return messages;
    }

    private Message<byte[]> getMessage(String bindingName) {
        try {
            return target.receive(0, bindingName);
        } catch (NullPointerException npe) {
            // If the messageQueues member variable in the target object contains no queues when the receive method is called, it will cause a NPE to be thrown.
            // So we catch the NPE here and return null to indicate that no messages were found.
            LOG.error("getMessage() received a NPE with binding = {}", bindingName);
            return null;
        }
    }

    private void postAndVerifyProduct(ProductAggregate productAggregate, HttpStatus expectedStatus) {
        client.post()
                .uri("/product-composite")
                .body(just(productAggregate), ProductAggregate.class)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }

    private void deleteAndVerifyProductDto(Long productId, HttpStatus expectedStatus) {
        client.delete()
                .uri("/product-composite/" + productId)
                .exchange()
                .expectStatus().isEqualTo(expectedStatus);
    }*/
}