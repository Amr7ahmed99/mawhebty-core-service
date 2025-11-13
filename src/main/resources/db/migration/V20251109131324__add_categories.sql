CREATE TABLE IF NOT EXISTS talent_categories (
    id SERIAL PRIMARY KEY,
    name_en VARCHAR(255) UNIQUE NOT NULL,
    name_ar VARCHAR(255) UNIQUE NOT NULL,
    partner_id INTEGER UNIQUE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO talent_categories (name_en, name_ar, partner_id)
VALUES
('Sports', 'الرياضة', 1),
('Music', 'الموسيقى', 2),
('Technology', 'التكنولوجيا', 3),
('Art', 'الفن', 4),
('Fashion', 'الموضة', 5),
('Culinary Arts', 'فن الطهو', 6),
('Education', 'التعليم', 7),
('Health & Fitness', 'الصحة واللياقة البدنية', 8),
('Business', 'الأعمال', 9),
('Film & Media', 'السينما والإعلام', 10);
