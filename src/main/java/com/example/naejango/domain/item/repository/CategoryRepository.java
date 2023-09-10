package com.example.naejango.domain.item.repository;

import com.example.naejango.domain.item.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {

    Category findByName(String name);

}
