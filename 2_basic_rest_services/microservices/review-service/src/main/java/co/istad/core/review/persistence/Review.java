package co.istad.core.review.persistence;

import jakarta.persistence.*;
import lombok.*;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "reviews",
        indexes = {@Index(name = "reviews_unique_idx", unique = true, columnList = "productId,reviewId")})
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;
    private Long productId;
    private Long reviewId;
    private String author;
    private String subject;
    private String content;

}
