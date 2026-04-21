CREATE TABLE businesses (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id      UUID REFERENCES users(id) ON DELETE SET NULL,
    name          VARCHAR(200) NOT NULL,
    address       VARCHAR(300),
    city          VARCHAR(100) NOT NULL,
    lat           DOUBLE PRECISION NOT NULL DEFAULT 36.8190,
    lng           DOUBLE PRECISION NOT NULL DEFAULT 10.1658,
    category      VARCHAR(30)  NOT NULL DEFAULT 'BARBER',
    gender_target VARCHAR(10)  NOT NULL DEFAULT 'MEN',
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    phone         VARCHAR(20),
    image_url     VARCHAR(500),
    rating_avg    DOUBLE PRECISION NOT NULL DEFAULT 0.0,
    review_count  INTEGER          NOT NULL DEFAULT 0,
    created_at    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE business_tags (
    business_id UUID        NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    tag         VARCHAR(50) NOT NULL,
    PRIMARY KEY (business_id, tag)
);

CREATE TABLE working_hours (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    business_id UUID        NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    day_of_week VARCHAR(10) NOT NULL,
    open_time   VARCHAR(5),
    close_time  VARCHAR(5),
    closed      BOOLEAN     NOT NULL DEFAULT FALSE,
    UNIQUE (business_id, day_of_week)
);

CREATE INDEX idx_businesses_city_status ON businesses(city, status);
CREATE INDEX idx_businesses_category    ON businesses(category);
