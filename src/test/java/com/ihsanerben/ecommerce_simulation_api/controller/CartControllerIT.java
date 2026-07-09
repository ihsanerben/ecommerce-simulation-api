package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.AddCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.UpdateCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.repository.CartItemRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CartControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    private String registerAndGetToken(String username) throws Exception {
        RegisterRequest request = new RegisterRequest(username, username + "@example.com", "password123");
        String body = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode json = objectMapper.readTree(body);
        return json.get("token").asText();
    }

    private Product saveProduct(String name, int stock) {
        Category category = categoryRepository.save(Category.builder().name(name + "-cat").description("desc").build());
        LocalDateTime now = LocalDateTime.now();
        return productRepository.save(Product.builder()
                .name(name)
                .description("desc")
                .price(new BigDecimal("100.00"))
                .stockQuantity(stock)
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    @Test
    void getCart_withoutAuthentication_isForbidden() throws Exception {
        mockMvc.perform(get("/api/cart"))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCart_whenNoItemsYet_returnsEmptyCart() throws Exception {
        String token = registerAndGetToken("ihsan");

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty())
                .andExpect(jsonPath("$.totalPrice").value(0));
    }

    @Test
    void addItem_withSufficientStock_returns201() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Laptop", 10);
        AddCartItemRequest request = new AddCartItemRequest(product.getId(), 2);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.totalPrice").value(200.0));
    }

    @Test
    void addItem_withInsufficientStock_returns409() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Laptop", 1);
        AddCartItemRequest request = new AddCartItemRequest(product.getId(), 5);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void addItem_whenProductNotExists_returns404() throws Exception {
        String token = registerAndGetToken("ihsan");
        AddCartItemRequest request = new AddCartItemRequest(999L, 1);

        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateItemQuantity_asOwner_returns200() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Laptop", 10);
        String addResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(product.getId(), 2))))
                .andReturn().getResponse().getContentAsString();
        Long itemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCartItemRequest(5))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(5));
    }

    @Test
    void updateItemQuantity_asDifferentUser_returns404() throws Exception {
        String ownerToken = registerAndGetToken("owner");
        String intruderToken = registerAndGetToken("intruder");
        Product product = saveProduct("Laptop", 10);
        String addResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(product.getId(), 2))))
                .andReturn().getResponse().getContentAsString();
        Long itemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        mockMvc.perform(put("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer " + intruderToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateCartItemRequest(5))))
                .andExpect(status().isNotFound());
    }

    @Test
    void removeItem_asOwner_returns204() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Laptop", 10);
        String addResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(product.getId(), 2))))
                .andReturn().getResponse().getContentAsString();
        Long itemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());
    }

    @Test
    void removeItem_asDifferentUser_returns404() throws Exception {
        String ownerToken = registerAndGetToken("owner");
        String intruderToken = registerAndGetToken("intruder");
        Product product = saveProduct("Laptop", 10);
        String addResponse = mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(product.getId(), 2))))
                .andReturn().getResponse().getContentAsString();
        Long itemId = objectMapper.readTree(addResponse).get("items").get(0).get("id").asLong();

        mockMvc.perform(delete("/api/cart/items/{itemId}", itemId)
                        .header("Authorization", "Bearer " + intruderToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void clearCart_removesAllItems() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Laptop", 10);
        mockMvc.perform(post("/api/cart/items")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new AddCartItemRequest(product.getId(), 2))));

        mockMvc.perform(delete("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isEmpty());
    }
}
