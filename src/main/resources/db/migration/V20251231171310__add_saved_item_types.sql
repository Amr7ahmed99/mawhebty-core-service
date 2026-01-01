CREATE TABLE IF NOT EXISTS saved_item_types (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO saved_item_types (name)
VALUES
    ('POST'),
    ('EVENT'),
    ('ARTICLE')
    ON CONFLICT (name) DO NOTHING;