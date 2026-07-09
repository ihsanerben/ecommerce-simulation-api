package com.ihsanerben.ecommerce_simulation_api.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsanerben.ecommerce_simulation_api.AbstractIntegrationTest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.AddCartItemRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.RegisterRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.request.UpdateOrderStatusRequest;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.OrderStatus;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.entity.Role;
import com.ihsanerben.ecommerce_simulation_api.repository.CartItemRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CartRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.OrderRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderControllerIT extends AbstractIntegrationTest {

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
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        orderRepository.deleteAll();
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
        return objectMapper.readTree(body).get("token").asText();
    }

    private void promoteToAdmin(String username) {
        var user = userRepository.findByUsername(username).orElseThrow();
        user.setRole(Role.ADMIN);
        userRepository.save(user);
    }

    private Product saveProduct(String name, String price, int stock) {
        Category category = categoryRepository.save(Category.builder().name(name + "-cat").description("desc").build());
        LocalDateTime now = LocalDateTime.now();
        return productRepository.save(Product.builder()
                .name(name)
                .description("desc")
                .price(new BigDecimal(price))
                .stockQuantity(stock)
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build());
    }

    private void addToCart(String token, Long productId, int quantity) throws Exception {
        mockMvc.perform(post("/api/cart/items")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AddCartItemRequest(productId, quantity))))
                .andExpect(status().isCreated());
    }

    @Test
    void checkout_withoutAuthentication_isForbidden() throws Exception {
        mockMvc.perform(post("/api/orders"))
                .andExpect(status().isForbidden());
    }

    @Test
    void checkout_whenCartIsEmpty_returns400() throws Exception {
        String token = registerAndGetToken("ihsan");

        mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkout_whenValid_createsOrderAndDecrementsStockAndClearsCart() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product mouse = saveProduct("Mouse", "20.00", 100);
        Product laptop = saveProduct("Laptop", "1000.00", 5);
        addToCart(token, mouse.getId(), 2);
        addToCart(token, laptop.getId(), 1);

        mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(1040.0))
                .andExpect(jsonPath("$.items.length()").value(2));

        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isEqualTo(98);
        assertThat(productRepository.findById(laptop.getId()).orElseThrow().getStockQuantity()).isEqualTo(4);

        mockMvc.perform(get("/api/cart").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.items").isEmpty());
    }

    @Test
    void checkout_whenOneItemHasInsufficientStock_returns409AndPersistsNothing() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product mouse = saveProduct("Mouse", "20.00", 100);
        Product laptop = saveProduct("Laptop", "1000.00", 5);
        addToCart(token, mouse.getId(), 2);
        addToCart(token, laptop.getId(), 5);

        // Simulate stock being depleted (e.g. another order) AFTER the item was added to
        // this cart but BEFORE checkout — the realistic way insufficient stock surfaces here,
        // since CartService already blocks adding more than the stock at add-time.
        laptop.setStockQuantity(1);
        productRepository.save(laptop);

        mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());

        // All-or-nothing: "Mouse" alone had enough stock, but nothing should have been
        // persisted or mutated because "Laptop" in the same cart did not.
        assertThat(orderRepository.count()).isZero();
        assertThat(productRepository.findById(mouse.getId()).orElseThrow().getStockQuantity()).isEqualTo(100);
        assertThat(productRepository.findById(laptop.getId()).orElseThrow().getStockQuantity()).isEqualTo(1);
        assertThat(cartItemRepository.count()).isEqualTo(2);
    }

    @Test
    void getOrders_returnsOnlyOwnOrders() throws Exception {
        String tokenA = registerAndGetToken("userA");
        String tokenB = registerAndGetToken("userB");
        Product product = saveProduct("Mouse", "20.00", 100);

        addToCart(tokenA, product.getId(), 1);
        mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/orders").header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOrderById_whenNotOwner_returns404() throws Exception {
        String tokenA = registerAndGetToken("userA");
        String tokenB = registerAndGetToken("userB");
        Product product = saveProduct("Mouse", "20.00", 100);
        addToCart(tokenA, product.getId(), 1);
        String orderBody = mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + tokenA))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(orderBody).get("id").asLong();

        mockMvc.perform(get("/api/orders/{id}", orderId).header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_asAdmin_returns200() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Mouse", "20.00", 100);
        addToCart(token, product.getId(), 1);
        String orderBody = mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(orderBody).get("id").asLong();

        promoteToAdmin("ihsan");
        String adminToken = loginAndGetToken("ihsan");

        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(
                                OrderStatus.SHIPPED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SHIPPED"));
    }

    @Test
    void updateStatus_asRegularUser_isForbidden() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Mouse", "20.00", 100);
        addToCart(token, product.getId(), 1);
        String orderBody = mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(orderBody).get("id").asLong();

        mockMvc.perform(put("/api/orders/{id}/status", orderId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new UpdateOrderStatusRequest(
                                OrderStatus.SHIPPED))))
                .andExpect(status().isForbidden());
    }

    @Test
    void cancelOrder_asOwner_restoresStock() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Mouse", "20.00", 100);
        addToCart(token, product.getId(), 3);
        String orderBody = mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(orderBody).get("id").asLong();
        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(97);

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(100);
    }

    @Test
    void cancelOrder_whenAlreadyCancelled_returns409() throws Exception {
        String token = registerAndGetToken("ihsan");
        Product product = saveProduct("Mouse", "20.00", 100);
        addToCart(token, product.getId(), 1);
        String orderBody = mockMvc.perform(post("/api/orders").header("Authorization", "Bearer " + token))
                .andReturn().getResponse().getContentAsString();
        Long orderId = objectMapper.readTree(orderBody).get("id").asLong();

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId).header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/orders/{id}/cancel", orderId).header("Authorization", "Bearer " + token))
                .andExpect(status().isConflict());
    }

    private String loginAndGetToken(String username) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"%s\",\"password\":\"password123\"}".formatted(username)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }
}
