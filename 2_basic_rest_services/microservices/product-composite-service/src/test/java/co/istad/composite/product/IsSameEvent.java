package co.istad.composite.product;

import co.istad.api.event.Event;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IsSameEvent extends TypeSafeMatcher<String> {

    private static final Logger LOG = LoggerFactory.getLogger(IsSameEvent.class);

    private final ObjectMapper mapper = new ObjectMapper();

    private final Event expectedEvent;


    private IsSameEvent(Event expectedEvent) {
        this.expectedEvent = expectedEvent;
    }

    @Override
    protected boolean matchesSafely(String eventAsJson) {

        if (expectedEvent == null) {
            return false;
        }

        LOG.info("Convert the following json string to a map: {}", eventAsJson);
        Map mapEvent = convertJsonStringToMap(eventAsJson);
        mapEvent.remove("eventCreatedAt");
        LOG.info("Converted the following json string to a map: {}", mapEvent);

        LOG.info("Expected event: {}", expectedEvent);
        Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);

        LOG.info("Got the map: {}", mapEvent.hashCode());
        LOG.info("Compare to the expected map: {}", mapExpectedEvent.hashCode());
        LOG.info("Result: {}", mapEvent.equals(mapExpectedEvent));

        return mapEvent.entrySet().stream()
                .allMatch(o -> {
					Map.Entry entry = (Map.Entry) o;
					return entry.getValue().equals(mapExpectedEvent.get(entry.getKey()));
				});
    }

    @Override
    public void describeTo(Description description) {
        /*Map mapExpectedEvent = getMapWithoutCreatedAt(expectedEvent);
        mapExpectedEvent.remove("eventCreatedAt");*/
        String expectedJson = convertObjectToJsonString(expectedEvent);
        LOG.info("expectedJson: {}", expectedJson);
        description.appendText("expected to look like " + expectedJson);
    }

    public static Matcher<String> sameEventExceptCreatedAt(Event expectedEvent) {
        return new IsSameEvent(expectedEvent);
    }

    private Map getMapWithoutCreatedAt(Event event) {
        Map mapEvent = convertObjectToMap(event);
        mapEvent.remove("eventCreatedAt");
        return mapEvent;
    }

    private Map convertObjectToMap(Object object) {
        JsonNode node = mapper.convertValue(object, JsonNode.class);
        return mapper.convertValue(node, Map.class);
    }

    private String convertObjectToJsonString(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private Map convertJsonStringToMap(String eventAsJson) {
        try {
            return mapper.readValue(eventAsJson, new TypeReference<HashMap>(){});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
