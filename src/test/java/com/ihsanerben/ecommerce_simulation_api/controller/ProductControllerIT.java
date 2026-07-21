package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.ProductRequest;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
    }

    private Category saveCategory(String name) {
        return categoryRepository.save(Category.builder().name(name).description("desc").build());
    }

    private Product saveProduct(String name, Category category) {
        LocalDateTime now = LocalDateTime.now();
        return productRepository.save(Product.builder()
                .name(name)
                .description("desc")
                .price(new BigDecimal("100.00"))
                .stockQuantity(10)
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    @Test
    void getProducts_isPubliclyAccessibleAndPaged() throws Exception {
        Category category = saveCategory("Electronics");
        saveProduct("Laptop", category);

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Laptop"))
                .andExpect(jsonPath("$.page.totalElements").value(1));
    }

    @Test
    void getProducts_filterByCategoryId_returnsOnlyMatching() throws Exception {
        Category electronics = saveCategory("Electronics");
        Category books = saveCategory("Books");
        saveProduct("Laptop", electronics);
        saveProduct("Novel", books);

        mockMvc.perform(get("/api/products").param("categoryId", electronics.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Laptop"));
    }

    @Test
    void getProducts_filterBySearch_returnsOnlyMatching() throws Exception {
        Category category = saveCategory("Electronics");
        saveProduct("Gaming Laptop", category);
        saveProduct("Desktop PC", category);

        mockMvc.perform(get("/api/products").param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].name").value("Gaming Laptop"));
    }

    @Test
    void getProducts_withInvalidSortProperty_returns400InsteadOf500() throws Exception {
        mockMvc.perform(get("/api/products").param("sort", "unknownField,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid sort property 'unknownField'."));
    }

    @Test
    void getProductById_whenNotExists_returns404() throws Exception {
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_asAdmin_returns201() throws Exception {
        Category category = saveCategory("Electronics");
        ProductRequest request = new ProductRequest("Laptop", "A laptop", new BigDecimal("999.99"), 5, category.getId());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Laptop"))
                .andExpect(jsonPath("$.category.name").value("Electronics"));
    }

    @Test
    void createProduct_withoutAuthentication_isUnauthorized() throws Exception {
        Category category = saveCategory("Electronics");
        ProductRequest request = new ProductRequest("Laptop", "A laptop", new BigDecimal("999.99"), 5, category.getId());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_withNegativePrice_returns400() throws Exception {
        Category category = saveCategory("Electronics");
        ProductRequest request = new ProductRequest("Laptop", "A laptop", new BigDecimal("-1"), 5, category.getId());

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.price").exists());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createProduct_whenCategoryNotExists_returns404() throws Exception {
        ProductRequest request = new ProductRequest("Laptop", "A laptop", new BigDecimal("999.99"), 5, 999L);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateProduct_asAdmin_returns200() throws Exception {
        Category category = saveCategory("Electronics");
        Product product = saveProduct("Laptop", category);
        ProductRequest request = new ProductRequest("Laptop Pro", "Updated", new BigDecimal("1299.99"), 3, category.getId());

        mockMvc.perform(put("/api/products/{id}", product.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop Pro"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_asAdmin_returns204() throws Exception {
        Category category = saveCategory("Electronics");
        Product product = saveProduct("Laptop", category);

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteProduct_asRegularUser_isForbidden() throws Exception {
        Category category = saveCategory("Electronics");
        Product product = saveProduct("Laptop", category);

        mockMvc.perform(delete("/api/products/{id}", product.getId()))
                .andExpect(status().isForbidden());
    }
}
