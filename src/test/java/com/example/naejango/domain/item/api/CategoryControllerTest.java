package com.example.naejango.domain.item.api;

import com.epages.restdocs.apispec.ResourceSnippetParameters;
import com.epages.restdocs.apispec.Schema;
import com.example.naejango.domain.config.RestDocsSupportTest;
import com.example.naejango.domain.item.application.CategoryService;
import com.example.naejango.domain.item.domain.Category;
import com.example.naejango.domain.item.dto.CategoryDto;
import org.junit.jupiter.api.*;
import org.mockito.BDDMockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.List;
import java.util.stream.Collectors;

import static com.epages.restdocs.apispec.ResourceDocumentation.resource;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;

@WebMvcTest(CategoryController.class)
@TestClassOrder(ClassOrderer.OrderAnnotation.class)
class CategoryControllerTest extends RestDocsSupportTest {

    @MockBean
    CategoryService categoryService;

    @Nested
    @Order(1)
    @DisplayName("모든 카테고리 조회")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class getCategory {
        Category cat1 = new Category(1, "의류");
        Category cat2 = new Category(2, "생필품");
        Category cat3 = new Category(3, "디지털기기");
        List<Category> result = List.of(cat1, cat2, cat3);
        List<CategoryDto> resultDto = result.stream().map(CategoryDto::new).collect(Collectors.toList());

        @Test
        @Tag("api")
        @DisplayName("성공")
        void test1() throws Exception {
            // given
            BDDMockito.given(categoryService.findAllCategory()).willReturn(resultDto);

            // when
            ResultActions resultActions = mockMvc.perform(RestDocumentationRequestBuilders
                    .get("/api/category")
                    .with(SecurityMockMvcRequestPostProcessors.csrf())
            );

            // then
            resultActions.andExpect(MockMvcResultMatchers.status().isOk());

            // restDoc
            resultActions.andDo(restDocs.document(resource(ResourceSnippetParameters.builder()
                    .tag("카테고리")
                    .summary("카테고리 조회")
                    .description("저장되어 있는 카테고리를 전부 조회합니다.")
                    .responseFields(
                            fieldWithPath("message").description("결과 메세지"),
                            fieldWithPath("result[].categoryId").description("카테고리 아이디"),
                            fieldWithPath("result[].categoryName").description("카테고리 이름"))
                    .requestSchema(
                            Schema.schema("카테고리 조회 Response"))
                    .build()))
            );
        }
    }
}