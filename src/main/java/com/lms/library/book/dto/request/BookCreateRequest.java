package com.lms.library.book.dto.request;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data // lombok tự tạo getter, setter, toString, equals, hashCode...
public class BookCreateRequest {
    @NotBlank(message = "ISBN không được để trống")
    private String isbn;
    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;
    // → Tương tự, tên sách bắt buộc phải có và không được rỗng

    private String author;
    // → Tác giả: có thể để trống (không bắt buộc)

    private String category;
    // → Thể loại: cũng có thể để trống

    @NotNull
    @Min(value = 1, message = "Số lượng tổng phải lớn hơn 0")
    private Integer totalQuantity;

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }
}

