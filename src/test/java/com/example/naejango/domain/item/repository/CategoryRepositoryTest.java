package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@ActiveProfiles("test")
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Nested
    @DisplayName("카테고리 전부 조회")
    class getCategoryTest {
        @BeforeEach
        @Transactional
        void setup(){
            categoryRepository.save(Category.builder().name("생필품").build());
            categoryRepository.save(Category.builder().name("의류").build());
            categoryRepository.save(Category.builder().name("도서").build());
            categoryRepository.save(Category.builder().name("디지털기기").build());
            categoryRepository.save(Category.builder().name("생활가전").build());
            categoryRepository.save(Category.builder().name("뷰티").build());
        }
        @Test
        @DisplayName("성공")
        void test1(){
            // given

            // when
            List<Category> all = categoryRepository.findAll();

            // then
            Assertions.assertFalse(all.isEmpty());
        }
    }

}
