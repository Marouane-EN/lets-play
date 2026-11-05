package letsplay.java.entity;

import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "productes")
public class Product {
    @Id
    private String id;
    private String name;
    private String description;
    private double price;
    @Indexed
    private String userId;
}