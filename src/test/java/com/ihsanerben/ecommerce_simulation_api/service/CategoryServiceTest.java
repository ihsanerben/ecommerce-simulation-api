package com.ihsanerben.ecommerce_simulation_api.service;

import com.ihsanerben.ecommerce_simulation_api.dto.request.CategoryRequest;
import com.ihsanerben.ecommerce_simulation_api.dto.response.CategoryResponse;
import com.ihsanerben.ecommerce_simulation_api.entity.Category;
import com.ihsanerben.ecommerce_simulation_api.exception.DuplicateResourceException;
import com.ihsanerben.ecommerce_simulation_api.exception.ResourceNotFoundException;
import com.ihsanerben.ecommerce_simulation_api.mapper.CategoryMapper;
import com.ihsanerben.ecommerce_simulation_api.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, new CategoryMapper());
    }

    @Test
    void getAllCategories_returnsMappedList() {
        Category category = Category.builder().id(1L).name("Electronics").description("Gadgets").build();
        given(categoryRepository.findAll()).willReturn(List.of(category));

        List<CategoryResponse> result = categoryService.getAllCategories();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Electronics");
    }

    @Test
    void getCategoryById_whenExists_returnsCategory() {
        Category category = Category.builder().id(1L).name("Electronics").description("Gadgets").build();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(category));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Electronics");
    }

    @Test
    void getCategoryById_whenNotExists_throwsResourceNotFoundException() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.getCategoryById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createCategory_whenNameIsUnique_savesAndReturnsCategory() {
        CategoryRequest request = new CategoryRequest("Electronics", "Gadgets");
        given(categoryRepository.existsByName("Electronics")).willReturn(false);

        CategoryResponse response = categoryService.createCategory(request);

        assertThat(response.name()).isEqualTo("Electronics");
        assertThat(response.description()).isEqualTo("Gadgets");
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_whenNameAlreadyTaken_throwsDuplicateResourceException() {
        CategoryRequest request = new CategoryRequest("Electronics", "Gadgets");
        given(categoryRepository.existsByName("Electronics")).willReturn(true);

        assertThatThrownBy(() -> categoryService.createCategory(request))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("name");

        verify(categoryRepository, never()).save(any());
    }

    @Test
    void updateCategory_whenExists_updatesAndReturnsCategory() {
        Category existing = Category.builder().id(1L).name("Old Name").description("Old Description").build();
        CategoryRequest request = new CategoryRequest("New Name", "New Description");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.existsByName("New Name")).willReturn(false);

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertThat(response.name()).isEqualTo("New Name");
        assertThat(response.description()).isEqualTo("New Description");
    }

    @Test
    void updateCategory_whenNotExists_throwsResourceNotFoundException() {
        CategoryRequest request = new CategoryRequest("New Name", "New Description");
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCategory_whenNewNameConflictsWithAnotherCategory_throwsDuplicateResourceException() {
        Category existing = Category.builder().id(1L).name("Old Name").description("Old Description").build();
        CategoryRequest request = new CategoryRequest("Taken Name", "New Description");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));
        given(categoryRepository.existsByName("Taken Name")).willReturn(true);

        assertThatThrownBy(() -> categoryService.updateCategory(1L, request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void updateCategory_whenNameUnchanged_doesNotCheckForDuplicate() {
        Category existing = Category.builder().id(1L).name("Same Name").description("Old Description").build();
        CategoryRequest request = new CategoryRequest("Same Name", "New Description");
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));

        CategoryResponse response = categoryService.updateCategory(1L, request);

        assertThat(response.description()).isEqualTo("New Description");
        verify(categoryRepository, never()).existsByName(any());
    }

    @Test
    void deleteCategory_whenExists_deletesCategory() {
        Category existing = Category.builder().id(1L).name("Electronics").description("Gadgets").build();
        given(categoryRepository.findById(1L)).willReturn(Optional.of(existing));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).delete(existing);
    }

    @Test
    void deleteCategory_whenNotExists_throwsResourceNotFoundException() {
        given(categoryRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(categoryRepository, never()).delete(any());
    }
}
