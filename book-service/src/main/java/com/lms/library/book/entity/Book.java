package com.lms.library.book.entity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
@Entity // Định nghĩa một bảng trong database
@Table(name = "books") // Đặt tên bảng là "books"
@Getter
@Setter
@NoArgsConstructor // Tạo constructor không tham số
@AllArgsConstructor // Tạo constructor với tất cả tham số
@Builder
public class Book { 
    @Id                                   // Đây là khóa chính (định danh duy nhất)
    @GeneratedValue(strategy = GenerationType.UUID)   // Tự động tạo mã ngẫu nhiên kiểu UUID (dài và rất khó trùng)
    private UUID id;                      // Mã định danh của sách (ví dụ: 550e8400-e29b-41d4-a716-446655440000)

    @Column(unique = true, nullable = false, length = 20)
    private String isbn;                  // Mã ISBN của sách (mỗi sách có 1 mã duy nhất, giống số CMND của sách)

    @Column(nullable = false)             // Bắt buộc phải có
    private String title;                 // Tên sách (ví dụ: "Nhà giả kim", "Clean Code")

    @Column(length = 150)                 // Tối đa 150 ký tự
    private String author;                // Tác giả (có thể để trống)

    @Column(length = 100)
    private String category;              // Thể loại (Văn học, Công nghệ, Kinh doanh...)

    @Column(name = "total_quantity", nullable = false)
    private Integer totalQuantity;        // Tổng số bản sách có trong thư viện (ví dụ: 10 cuốn)

    @Column(name = "available_qty", nullable = false)
    private Integer availableQty;         // Số cuốn còn lại để mượn được (ví dụ: 3 cuốn đang có sẵn)

    @Enumerated(EnumType.STRING)          // Lưu dưới dạng chữ thay vì số
    @Column(length = 20)
    private BookStatus status;            // Trạng thái hiện tại của sách

    // Danh sách các trạng thái có thể có
    public enum BookStatus {
        AVAILABLE,      // Còn sách để mượn
        OUT_OF_STOCK,   // Hết sách
        ARCHIVED        // Đã lưu trữ, không cho mượn nữa (có thể là sách cũ, hỏng...)
    }
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
