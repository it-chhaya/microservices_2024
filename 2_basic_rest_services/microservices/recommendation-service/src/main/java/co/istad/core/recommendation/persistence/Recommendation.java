package co.istad.core.recommendation.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "recommendations")
@CompoundIndex(name = " prod-rec-id",
        unique = true,
        def = "{'productId': 1, 'recommendationId': 1}")
public class Recommendation {

    @Id
    private String id;

    @Version
    private Integer version;

    private Long recommendationId;
    private Long productId;
    private String author;
    private Integer rate;
    private String content;

}
