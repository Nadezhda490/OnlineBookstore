package com.smarttodo.clickbook.entity;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private String genre;

    @Column(name = "age_rating")
    private String ageRating = "0+";

    @Column(name = "price_digital", nullable = false)
    private Double priceDigital;

    @Column(name = "price_printed", nullable = false)
    private Double pricePrinted;

    private Integer pages;

    @Column(name = "image_url")
    private String imageUrl = "/images/books/default.jpg"; // Дефолтное значение

    // конструкторы
    public Book() {}

    public Book(String title, String author, String description, Double priceDigital, Double pricePrinted) {
        this.title = title;
        this.author = author;
        this.description = description;
        this.priceDigital = priceDigital;
        this.pricePrinted = pricePrinted;
        this.imageUrl = "/images/books/default.jpg"; // Устанавливаем дефолтное изображение
    }

    // геттеры и сеттеры
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public String getGenre() { return genre; }
    public void setGenre(String genre) { this.genre = genre; }

    public String getAgeRating() { return ageRating; }
    public void setAgeRating(String ageRating) { this.ageRating = ageRating; }

    public Double getPriceDigital() { return priceDigital; }
    public void setPriceDigital(Double priceDigital) { this.priceDigital = priceDigital; }

    public Double getPricePrinted() { return pricePrinted; }
    public void setPricePrinted(Double pricePrinted) { this.pricePrinted = pricePrinted; }

    public Integer getPages() { return pages; }
    public void setPages(Integer pages) { this.pages = pages; }

    public String getImageUrl() {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            return "/images/books/default.jpg";
        }
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    // equals и hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id) &&
                Objects.equals(title, book.title) &&
                Objects.equals(author, book.author);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, author);
    }

    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", priceDigital=" + priceDigital +
                ", pricePrinted=" + pricePrinted +
                '}';
    }
}