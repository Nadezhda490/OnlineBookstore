package com.smarttodo.clickbook.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer quantity = 1;

    @Column(name = "book_type", nullable = false)
    private String bookType = "DIGITAL";

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    // Конструкторы
    public CartItem() {}

    public CartItem(User user, Book book, String bookType) {
        this.user = user;
        this.book = book;
        this.bookType = bookType;
    }

    // Геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Book getBook() { return book; }
    public void setBook(Book book) { this.book = book; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getBookType() { return bookType; }
    public void setBookType(String bookType) { this.bookType = bookType; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "id=" + id +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", quantity=" + quantity +
                ", bookType='" + bookType + '\'' +
                '}';
    }
}