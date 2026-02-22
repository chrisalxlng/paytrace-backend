package dev.christopherlang.paytrace.features.payroll.data;

import dev.christopherlang.paytrace.features.payroll.domain.PayrollEntryType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payroll_entries")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PayrollEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payroll_entry_id")
    private UUID payrollEntryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private PayrollEntryType type;

    @Column(name = "amount")
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payroll_id")
    @ToString.Exclude
    private PayrollEntity payroll;

}
