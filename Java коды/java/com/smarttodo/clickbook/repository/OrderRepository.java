package com.smarttodo.clickbook.repository;

import com.smarttodo.clickbook.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByUserUsernameOrderByCreatedAtDesc(String username);

    // Получение всех заказов с сортировкой по дате (новые сверху)
    List<Order> findAllByOrderByCreatedAtDesc();

    // Поиск заказов по статусу
    List<Order> findByStatusOrderByCreatedAtDesc(String status);

    // Поиск заказов по городу доставки
    @Query("SELECT o FROM Order o WHERE o.deliveryCity LIKE %:city% ORDER BY o.createdAt DESC")
    List<Order> findByDeliveryCityContaining(@Param("city") String city);

    // Поиск заказов по службе доставки
    List<Order> findByDeliveryServiceOrderByCreatedAtDesc(String deliveryService);

    // Количество заказов пользователя
    @Query("SELECT COUNT(o) FROM Order o WHERE o.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    // Сумма всех заказов пользователя
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o WHERE o.user.id = :userId")
    double sumTotalAmountByUserId(@Param("userId") Long userId);
}