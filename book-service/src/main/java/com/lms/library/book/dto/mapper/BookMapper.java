package com.lms.library.book.dto.mapper;

import com.lms.library.book.dto.request.BookCreateRequest;
import com.lms.library.book.dto.response.BookResponse;
import com.lms.library.book.entity.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

// @Mapper(componentModel = "spring") báo cho Spring Boot biết: 
// "Hãy coi cái interface này như một Bean (nhân viên), khi nào tôi cần thì gọi nó ra làm việc"
@Mapper(componentModel = "spring")
public interface BookMapper {

    // 1. Chuyển từ Request (khách gửi) -> Entity (chuẩn bị cất vào kho)
    // - Bỏ qua trường 'id' vì database sẽ tự sinh.
    // - Lấy 'totalQuantity' (tổng số sách) gán vào 'availableQty' (vì sách mới mua về thì Tổng = Đang có trên kệ).
    // - Gán cứng trạng thái là "AVAILABLE".
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "availableQty", source = "totalQuantity")
    @Mapping(target = "status", constant = "AVAILABLE")
    Book toEntity(BookCreateRequest request);

    // 2. Chuyển từ Entity (vừa lấy từ kho ra) -> Response (trả về cho khách xem)
    BookResponse toResponse(Book entity);
}