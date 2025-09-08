package com.warsha.erp.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.warsha.erp.entities.Product;
import com.warsha.erp.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/products")
@CrossOrigin
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public List<Product> getAll() {
        return productService.getAllProducts();
    }

    @GetMapping("/getImage")
    public ResponseEntity<Resource> getProductImage(@RequestParam String filename) {
        try {
            Resource resource = productService.getProductImage(filename);

            if (resource == null) {
                return ResponseEntity.notFound().build();
            }

            // Optional: dynamically detect MIME type
            String contentType = "application/octet-stream";
            try {
                contentType = Files.probeContentType(Paths.get(filename));
            } catch (IOException ex) {
                // fallback to octet-stream
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @PostMapping(consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<Product> create(
            @RequestPart("product") String productJson,
            @RequestPart("image") MultipartFile image) throws JsonProcessingException {

        // Convert JSON string to ProductModel using Jackson
        Product product = new ObjectMapper().readValue(productJson, Product.class);

        return new ResponseEntity<>(productService.createProduct(product, image), HttpStatus.CREATED);
    }

    @PutMapping(value = "/{id}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public Product updateProduct(
            @PathVariable Long id,
            @RequestPart("product") String product,
            @RequestPart(value = "image", required = false) MultipartFile image) throws JsonProcessingException {

        Product productConverted = new ObjectMapper().readValue(product, Product.class);

        return productService.updateProductWithImage(id, productConverted, image);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAll() {
        productService.deleteAll();
        return ResponseEntity.noContent().build();
    }
}
