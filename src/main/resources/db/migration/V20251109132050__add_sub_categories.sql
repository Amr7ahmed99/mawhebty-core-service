CREATE TABLE IF NOT EXISTS talent_sub_categories (
    id SERIAL PRIMARY KEY,
    name_en VARCHAR(255) UNIQUE NOT NULL,
    name_ar VARCHAR(255) UNIQUE NOT NULL,
    partner_id INTEGER UNIQUE NOT NULL,
    category_id INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_talent_category
        FOREIGN KEY (category_id)
        REFERENCES talent_categories (id)
        ON DELETE CASCADE
);

INSERT INTO talent_sub_categories (name_en, name_ar, partner_id, category_id, created_at, updated_at)
VALUES
('Football', 'كرة القدم', 1, 1, NOW(), NOW()),
('Basketball', 'كرة السلة', 2, 1, NOW(), NOW()),
('Tennis', 'التنس', 3, 1, NOW(), NOW()),
('Swimming', 'السباحة', 4, 1, NOW(), NOW()),
('Singing', 'الغناء', 5, 2, NOW(), NOW()),
('Piano', 'بيانو', 6, 2, NOW(), NOW()),
('Guitar', 'جيتار', 7, 2, NOW(), NOW()),
('Drums', 'طبول', 8, 2, NOW(), NOW()),
('Software Engineering', 'هندسة البرمجيات', 9, 3, NOW(), NOW()),
('Data Science', 'علوم البيانات', 10, 3, NOW(), NOW()),
('Cybersecurity', 'الأمن السيبراني', 11, 3, NOW(), NOW()),
('Artificial Intelligence', 'الذكاء الاصطناعي', 12, 3, NOW(), NOW()),
('Painting', 'الرسم', 13, 4, NOW(), NOW()),
('Sculpture', 'النحت', 14, 4, NOW(), NOW()),
('Photography', 'التصوير الفوتوغرافي', 15, 4, NOW(), NOW()),
('Designer', 'مصمم', 16, 5, NOW(), NOW()),
('Model', 'عارض', 17, 5, NOW(), NOW()),
('Chef', 'شيف', 18, 6, NOW(), NOW()),
('Baker', 'خباز', 19, 6, NOW(), NOW()),
('Teacher', 'معلم', 20, 7, NOW(), NOW()),
('Lecturer', 'محاضر', 21, 7, NOW(), NOW()),
('Personal Trainer', 'مدرب شخصي', 22, 8, NOW(), NOW()),
('Nutritionist', 'أخصائي تغذية', 23, 8, NOW(), NOW()),
('Yoga Instructor', 'مدرب يوجا', 24, 8, NOW(), NOW()),
('Entrepreneur', 'رائد أعمال', 25, 9, NOW(), NOW()),
('Consultant', 'استشاري', 26, 9, NOW(), NOW()),
('Film Director', 'مخرج أفلام', 27, 10, NOW(), NOW()),
('Photographer', 'مصور', 28, 10, NOW(), NOW()),
('Editor', 'محرر', 29, 10, NOW(), NOW());
