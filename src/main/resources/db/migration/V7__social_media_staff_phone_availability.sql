-- Social media handles on businesses
ALTER TABLE businesses
  ADD COLUMN IF NOT EXISTS instagram_handle VARCHAR(100),
  ADD COLUMN IF NOT EXISTS facebook_handle  VARCHAR(100),
  ADD COLUMN IF NOT EXISTS tiktok_handle    VARCHAR(100),
  ADD COLUMN IF NOT EXISTS whatsapp_number  VARCHAR(30);

-- Direct phone number on each staff member
ALTER TABLE staff
  ADD COLUMN IF NOT EXISTS phone VARCHAR(30);

-- Owner can mark individual staff as unavailable for a date range
CREATE TABLE IF NOT EXISTS staff_availability (
    id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    staff_id         UUID NOT NULL REFERENCES staff(id) ON DELETE CASCADE,
    business_id      UUID NOT NULL REFERENCES businesses(id) ON DELETE CASCADE,
    unavailable_from DATE NOT NULL,
    unavailable_to   DATE NOT NULL,
    created_at       TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT staff_avail_dates_check CHECK (unavailable_to >= unavailable_from)
);

CREATE INDEX IF NOT EXISTS idx_staff_avail_staff_dates
    ON staff_availability (staff_id, unavailable_from, unavailable_to);
