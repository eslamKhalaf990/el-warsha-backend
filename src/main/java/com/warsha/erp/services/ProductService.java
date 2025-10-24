package com.warsha.erp.services;
import com.warsha.erp.dtos.ProductDTO;
import com.warsha.erp.entities.Product;
import com.warsha.erp.repository.ProductRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;
    private final GoogleDriveService googleDriveService;

    public ProductService(ProductRepository productRepository, GoogleDriveService googleDriveService) {
        this.productRepository = productRepository;
        this.googleDriveService = googleDriveService;
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

    public Resource getProductImage(String filename) throws MalformedURLException {
        Path filePath = Paths.get(filename).normalize();
        Resource resource = new UrlResource(filePath.toUri());

        if (resource.exists() && resource.isReadable()) {
            return resource;
        } else {
            return null;
        }
    }

    public Product createProduct(Product product, MultipartFile image) {
        product.setCreatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(product);

        if (image != null && !image.isEmpty()) {
            try {
                // Upload to Google Drive instead of local storage
                String imageUrl = googleDriveService.uploadFile(image);

                // Save the Drive public link in DB
                savedProduct.setImageUrl(imageUrl);
                productRepository.save(savedProduct);

            } catch (IOException e) {
                throw new RuntimeException("Failed to save image to Google Drive", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }


        return savedProduct;
    }

    public void updateProduct(Long id, Product updatedProduct) {
        Product existing = getProductById(id);
        existing.setName(updatedProduct.getName());
        existing.setDescription(updatedProduct.getDescription());
        existing.setBuyingPrice(updatedProduct.getBuyingPrice());
        existing.setSellingPrice(updatedProduct.getSellingPrice());
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
        existing.setQuantity(updatedProduct.getQuantity());
        existing.setCategory(updatedProduct.getCategory());
        existing.setUpdatedAt(LocalDateTime.now());

        if (image != null && !image.isEmpty()) {
            try {
                // Upload to Google Drive
                String imageUrl = googleDriveService.uploadFile(image);
                existing.setImageUrl(imageUrl);
            } catch (IOException e) {
                throw new RuntimeException("Failed to save image to Google Drive", e);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id " + id));

        // If product has an image, delete it from Google Drive
        if (product.getImageUrl() != null) {
            try {
                String fileId = extractFileId(product.getImageUrl());
                if (fileId != null) {
                    googleDriveService.deleteFile(fileId);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete image from Google Drive", e);
            }
        }

        // Soft delete: mark as deleted
        product.setDeleted("true");
        product.setUpdatedAt(LocalDateTime.now());
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private String extractFileId(String imageUrl) {
        try {
            URI uri = new URI(imageUrl);
            String query = uri.getQuery(); // export=view&id=ABC123XYZ
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2 && pair[0].equals("id")) {
                    return pair[1];
                }
            }
        } catch (Exception e) {
            // log error
        }
        return null;
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }
}

