package com.smarttodo.clickbook.repository;

import com.smarttodo.clickbook.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    @Query(value = "SELECT TOP 6 * FROM books ORDER BY created_at DESC", nativeQuery = true)
    List<Book> findTop6ByOrderByCreatedAtDesc();

    @Query("SELECT b FROM Book b WHERE b.category.name = :categoryName AND b.id != :excludeId")
    List<Book> findSimilarBooks(@Param("categoryName") String categoryName,
                                @Param("excludeId") Long excludeId);

    List<Book> findByAuthorAndIdNot(String author, Long excludeId);

    @Query("SELECT b FROM Book b WHERE b.ageRating IN ('0+', '6+', '12+')")
    List<Book> findBooksForMinors();

    @Query("SELECT b FROM Book b WHERE b.ageRating = '18+'")
    List<Book> findAdultBooks();

    // Новый метод для поиска по нескольким критериям
    @Query("SELECT b FROM Book b WHERE " +
            "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
            "(:minPrice IS NULL OR b.priceDigital >= :minPrice) AND " +
            "(:maxPrice IS NULL OR b.priceDigital <= :maxPrice)")
    List<Book> searchBooks(@Param("title") String title,
                           @Param("author") String author,
                           @Param("minPrice") Double minPrice,
                           @Param("maxPrice") Double maxPrice);

    // ПОИСК КНИГ ПО НАЗВАНИЮ И АВТОРУ
    @Query("SELECT b FROM Book b WHERE " +
            "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "(b.category IS NOT NULL AND LOWER(b.category.name) LIKE LOWER(CONCAT('%', :query, '%'))) OR " +
            "LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Book> searchBooks(@Param("query") String query);

    // Поиск по точному совпадению (для быстрого поиска)
    List<Book> findByTitleContainingIgnoreCase(String title);
    List<Book> findByAuthorContainingIgnoreCase(String author);
}