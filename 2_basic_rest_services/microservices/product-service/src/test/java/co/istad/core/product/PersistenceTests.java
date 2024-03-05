package co.istad.core.product;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import co.istad.core.product.persistence.Product;
import co.istad.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

	@Autowired
	private ProductRepository repository;

	private Product savedEntity;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();

		Product entity = Product.builder()
				.productId(1L)
				.name("n")
				.weight(1)
				.build();
		savedEntity = repository.save(entity);

		assertEqualsProduct(entity, savedEntity);
	}


	@Test
	void create() {

		Product newEntity = Product.builder()
				.productId(2L)
				.name("n")
				.weight(2)
				.build();
		repository.save(newEntity);

		Product foundEntity = repository.findById(newEntity.getId()).get();
		assertEqualsProduct(newEntity, foundEntity);

		assertEquals(2, repository.count());
	}

	@Test
	void update() {
		savedEntity.setName("n2");
		repository.save(savedEntity);

		Product foundEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, foundEntity.getVersion());
		assertEquals("n2", foundEntity.getName());
	}

	@Test
	void delete() {
		repository.delete(savedEntity);
		assertFalse(repository.existsById(savedEntity.getId()));
	}

	@Test
	void getByProductId() {
		Optional<Product> entity = repository.findByProductId(savedEntity.getProductId());

		assertTrue(entity.isPresent());
		assertEqualsProduct(savedEntity, entity.get());
	}

	/*@Test
	void duplicateError() {
		assertThrows(DuplicateKeyException.class, () -> {
			Product entity = new Product();
			entity.setProductId(savedEntity.getProductId());
			entity.setName("n");
			entity.setWeight(1);
			repository.save(entity);
		});
	}*/

	@Test
	void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		Product entity1 = repository.findById(savedEntity.getId()).get();
		Product entity2 = repository.findById(savedEntity.getId()).get();

		// Update the entity using the first entity object
		entity1.setName("n1");
		repository.save(entity1);

		// Update the entity using the second entity object.
		// This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
		assertThrows(OptimisticLockingFailureException.class, () -> {
			entity2.setName("n2");
			repository.save(entity2);
		});

		// Get the updated entity from the database and verify its new state
		Product updatedEntity = repository.findById(savedEntity.getId()).get();
		assertEquals(1, updatedEntity.getVersion());
		assertEquals("n1", updatedEntity.getName());
	}

	@Test
	void paging() {

		repository.deleteAll();

		List<Product> newProducts = rangeClosed(1001, 1010)
				.mapToObj(i -> Product.builder()
						.productId((long) i)
						.name("name " + i)
						.weight(i)
						.build())
				.collect(Collectors.toList());
		repository.saveAll(newProducts);

		Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
		nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
		nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
		nextPage = testNextPage(nextPage, "[1009, 1010]", false);
	}

	private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
		Page<Product> productPage = repository.findAll(nextPage);
		assertEquals(expectedProductIds, productPage.getContent().stream().map(p -> p.getProductId()).collect(Collectors.toList()).toString());
		assertEquals(expectsNextPage, productPage.hasNext());
		return productPage.nextPageable();
	}

	private void assertEqualsProduct(Product expectedEntity, Product actualEntity) {
		assertEquals(expectedEntity.getId(),               actualEntity.getId());
		assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
		assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
		assertEquals(expectedEntity.getName(),           actualEntity.getName());
		assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
	}

}
