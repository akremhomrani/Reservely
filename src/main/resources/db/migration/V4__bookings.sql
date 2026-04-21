CREATE TABLE bookings (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_code VARCHAR(30)  NOT NULL UNIQUE,
    customer_id    UUID         NOT NULL REFERENCES users(id),
    business_id    UUID         NOT NULL REFERENCES businesses(id),
    service_id     UUID         NOT NULL REFERENCES service_items(id),
    staff_id       UUID         REFERENCES staff(id),
    start_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    end_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'CONFIRMED',
    notes          VARCHAR(500),
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bookings_customer ON bookings(customer_id);
CREATE INDEX idx_bookings_business ON bookings(business_id, start_at);
CREATE INDEX idx_bookings_staff    ON bookings(staff_id, start_at);
