package com.lms.library.borrow.entity;

import com.lms.library.borrow.entity.enums.MemberType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "borrow_policies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowPolicy extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "policy_name", nullable = false, unique = true, length = 100)
    private String policyName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "member_type", nullable = false, length = 50)
    private MemberType memberType;

    @Column(name = "max_books_allowed", nullable = false)
    @Builder.Default
    private Integer maxBooksAllowed = 5;

    @Column(name = "loan_period_days", nullable = false)
    @Builder.Default
    private Integer loanPeriodDays = 14;

    @Column(name = "max_extensions", nullable = false)
    @Builder.Default
    private Integer maxExtensions = 3;

    @Column(name = "extension_days", nullable = false)
    @Builder.Default
    private Integer extensionDays = 7;

    @Column(name = "fine_per_day", nullable = false, precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal finePerDay = new BigDecimal("1.00");

    @Column(name = "max_fine_per_book", precision = 10, scale = 2)
    private BigDecimal maxFinePerBook;

    @Column(name = "book_format_allowed")
    private String bookFormatAllowed;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Explicit getters since Lombok not enabled
    public UUID getId() { return id; }
    public String getPolicyName() { return policyName; }
    public String getDescription() { return description; }
    public MemberType getMemberType() { return memberType; }
    public Integer getMaxBooksAllowed() { return maxBooksAllowed; }
    public Integer getLoanPeriodDays() { return loanPeriodDays; }
    public Integer getMaxExtensions() { return maxExtensions; }
    public Integer getExtensionDays() { return extensionDays; }
    public BigDecimal getFinePerDay() { return finePerDay; }
    public BigDecimal getMaxFinePerBook() { return maxFinePerBook; }
    public String getBookFormatAllowed() { return bookFormatAllowed; }
    public Boolean getIsActive() { return isActive; }

    // Explicit setters
    public void setId(UUID id) { this.id = id; }
    public void setPolicyName(String policyName) { this.policyName = policyName; }
    public void setDescription(String description) { this.description = description; }
    public void setMemberType(MemberType memberType) { this.memberType = memberType; }
    public void setMaxBooksAllowed(Integer maxBooksAllowed) { this.maxBooksAllowed = maxBooksAllowed; }
    public void setLoanPeriodDays(Integer loanPeriodDays) { this.loanPeriodDays = loanPeriodDays; }
    public void setMaxExtensions(Integer maxExtensions) { this.maxExtensions = maxExtensions; }
    public void setExtensionDays(Integer extensionDays) { this.extensionDays = extensionDays; }
    public void setFinePerDay(BigDecimal finePerDay) { this.finePerDay = finePerDay; }
    public void setMaxFinePerBook(BigDecimal maxFinePerBook) { this.maxFinePerBook = maxFinePerBook; }
    public void setBookFormatAllowed(String bookFormatAllowed) { this.bookFormatAllowed = bookFormatAllowed; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
