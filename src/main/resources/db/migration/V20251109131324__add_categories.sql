CREATE TABLE IF NOT EXISTS talent_categories (
    id SERIAL PRIMARY KEY,
    name_en VARCHAR(255) UNIQUE NOT NULL,
    name_ar VARCHAR(255) UNIQUE NOT NULL,
    partner_id INTEGER UNIQUE NOT NULL,
    participation_type_id INTEGER NOT NULL REFERENCES participation_types(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);


INSERT INTO talent_categories (name_en, name_ar, partner_id, participation_type_id, created_at, updated_at)
VALUES
('Sports', 'الرياضة', 1, 1,NOW(), NOW()),
('Music', 'الموسيقى', 2, 1, NOW(), NOW()),
('Technology', 'التكنولوجيا', 3, 1, NOW(), NOW()),
('Art', 'الفن', 4, 1, NOW(), NOW()),
('Fashion', 'الموضة', 5, 1, NOW(), NOW()),
('Culinary Arts', 'فن الطهو', 6, 1, NOW(), NOW()),
('Education', 'التعليم', 7, 1, NOW(), NOW()),
('Health & Fitness', 'الصحة واللياقة البدنية', 8, 1, NOW(), NOW()),
('Business', 'الأعمال', 9, 1, NOW(), NOW()),
('Film & Media', 'السينما والإعلام', 10, 1, NOW(), NOW());