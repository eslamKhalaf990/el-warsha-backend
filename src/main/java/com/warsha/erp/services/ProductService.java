package com.warsha.erp.services;

import com.warsha.erp.dtos.ProductDTO;
import com.warsha.erp.entities.Product;
import com.warsha.erp.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    // This pulls the path from your application.properties,
    // defaulting to your Ubuntu path if not specified
    @Value("${image.upload.dir:/var/www/el-warsha/images/}")
    private String uploadDir;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productRepository.findAllNotDeleted();

        return products.stream()
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getProductID());
                    dto.setName(product.getName());
                    dto.setDeleted(product.getDeleted());
                    dto.setDeletedAt(product.getDeletedAt());
                    dto.setDescription(product.getDescription());
                    dto.setBuyingPrice(product.getBuyingPrice());
                    dto.setSellingPrice(product.getSellingPrice());
                    dto.setDiscount(product.getDiscount());
                    dto.setTotalPrice(product.getSellingPrice() - product.getDiscount());
                    dto.setImageUrl(product.getImageUrl());
                    dto.setSku(product.getSku());
                    dto.setQuantity(product.getQuantity());
                    dto.setCategoryId(product.getCategory().getCategoryId());
                    dto.setCategoryName(
                            product.getCategory() != null ? product.getCategory().getName() : null
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductsByCategoryId(Long categoryId) {
        List<Product> products = productRepository.findByCategoryId(categoryId);

        return products.stream()
                .map(product -> {
                    ProductDTO dto = new ProductDTO();
                    dto.setId(product.getProductID());
                    dto.setName(product.getName());
                    dto.setDeleted(product.getDeleted());
                    dto.setDeletedAt(product.getDeletedAt());
                    dto.setDescription(product.getDescription());
                    dto.setBuyingPrice(product.getBuyingPrice());
                    dto.setSellingPrice(product.getSellingPrice());
                    dto.setImageUrl(product.getImageUrl());
                    dto.setSku(product.getSku());
                    dto.setQuantity(product.getQuantity());
                    dto.setCategoryName(
                            product.getCategory() != null ? product.getCategory().getName() : null
                    );
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // You likely won't need this method anymore since Nginx serves the images,
    // but I've updated it to read from the local hard drive just in case.
    public Resource getProductImage(String filename) throws MalformedURLException {
        Path filePath = Paths.get(uploadDir).resolve(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            return null;
        }
    }

    public Product createProduct(Product product, MultipartFile image) {
        product.setCreatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = saveImageToDisk(image);
                product.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image to disk", e);
            }
        }

        return productRepository.save(product);
    }

    public void updateProduct(Long id, Product updatedProduct) {
        Product existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setBuyingPrice(updatedProduct.getBuyingPrice());
        existing.setSellingPrice(updatedProduct.getSellingPrice());
        existing.setDiscount(updatedProduct.getDiscount());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCategory(updatedProduct.getCategory());
        existing.setUpdatedAt(LocalDateTime.now());
        productRepository.save(existing);
    }

    public Product updateProductWithImage(Long id, Product updatedProduct, MultipartFile image) {
        Product existing = getProductById(id);

        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setBuyingPrice(updatedProduct.getBuyingPrice());
        existing.setSellingPrice(updatedProduct.getSellingPrice());
        existing.setDiscount(updatedProduct.getDiscount());
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCategory(updatedProduct.getCategory());
        existing.setUpdatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            try {
                // Optionally delete the old image from disk to save space
                deleteImageFromDisk(existing.getImageUrl());

                // Save the new image
                String imageUrl = saveImageToDisk(image);
                existing.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image to disk", e);
            }
        }

        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));

        // Delete the physical file from the hard drive
        deleteImageFromDisk(product.getImageUrl());

        // Soft delete: mark as deleted
        product.setDeleted("true");
        product.setUpdatedAt(LocalDateTime.now());
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }

    // --- Private Helper Methods ---

    private String saveImageToDisk(MultipartFile image) throws IOException {
        Path uploadPath = Paths.get(uploadDir);

        // Create the directory if it doesn't exist yet
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Keep original extension, but give it a UUID name to prevent overwriting
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String uniqueFileName = UUID.randomUUID().toString() + extension;
        Path filePath = uploadPath.resolve(uniqueFileName);

        // Copy the file to the target location
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return the exact string that needs to be saved in the database
        return "/images/" + uniqueFileName;
    }

    private void deleteImageFromDisk(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Strip the "/images/" part to get just the filename
            String filename = imageUrl.replace("/images/", "");
            Path filePath = Paths.get(uploadDir).resolve(filename).normalize();

            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            System.err.println("Failed to delete image file: " + e.getMessage());
        }
    }
}