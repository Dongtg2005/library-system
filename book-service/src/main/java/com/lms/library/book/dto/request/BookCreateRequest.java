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
    // → Số lượng tổng: 
    //   - Bắt buộc phải có (không được null)
    //   - Phải ≥ 1 (không chấp nhận 0 hoặc số âm)
}

