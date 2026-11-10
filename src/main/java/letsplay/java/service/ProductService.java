package letsplay.java.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import letsplay.java.dto.Response.ProductDto;
import letsplay.java.entity.Product;
import letsplay.java.entity.User;
import letsplay.java.repository.ProductRepository;
import letsplay.java.repository.UserRepository;

@Service
public class ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private UserRepository userRepository;

    public List<ProductDto> getProducts() {
        List<Product> products = productRepository.findAll();
        return products.stream().map((product) -> mapToDto(product)).toList();
    }

    public ProductDto getProduct(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        return mapToDto(product);
    }

    public ProductDto createProduct(letsplay.java.dto.Request.ProductDto request, String userId) {
        Product newProduct = Product.builder().name(request.title()).description(request.description())
                .price(request.price()).userId(userId).build();
        productRepository.save(newProduct);
        return mapToDto(newProduct);
    }

    public ProductDto updateProduct(letsplay.java.dto.Request.ProductDto request, String userId, String productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!product.getUserId().equals(userId) || !user.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not update this product");
        }
        product.setName(request.title());
        product.setDescription(request.description());
        product.setPrice(request.price());
        productRepository.save(product);
        return mapToDto(product);
    }

    public String deleteProduct(String userId, String productId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (!product.getUserId().equals(userId) || !user.getRole().equals("ADMIN")) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can not delete this product");
        }
        productRepository.delete(product);
        return "Product deleted successfully";
    }

    private ProductDto mapToDto(Product product) {
        return new ProductDto(product.getId(), product.getName(), product.getDescription(), product.getPrice(),
                product.getUserId());
    }
}