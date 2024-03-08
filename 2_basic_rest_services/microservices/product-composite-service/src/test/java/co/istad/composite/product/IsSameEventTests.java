package co.istad.composite.product;

import static co.istad.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import co.istad.api.core.product.ProductDto;
import co.istad.api.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class IsSameEventTests {

    ObjectMapper mapper = new ObjectMapper();

    @Test
    void testEventObjectCompare() throws JsonProcessingException {

        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events
        Event<Long, ProductDto> event1 = Event.<Long, ProductDto>builder()
                .eventType(Event.Type.CREATE)
                .key(1L)
                .data(ProductDto.builder()
                        .productId(1L)
                        .name("name")
                        .weight(1)
                        .build())
                .build();
        Event<Long, ProductDto> event2 = Event.<Long, ProductDto>builder()
                .eventType(Event.Type.CREATE)
                .key(1L)
                .data(ProductDto.builder()
                        .productId(1L)
                        .name("name")
                        .weight(1)
                        .build())
                .build();
        Event<Long, ProductDto> event3 = Event.<Long, ProductDto>builder()
                .eventType(Event.Type.DELETE)
                .key(1L)
                .build();
        Event<Long, ProductDto> event4 = Event.<Long, ProductDto>builder()
                .eventType(Event.Type.CREATE)
                .key(1L)
                .data(ProductDto.builder()
                        .productId(2L)
                        .name("name")
                        .weight(1)
                        .build())
                .build();

        String event1Json = mapper.writeValueAsString(event1);

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        assertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
