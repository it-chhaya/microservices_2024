package co.istad.core.review;

import co.istad.core.review.persistence.Review;
import co.istad.core.review.persistence.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PersistenceTests extends PostgresqlTestBase {

    @Autowired
    private ReviewRepository reviewRepository;

    private Review savedEntity;

    @BeforeEach
    void setupDb() {
        reviewRepository.deleteAll();

        Review entity = Review.builder()
                .id(1L)
                .productId(2L)
                .subject("a")
                .author("s")
                .content("c")
                .build();
        savedEntity = reviewRepository.save(entity);

        assertEqualsReview(entity, savedEntity);
    }

    @Test
    void create() {

        Review newEntity = Review.builder()
                .id(2L)
                .productId(2L)
                .subject("a")
                .author("s")
                .content("c")
                .build();
        reviewRepository.save(newEntity);

        Review foundEntity = reviewRepository.findById(newEntity.getId()).get();
        assertEqualsReview(newEntity, foundEntity);
        assertEquals(2, reviewRepository.count());
    }

    @Test
    void update() {
        savedEntity.setAuthor("a2");
        reviewRepository.save(savedEntity);

        Review foundEntity = reviewRepository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("a2", foundEntity.getAuthor());
    }

    @Test
    void delete() {
        reviewRepository.delete(savedEntity);
        assertFalse(reviewRepository.existsById(savedEntity.getId()));
    }

    @Test
    void getByProductId() {
        List<Review> entityList = reviewRepository.findByProductId(savedEntity.getProductId());

        assertThat(entityList, hasSize(1));
        assertEqualsReview(savedEntity, entityList.get(0));
    }

    @Test
    void duplicateError() {
        assertThrows(DataIntegrityViolationException.class, () -> {
            Review entity = Review.builder()
                    .id(1L)
                    .productId(2L)
                    .subject("a")
                    .author("s")
                    .content("c")
                    .build();
            reviewRepository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {

        // Store the saved entity in two separate entity objects
        Review entity1 = reviewRepository.findById(savedEntity.getId()).get();
        Review entity2 = reviewRepository.findById(savedEntity.getId()).get();

        // Update the entity using the first entity object
        entity1.setAuthor("a1");
        reviewRepository.save(entity1);

        // Update the entity using the second entity object.
        // This should fail since the second entity now holds an old version number, i.e. an Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setAuthor("a2");
            reviewRepository.save(entity2);
        });

        // Get the updated entity from the database and verify its new sate
        Review updatedEntity = reviewRepository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("a1", updatedEntity.getAuthor());
    }

    private void assertEqualsReview(Review expectedEntity, Review actualEntity) {
        assertEquals(expectedEntity.getId(), actualEntity.getId());
        assertEquals(expectedEntity.getVersion(), actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(), actualEntity.getProductId());
        assertEquals(expectedEntity.getReviewId(), actualEntity.getReviewId());
        assertEquals(expectedEntity.getAuthor(), actualEntity.getAuthor());
        assertEquals(expectedEntity.getSubject(), actualEntity.getSubject());
        assertEquals(expectedEntity.getContent(), actualEntity.getContent());
    }

}
