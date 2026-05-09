package com.lms.library.application.service;

import com.lms.library.domain.entity.BorrowRecord;
import com.lms.library.domain.entity.Book;
import com.lms.library.domain.entity.User;
import com.lms.library.domain.entity.UserProfile;
import com.lms.library.domain.repository.BookRepository;
import com.lms.library.domain.repository.BorrowRecordRepository;
import com.lms.library.domain.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatPromptBuilder {

    private final BorrowRecordRepository borrowRecordRepository;
    private final BookRepository bookRepository;
    private final UserProfileRepository userProfileRepository;

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("5000");

    public String buildSystemPrompt(User user, String userRole) {
        StringBuilder sb = new StringBuilder();

        sb.append("Bạn là trợ lý AI của Hệ thống Quản lý Thư viện.\n");
        sb.append("Nhiệm vụ: Hỗ trợ độc giả và thủ thư một cách chính xác, thân thiện và chuyên nghiệp.\n\n");

        // ─── THÔNG TIN NGƯỜI DÙNG ───
        sb.append("═══════════════════════════════════════\n");
        sb.append("THÔNG TIN NGƯỜI DÙNG HIỆN TẠI\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append("Tên: ").append(user.getFullName()).append("\n");
        sb.append("Vai trò: ").append(mapRole(userRole)).append("\n\n");

        // ─── DỮ LIỆU THỰC TẾ ───
        sb.append("═══════════════════════════════════════\n");
        sb.append("DỮ LIỆU THỰC TẾ TỪ HỆ THỐNG\n");
        sb.append("═══════════════════════════════════════\n");

        boolean isReader = "USER".equalsIgnoreCase(userRole);
        boolean isLibrarian = "LIBRARIAN".equalsIgnoreCase(userRole) || "ADMIN".equalsIgnoreCase(userRole);

        if (isReader) {
            buildReaderData(sb, user);
        }
        if (isLibrarian) {
            buildLibrarianData(sb);
        }

        // ─── QUY TẮC XỬ LÝ ───
        sb.append("\n═══════════════════════════════════════\n");
        sb.append("QUY TẮC XỬ LÝ\n");
        sb.append("═══════════════════════════════════════\n");
        sb.append("1. Hỏi về tài khoản/mượn/phạt → Trả lời dựa vào dữ liệu thực tế ở trên.\n");
        sb.append("2. Hỏi về nội dung sách/gợi ý sách → Trả lời bằng kiến thức của bạn, hỏi kiểm tra tồn kho.\n");
        sb.append("3. Hỏi ngoài phạm vi thư viện → Từ chối nhẹ nhàng, hướng về chủ đề thư viện.\n");
        sb.append("4. Gợi ý sách: Hỏi trình độ nếu chưa rõ. Gợi ý theo lộ trình có thứ tự.\n");
        sb.append("5. Sau khi gợi ý → hỏi 'Bạn muốn mình kiểm tra cuốn nào còn sách không?'\n");
        sb.append("6. Nếu không có dữ liệu → Nói 'Bạn vui lòng kiểm tra trực tiếp trên hệ thống'.\n\n");

        // ─── PHONG CÁCH ───
        sb.append("PHONG CÁCH:\n");
        if (isReader) {
            sb.append("- Xưng 'mình', gọi người dùng là 'bạn'. Thân thiện, ngắn gọn, dùng tiếng Việt.\n");
        } else {
            sb.append("- Xưng 'tôi', gọi 'anh/chị'. Chuyên nghiệp, súc tích.\n");
        }
        sb.append("- Dùng emoji phù hợp (📗📕⚠️✅) để làm nổi bật thông tin quan trọng.\n");
        sb.append("- KHÔNG bịa dữ liệu. Ngày hôm nay là: ").append(LocalDate.now().format(DATE_FMT)).append(".\n");

        return sb.toString();
    }

    private void buildReaderData(StringBuilder sb, User user) {
        List<BorrowRecord> activeBorrows = borrowRecordRepository.findByMemberId(user.getId())
                .stream()
                .filter(r -> r.getBorrowStatus() == BorrowRecord.BorrowStatus.ACTIVE
                        || r.getBorrowStatus() == BorrowRecord.BorrowStatus.OVERDUE)
                .toList();

        sb.append("[SÁCH ĐANG MƯỢN]\n");
        if (activeBorrows.isEmpty()) {
            sb.append("Hiện tại bạn không có sách nào đang mượn.\n");
        } else {
            BigDecimal totalFine = BigDecimal.ZERO;
            for (BorrowRecord r : activeBorrows) {
                String title = getBookTitle(r.getBookId());
                LocalDate due = r.getDueDate();
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), due);

                if (daysLeft >= 0) {
                    sb.append("📗 \"").append(title).append("\" – hạn trả: ")
                            .append(due.format(DATE_FMT))
                            .append(" (còn ").append(daysLeft).append(" ngày)\n");
                } else {
                    long overdue = Math.abs(daysLeft);
                    BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(overdue));
                    totalFine = totalFine.add(fine);
                    sb.append("📕 \"").append(title).append("\" – hạn trả: ")
                            .append(due.format(DATE_FMT))
                            .append(" (⚠️ QUÁ HẠN ").append(overdue).append(" ngày, phạt: ")
                            .append(String.format("%,.0f", fine)).append("đ)\n");
                }
            }

            // Lấy tổng phạt từ user_profiles
            Optional<UserProfile> profile = userProfileRepository.findByUserId(user.getId());
            BigDecimal outstanding = profile.map(UserProfile::getOutstandingFines)
                    .orElse(BigDecimal.ZERO);

            sb.append("\n[TIỀN PHẠT CHƯA THANH TOÁN]\n");
            sb.append("Tổng tiền phạt: ").append(String.format("%,.0f", outstanding)).append("đ\n");
        }
    }

    private void buildLibrarianData(StringBuilder sb) {
        List<BorrowRecord> allOverdue = borrowRecordRepository.findAll()
                .stream()
                .filter(r -> r.getBorrowStatus() == BorrowRecord.BorrowStatus.OVERDUE)
                .toList();

        sb.append("[SÁCH QUÁ HẠN TOÀN THƯ VIỆN]\n");
        if (allOverdue.isEmpty()) {
            sb.append("Hiện không có phiếu mượn nào quá hạn.\n");
        } else {
            BigDecimal total = BigDecimal.ZERO;
            for (BorrowRecord r : allOverdue) {
                long days = Math.abs(ChronoUnit.DAYS.between(LocalDate.now(), r.getDueDate()));
                BigDecimal fine = FINE_PER_DAY.multiply(BigDecimal.valueOf(days));
                total = total.add(fine);
            }
            sb.append("Hiện có ").append(allOverdue.size())
                    .append(" phiếu mượn quá hạn, tổng phạt tồn đọng ước tính: ")
                    .append(String.format("%,.0f", total)).append("đ\n");
        }

        // Thống kê hôm nay
        long activeToday = borrowRecordRepository.findAll().stream()
                .filter(r -> r.getBorrowStatus() == BorrowRecord.BorrowStatus.ACTIVE
                        || r.getBorrowStatus() == BorrowRecord.BorrowStatus.OVERDUE)
                .count();
        sb.append("[THỐNG KÊ]\n");
        sb.append("Tổng phiếu mượn đang hoạt động: ").append(activeToday).append("\n");
    }

    private String getBookTitle(java.util.UUID bookId) {
        try {
            return bookRepository.findById(bookId)
                    .map(Book::getTitle)
                    .orElse("Sách #" + bookId.toString().substring(0, 8));
        } catch (Exception e) {
            return "Không xác định";
        }
    }

    private String mapRole(String role) {
        return switch (role.toUpperCase()) {
            case "ADMIN" -> "Quản trị viên";
            case "LIBRARIAN" -> "Thủ thư";
            default -> "Độc giả";
        };
    }
}
