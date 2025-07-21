package com.warsha.erp.services;
import com.warsha.erp.models.ProductModel;
import com.warsha.erp.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

    public ProductModel createProduct(ProductModel product) {
        product.setCreatedAt(LocalDateTime.now());
        return productRepository.save(product);
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
