package co.istad.composite.product;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.CompositeReactiveHealthContributor;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.ReactiveHealthContributor;
import org.springframework.boot.actuate.health.ReactiveHealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class HealthCheckConfig {

    private final ProductCompositeIntegration compositeIntegration;

    @Bean
    ReactiveHealthContributor coreServiceReactiveHealthContributor() {

        final Map<String, ReactiveHealthIndicator> registry = new HashMap<>();
        registry.put("product", compositeIntegration::getProductHealth);
        registry.put("recommendation", compositeIntegration::getRecommendationHealth);
        registry.put("review", compositeIntegration::getReviewHealth);

        return CompositeReactiveHealthContributor.fromMap(registry);

    }

}
