package co.istad.util.reactor;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;

public class ReactorTest {

	@Test
	void testFlux() {
		List<Integer> list = new ArrayList<>();
		Flux.just(1, 2, 3, 4)
				.filter(n -> n % 2 == 0)
				.map(n -> n * 2)
				.log()
				.subscribe(list::add);
		Assertions.assertThat(list).containsExactly(4, 8);
	}

	@Test
	void testFluxBlocking() {
		List<Integer> list = Flux.just(1, 2, 3, 4)
				.filter(n -> n % 2 == 0)
				.map(n -> n * 2)
				.log()
				.collectList()
				.block();
		Assertions.assertThat(list).containsExactly(4, 8);
	}

}
