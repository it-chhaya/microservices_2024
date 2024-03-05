package co.istad.api.event;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.ZonedDateTimeSerializer;
import lombok.Builder;

import java.time.ZonedDateTime;

@Builder
public record Event <K, T> (
		Type eventType,
		K key,
		T data,
		@JsonSerialize(using = ZonedDateTimeSerializer.class)
		ZonedDateTime eventCreatedAt
) {

	public enum Type {
		CREATE,
		DELETE
	}

}
