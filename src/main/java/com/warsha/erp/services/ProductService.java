package com.warsha.erp.services;
import com.warsha.erp.models.ProductModel;
import com.warsha.erp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<ProductModel> getAllProducts() {
        return productRepository.findAll();
    }

    public ProductModel getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Resource getProductImage(String filename) throws MalformedURLException {
        Path filePath = Paths.get(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            return null;
        }
    }

    public ProductModel createProduct(ProductModel product, MultipartFile image) {
        // Step 1: Generate UUID SKU before saving
        String generatedSku = "SKU-" + UUID.randomUUID();
        product.setSku(generatedSku);
        product.setCreatedAt(LocalDateTime.now());

        // Step 2: Save product (now SKU is not null and unique)
        ProductModel savedProduct = productRepository.save(product);

        // Step 3: Save product image
        if (image != null && !image.isEmpty()) {
            String uploadDir = "uploads/";
            try {
                Files.createDirectories(Paths.get(uploadDir));
                String fileName = generatedSku + "_" + image.getOriginalFilename();
                Path filePath = Paths.get(uploadDir, fileName);
                Files.write(filePath, image.getBytes());

                savedProduct.setImageUrl(filePath.toString());
                productRepository.save(savedProduct);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save image", e);
            }
        }

        return savedProduct;
    }

    public ProductModel updateProduct(Long id, ProductModel updatedProduct) {
        ProductModel existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setBuyingPrice(updatedProduct.getBuyingPrice());
        existing.setSellingPrice(updatedProduct.getSellingPrice());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCategory(updatedProduct.getCategory());
        existing.setSku(updatedProduct.getSku());
        existing.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }
}
