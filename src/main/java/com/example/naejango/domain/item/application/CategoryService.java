package com.example.naejango.domain.item.application;

import com.example.naejango.domain.item.dto.CategoryDto;
import com.example.naejango.domain.item.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public List<CategoryDto> findAllCategory() {
         return categoryRepository.findAll().stream().map(CategoryDto::new).collect(Collectors.toList());
    }
}
