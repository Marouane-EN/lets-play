package letsplay.java.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private String id;
    @Field("name")
    @Indexed(unique = true)
    private String username;
    @Indexed(unique = true)
    private String email;
    @Field("password")
    private String passwordHash;
    @Builder.Default
    private String role = "USER";
}