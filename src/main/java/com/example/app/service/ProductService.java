package com.example.app.service;

import com.example.app.entity.Product;
import com.example.app.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Məhsul tapılmadı"));
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public List<Product> searchProducts(String name) {
        return productRepository.findByNameContainingIgnoreCase(name);
    }

    public boolean checkStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        return product.getStock() >= quantity;
    }

    public void reduceStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        if (product.getStock() < quantity) {
            throw new RuntimeException("Kifayət qədər stok yoxdur");
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }
}
