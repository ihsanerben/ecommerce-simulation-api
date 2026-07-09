package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.ProductRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.ProductResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.entity.Product;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.CategoryMapper;
import com.ihsanerben.ecommerce_simulation_api.mapper.ProductMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import com.ihsanerben.ecommerce_simulation_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private ProductService productService;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, categoryRepository, new ProductMapper(new CategoryMapper()));
    }

    private Category sampleCategory() {
        return Category.builder().id(1L).name("Electronics").description("Gadgets").build();
    }

    private Product sampleProduct(Category category) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .id(10L)
                .name("Laptop")
                .description("A laptop")
                .price(new BigDecimal("999.99"))
                .stockQuantity(5)
                .category(category)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    @Test
    void searchProducts_returnsMappedPage() {
        Category category = sampleCategory();
        Product product = sampleProduct(category);
        Pageable pageable = PageRequest.of(0, 20);
        Page<Product> page = new PageImpl<>(List.of(product), pageable, 1);

        given(productRepository.findAll(any(Specification.class), any(Pageable.class))).willReturn(page);

        Page<ProductResponse> result = productService.searchProducts(null, null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("Laptop");
    }

    @Test
    void getProductById_whenExists_returnsProduct() {
        Category category = sampleCategory();
        Product product = sampleProduct(category);
        given(productRepository.findById(10L)).willReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(10L);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.category().name()).isEqualTo("Electronics");
    }

    @Test
    void getProductById_whenNotExists_throwsResourceNotFoundException() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createProduct_whenCategoryExists_savesAndReturnsProduct() {
        Category category = sampleCategory();
        ProductRequest request = new ProductRequest("Laptop", "A laptop", new BigDecimal("999.99"), 5, 1L);
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        ProductResponse response = productService.createProduct(request);

        assertThat(response.name()).isEqualTo("Laptop");
        assertThat(response.category().id()).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_whenCategoryNotExists_throwsResourceNotFoundException() {
        ProductRequest request = new ProductRequest("Laptop", "A laptop", new BigDecimal("999.99"), 5, 99L);
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_whenExists_updatesAndReturnsProduct() {
        Category category = sampleCategory();
        Product existing = sampleProduct(category);
        ProductRequest request = new ProductRequest("Laptop Pro", "Updated", new BigDecimal("1299.99"), 3, 1L);
        given(productRepository.findById(10L)).willReturn(Optional.of(existing));
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        ProductResponse response = productService.updateProduct(10L, request);

        assertThat(response.name()).isEqualTo("Laptop Pro");
        assertThat(response.price()).isEqualByComparingTo("1299.99");
        assertThat(response.stockQuantity()).isEqualTo(3);
    }

    @Test
    void updateProduct_whenProductNotExists_throwsResourceNotFoundException() {
        ProductRequest request = new ProductRequest("Laptop Pro", "Updated", new BigDecimal("1299.99"), 3, 1L);
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateProduct_whenCategoryNotExists_throwsResourceNotFoundException() {
        Category category = sampleCategory();
        Product existing = sampleProduct(category);
        ProductRequest request = new ProductRequest("Laptop Pro", "Updated", new BigDecimal("1299.99"), 3, 99L);
        given(productRepository.findById(10L)).willReturn(Optional.of(existing));
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.updateProduct(10L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteProduct_whenExists_deletesProduct() {
        Category category = sampleCategory();
        Product existing = sampleProduct(category);
        given(productRepository.findById(10L)).willReturn(Optional.of(existing));

        productService.deleteProduct(10L);

        verify(productRepository).delete(existing);
    }

    @Test
    void deleteProduct_whenNotExists_throwsResourceNotFoundException() {
        given(productRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> productService.deleteProduct(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).delete(any(Product.class));
    }
}
