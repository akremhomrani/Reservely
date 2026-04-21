CREATE TABLE service_items (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id      UUID         NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name             VARCHAR(200) NOT NULL,
    duration_minutes INTEGER      NOT NULL DEFAULT 30,
    price            DOUBLE PRECISION NOT NULL,
    description      VARCHAR(500),
    active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE staff (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID         NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    name        VARCHAR(100) NOT NULL,
    avatar_url  VARCHAR(500),
    rating      DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE staff_specialties (
    staff_id  UUID        NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    specialty VARCHAR(100) NOT NULL,
    PRIMARY KEY (staff_id, specialty)
);

CREATE INDEX idx_service_items_business ON service_items(business_id);
CREATE INDEX idx_staff_business         ON staff(business_id);
