package com.warsha.erp.services;
import com.warsha.erp.entities.Product;
import com.warsha.erp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;



@Service
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;
    private final GoogleDriveService googleDriveService;

    public ProductService(ProductRepository productRepository, GoogleDriveService googleDriveService) {
        this.productRepository = productRepository;
        this.googleDriveService = googleDriveService;
    }


    public List<Product> getAllProducts() {
        return productRepository.findAll();
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

        // Step 2: Save product (now SKU is not null and unique)
        Product savedProduct = productRepository.save(product);

//        GoogleDriveService googleDriveService = new GoogleDriveService();

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
        productRepository.deleteById(id);
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }
}

