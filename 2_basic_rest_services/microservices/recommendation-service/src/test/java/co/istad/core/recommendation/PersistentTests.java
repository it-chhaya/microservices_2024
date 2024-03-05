package co.istad.core.recommendation;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import co.istad.core.recommendation.persistence.Recommendation;
import co.istad.core.recommendation.persistence.RecommendationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;

@DataMongoTest
public class PersistentTests extends MongoDbTestBase {

	@Autowired
	private RecommendationRepository repository;

	private Recommendation savedEntity;

	@BeforeEach
	void setupDb() {
		repository.deleteAll().block();

		Recommendation entity = Recommendation.builder()
				.productId(1L)
				.recommendationId(2L)
				.author("a")
				.rate(3)
				.content("c")
				.build();
		savedEntity = repository.save(entity).block();

		assertEqualsRecommendation(entity, savedEntity);
	}


	@Test
	void create() {

		Recommendation newEntity = Recommendation.builder()
				.productId(1L)
				.recommendationId(1L)
				.author("a")
				.rate(3)
				.content("c")
				.build();
		repository.save(newEntity).block();

		Recommendation foundEntity = repository.findById(newEntity.getId()).block();
		assertEqualsRecommendation(newEntity, foundEntity);

		assertEquals(2, (long)repository.count().block());
	}

	@Test
	void update() {
		savedEntity.setAuthor("a2");
		repository.save(savedEntity).block();

		Recommendation foundEntity = repository.findById(savedEntity.getId()).block();
		assertEquals(1, (long)foundEntity.getVersion());
		assertEquals("a2", foundEntity.getAuthor());
	}

	@Test
	void delete() {
		repository.delete(savedEntity).block();
		assertFalse(repository.existsById(savedEntity.getId()).block());
	}

	@Test
	void getByProductId() {
		List<Recommendation> entityList = repository.findByProductId(savedEntity.getProductId()).collectList().block();

		assertThat(entityList, hasSize(1));
		assertEqualsRecommendation(savedEntity, entityList.get(0));
	}

	@Test
	void duplicateError() {
		assertThrows(DuplicateKeyException.class, () -> {
			Recommendation entity = Recommendation.builder()
					.productId(1L)
					.recommendationId(2L)
					.author("a")
					.rate(3)
					.content("c")
					.build();
			repository.save(entity).block();
		});
	}

	@Test
	void optimisticLockError() {

		// Store the saved entity in two separate entity objects
		Recommendation entity1 = repository.findById(savedEntity.getId()).block();
		Recommendation entity2 = repository.findById(savedEntity.getId()).block();

		// Update the entity using the first entity object
		entity1.setAuthor("a1");
		repository.save(entity1).block();

		//  Update the entity using the second entity object.
		// This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
		assertThrows(OptimisticLockingFailureException.class, () -> {
			entity2.setAuthor("a2");
			repository.save(entity2).block();
		});

		// Get the updated entity from the database and verify its new sate
		Recommendation updatedEntity = repository.findById(savedEntity.getId()).block();
		assertEquals(1, (int)updatedEntity.getVersion());
		assertEquals("a1", updatedEntity.getAuthor());
	}

	private void assertEqualsRecommendation(Recommendation expectedEntity, Recommendation actualEntity) {
		assertEquals(expectedEntity.getId(),               actualEntity.getId());
		assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
		assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
		assertEquals(expectedEntity.getRecommendationId(), actualEntity.getRecommendationId());
		assertEquals(expectedEntity.getAuthor(),           actualEntity.getAuthor());
		assertEquals(expectedEntity.getRate(),           actualEntity.getRate());
		assertEquals(expectedEntity.getContent(),          actualEntity.getContent());
	}

}
