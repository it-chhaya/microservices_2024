package co.istad.composite.product;

import static co.istad.composite.product.IsSameEvent.sameEventExceptCreatedAt;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import co.istad.api.core.product.ProductDto;
import co.istad.api.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IsSameEventTests {

    ObjectMapper mapper = new ObjectMapper();

    //@Test
    void testEventObjectCompare() throws JsonProcessingException {

        // Event #1 and #2 are the same event, but occurs as different times
        // Event #3 and #4 are different events
        Event<Long, ProductDto> event1 = new Event<>(Event.Type.CREATE, 1L, new ProductDto(1L, "name", 1, null));
        Event<Long, ProductDto> event2 = new Event<>(Event.Type.CREATE, 1L, new ProductDto(1L, "name", 1, null));
        Event<Long, ProductDto> event3 = new Event<>(Event.Type.DELETE, 1L, null);
        Event<Long, ProductDto> event4 = new Event<>(Event.Type.CREATE, 1L, new ProductDto(2L, "name", 1, null));

        String event1Json = mapper.writeValueAsString(event1);
        String event2Json = mapper.writeValueAsString(event2);
        System.out.println("event1Json: " + event1Json);
        System.out.println(is(sameEventExceptCreatedAt(event2)));

        assertThat(event1Json, is(sameEventExceptCreatedAt(event2)));
        //assertThat(event1Json, not(sameEventExceptCreatedAt(event3)));
        //ssertThat(event1Json, not(sameEventExceptCreatedAt(event4)));
    }
}
