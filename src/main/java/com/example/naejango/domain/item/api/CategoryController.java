package com.example.naejango.domain.item.api;

import com.example.naejango.domain.common.CommonResponseDto;
import com.example.naejango.domain.item.application.CategoryService;
import com.example.naejango.domain.item.dto.CategoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/api/category")
@RestController
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    /** 카테고리 조회 */
    @GetMapping("")
    public ResponseEntity<CommonResponseDto<List<CategoryDto>>> getCategory() {
        List<CategoryDto> result = categoryService.findAllCategory();
        return ResponseEntity.ok().body(new CommonResponseDto<>("조회 성공", result));
    }
}
