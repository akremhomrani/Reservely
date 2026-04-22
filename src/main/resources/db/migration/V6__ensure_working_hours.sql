-- Backfill default working hours for any business that has none.
-- Uses INSERT ... ON CONFLICT DO NOTHING so it's safe to run multiple times.

INSERT INTO working_hours (business_id, day_of_week, open_time, close_time, closed)
SELECT b.id, d.day, '09:00', '19:00', false
FROM businesses b
CROSS JOIN (VALUES
  ('MONDAY'),('TUESDAY'),('WEDNESDAY'),('THURSDAY'),('FRIDAY'),('SATURDAY')
) AS d(day)
WHERE NOT EXISTS (
    SELECT 1 FROM working_hours wh
    WHERE wh.business_id = b.id AND wh.day_of_week = d.day
)
ON CONFLICT (business_id, day_of_week) DO NOTHING;

INSERT INTO working_hours (business_id, day_of_week, open_time, close_time, closed)
SELECT b.id, 'SUNDAY', NULL, NULL, true
FROM businesses b
WHERE NOT EXISTS (
    SELECT 1 FROM working_hours wh
    WHERE wh.business_id = b.id AND wh.day_of_week = 'SUNDAY'
)
ON CONFLICT (business_id, day_of_week) DO NOTHING;
