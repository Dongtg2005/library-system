
package com.lms.library.book.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BookUpdateRequest { 

    @NotBlank(message = "Tiêu đề không được để trống")
    private String title;

    private String author;
    
    private String category;
    
    // Chưa cho phép sửa số lượng (totalQuantity) ở đây. 
    // Việc nhập/xuất kho sẽ có một API riêng biệt để đảm bảo tính chính xác của kho.
}