package co.istad.core.product;

import co.istad.core.product.persistence.Product;
import co.istad.core.product.persistence.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import reactor.test.StepVerifier;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase {

	@Autowired
	private ProductRepository repository;

	private Product savedEntity;

	@BeforeEach
	void setupDb() {

		StepVerifier.create(repository.deleteAll()).verifyComplete();

		Product entity = Product.builder()
				.productId(1L)
				.name("n")
				.weight(1)
				.build();

		StepVerifier.create(repository.save(entity))
				.expectNextMatches(createdEntity -> {
					savedEntity = createdEntity;
					return areProductEqual(entity, savedEntity);
				})
				.verifyComplete();

		areProductEqual(entity, savedEntity);
	}


	@Test
	void create() {
		Product newEntity = Product.builder()
				.productId(2L)
				.name("n")
				.weight(2)
				.build();

		StepVerifier.create(repository.save(newEntity))
				.expectNextMatches(createdEntity -> newEntity.getProductId() == createdEntity.getProductId())
				.verifyComplete();

		StepVerifier.create(repository.findById(newEntity.getId()))
				.expectNextMatches(foundEntity -> areProductEqual(newEntity, foundEntity))
				.verifyComplete();

		StepVerifier.create(repository.count()).expectNext(2L).verifyComplete();
	}

	@Test
	void update() {
		savedEntity.setName("n2");
		StepVerifier.create(repository.save(savedEntity))
				.expectNextMatches(updatedEntity -> updatedEntity.getName().equals("n2"))
				.verifyComplete();

		StepVerifier.create(repository.findById(savedEntity.getId()))
				.expectNextMatches(foundEntity ->
						foundEntity.getVersion() == 1
								&& foundEntity.getName().equals("n2"))
				.verifyComplete();
	}

	@Test
	void delete() {
		StepVerifier.create(repository.delete(savedEntity)).verifyComplete();
		StepVerifier.create(repository.existsById(savedEntity.getId())).expectNext(false).verifyComplete();
	}

	@Test
	void getByProductId() {

		StepVerifier.create(repository.findByProductId(savedEntity.getProductId()))
				.expectNextMatches(foundEntity -> areProductEqual(savedEntity, foundEntity))
				.verifyComplete();
	}

	@Test
	void duplicateError() {
		Product entity = Product.builder()
				.productId(savedEntity.getProductId())
				.name("n")
				.weight(1)
				.build();
		StepVerifier.create(repository.save(entity)).expectError(DuplicateKeyException.class).verify();
	}

	@Test
	void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		Product entity1 = repository.findById(savedEntity.getId()).block();
		Product entity2 = repository.findById(savedEntity.getId()).block();

		// Update the entity using the first entity object
		entity1.setName("n1");
		repository.save(entity1).block();

		//  Update the entity using the second entity object.
		// This should fail since the second entity now holds a old version number, i.e. a Optimistic Lock Error
		StepVerifier.create(repository.save(entity2)).expectError(OptimisticLockingFailureException.class).verify();

		// Get the updated entity from the database and verify its new sate
		StepVerifier.create(repository.findById(savedEntity.getId()))
				.expectNextMatches(foundEntity ->
						foundEntity.getVersion() == 1
								&& foundEntity.getName().equals("n1"))
				.verifyComplete();
	}

	private boolean areProductEqual(Product expectedEntity, Product actualEntity) {
		return
				(expectedEntity.getId().equals(actualEntity.getId()))
						&& (expectedEntity.getVersion() == actualEntity.getVersion())
						&& (expectedEntity.getProductId() == actualEntity.getProductId())
						&& (expectedEntity.getName().equals(actualEntity.getName()))
						&& (expectedEntity.getWeight() == actualEntity.getWeight());
	}

}
