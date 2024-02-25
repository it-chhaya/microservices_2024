package co.istad.core.product.persistence;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collation = "products")
public class Product {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private Long productId;
    private String name;
    private Integer weight;

}
