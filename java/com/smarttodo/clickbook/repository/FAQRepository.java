package com.smarttodo.clickbook.repository;

import com.smarttodo.clickbook.entity.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ, Long> {
    List<FAQ> findByCategoryOrderByDisplayOrderAsc(String category);

    @Query("SELECT DISTINCT f.category FROM FAQ f ORDER BY f.category")
    List<String> findDistinctCategories();

    List<FAQ> findAllByOrderByDisplayOrderAsc();
}