package letsplay.java.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import letsplay.java.config.SecurityUser;
import letsplay.java.dto.Request.*;
import letsplay.java.dto.Response.*;
import letsplay.java.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<?> getProducts() {
        List<ResponseProductDto> products = productService.getProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProduct(@PathVariable String id) {
        ResponseProductDto product = productService.getProduct(id);
        return ResponseEntity.ok(product);
    }

    @PostMapping
    public ResponseEntity<?> createProduct(@Valid @RequestBody RequestProductDto request,
            @AuthenticationPrincipal SecurityUser user) {
        ResponseProductDto product = productService.createProduct(request, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(product);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateProduct(@Valid @RequestBody RequestProductDto request,
            @PathVariable String id, @AuthenticationPrincipal SecurityUser user) {
        ResponseProductDto product = productService.updateProduct(request, user.getId(), id);
        return ResponseEntity.ok(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable String id, @AuthenticationPrincipal SecurityUser user) {
        String message = productService.deleteProduct(user.getId(), id);
        return ResponseEntity.ok(Map.of("message", message));
    }

}