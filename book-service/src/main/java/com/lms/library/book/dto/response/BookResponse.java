package com.lms.library.book.dto.response;

import lombok.Data;
import java.util.UUID;

@Data
public class BookResponse { // Nhớ là luôn phải có chữ "class" nhé!
    
    // ID này database tự sinh ra, giờ mình trả về để Front-end biết 
    // mốt còn dùng ID này để xóa hoặc sửa sách.
    private UUID id; 
    
    private String isbn;
    private String title;
    private String author;
    private String category;
    
    // Trả về số lượng thực tế đang còn trên kệ
    private Integer availableQty; 
    
    // Chú ý: Tại sao chỗ này là String mà không phải Enum (BookStatus)?
    // Vì trả ra chuỗi chữ "AVAILABLE" thì Front-end (React/Flutter) 
    // sẽ dễ đọc và hiển thị lên màn hình hơn là ném nguyên một cái Object Enum cho họ.
    private String status; 
}