package letsplay.java.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import letsplay.java.entity.Product;

public interface ProductRepository extends MongoRepository<Product, String> {

}