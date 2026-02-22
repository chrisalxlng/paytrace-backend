CREATE TABLE payrolls (
    payroll_id UUID PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    accounting_period VARCHAR(7) NOT NULL,
    payout NUMERIC(19, 2) NOT NULL,
    consistency_deviation NUMERIC(19, 2) NOT NULL DEFAULT 0
);

CREATE TABLE payroll_entries (
    payroll_entry_id UUID PRIMARY KEY,
    payroll_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(19, 2) NOT NULL,
    CONSTRAINT fk_payroll FOREIGN KEY (payroll_id) REFERENCES payrolls(payroll_id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX idx_payroll_user_accounting_period
ON payrolls(user_id, accounting_period);

CREATE INDEX idx_payroll_user_id
ON payrolls(user_id);
