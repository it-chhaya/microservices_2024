package co.istad.core.product.service;

import co.istad.api.core.product.ProductDto;
import co.istad.api.core.product.ProductService;
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

	private final ProductService productService;

	@Bean
	Consumer<Event<Long, ProductDto>> messageProcessor() {
		return new Consumer<Event<Long, ProductDto>>() {
			@Override
			public void accept(Event<Long, ProductDto> event) {
				log.info("Process message created at {}...",  event);

				switch (event.getEventType()) {
					case CREATE -> {
						ProductDto productDto = event.getData();
						log.info("Create product with ID: {}", productDto.getProductId());
					}
					case DELETE -> {
						Long productId = event.getKey();
						log.info("Delete product with ProductID: {}", productId);
						productService.deleteProduct(productId).block();
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
