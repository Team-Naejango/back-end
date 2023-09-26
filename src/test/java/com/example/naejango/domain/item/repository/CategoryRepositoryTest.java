package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class CategoryRepositoryTest {
    @Autowired
    CategoryRepository categoryRepository;

    @Nested
    @DisplayName("카테고리 전부 조회")
    class getCategoryTest {
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
