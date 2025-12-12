package com.smarttodo.clickbook.repository;

import com.smarttodo.clickbook.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartRepository extends JpaRepository<CartItem, Long> {

    // ПОЛУЧЕНИЕ ВСЕХ ТОВАРОВ КОРЗИНЫ ПОЛЬЗОВАТЕЛЯ
    List<CartItem> findByUserId(Long userId);

    // ПОИСК КОНКРЕТНОГО ТОВАРА В КОРЗИНЕ ПОЛЬЗОВАТЕЛЯ
    @Query("SELECT c FROM CartItem c WHERE c.user.id = :userId AND c.book.id = :bookId AND c.bookType = :bookType")
    List<CartItem> findByUserIdAndBookIdAndBookType(@Param("userId") Long userId,
                                                    @Param("bookId") Long bookId,
                                                    @Param("bookType") String bookType);

    // ПОДСЧЕТ КОЛИЧЕСТВА ТОВАРОВ В КОРЗИНЕ
    @Query("SELECT COUNT(c) FROM CartItem c WHERE c.user.id = :userId")
    Integer countByUserId(@Param("userId") Long userId);

    // 🗑УДАЛЕНИЕ ВСЕХ ТОВАРОВ КОРЗИНЫ ПОЛЬЗОВАТЕЛЯ
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    // УДАЛЕНИЕ КОНКРЕТНОГО ТОВАРА ИЗ КОРЗИНЫ
    @Modifying
    @Query("DELETE FROM CartItem c WHERE c.user.id = :userId AND c.book.id = :bookId AND c.bookType = :bookType")
    void deleteByUserIdAndBookIdAndBookType(@Param("userId") Long userId,
                                            @Param("bookId") Long bookId,
                                            @Param("bookType") String bookType);

    // ПОЛУЧЕНИЕ ВСЕХ ТОВАРОВ КОРЗИНЫ С ЗАГРУЗКОЙ КНИГ (JOIN FETCH)
    @Query("SELECT c FROM CartItem c JOIN FETCH c.book WHERE c.user.id = :userId")
    List<CartItem> findByUserIdWithBooks(@Param("userId") Long userId);
}