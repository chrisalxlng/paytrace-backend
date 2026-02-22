package dev.christopherlang.paytrace.features.payroll.data;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "payrolls")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class PayrollEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payroll_id")
    private UUID payrollId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "accounting_period")
    private YearMonth accountingPeriod;

    @Column(name = "payout")
    private BigDecimal payout;

    @Column(name = "consistency_deviation")
    private BigDecimal consistencyDeviation;

    @OneToMany(
        mappedBy = "payroll",
        cascade = CascadeType.ALL,
        orphanRemoval = true
    )
    @Builder.Default
    private List<PayrollEntryEntity> entries = new ArrayList<>();

    public void addEntry(PayrollEntryEntity entry) {
        entries.add(entry);
        entry.setPayroll(this);
    }

}
