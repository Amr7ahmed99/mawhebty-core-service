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
('Football', 'كرة القدم', 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Basketball', 'كرة السلة', 2, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Tennis', 'التنس', 3, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Swimming', 'السباحة', 4, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Singing', 'الغناء', 5, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Piano', 'بيانو', 6, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Guitar', 'جيتار', 7, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Drums', 'طبول', 8, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Software Engineering', 'هندسة البرمجيات', 9, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Data Science', 'علوم البيانات', 10, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Cybersecurity', 'الأمن السيبراني', 11, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Artificial Intelligence', 'الذكاء الاصطناعي', 12, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Painting', 'الرسم', 13, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Sculpture', 'النحت', 14, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Photography', 'التصوير الفوتوغرافي', 15, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Designer', 'مصمم', 16, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Model', 'عارض', 17, 5, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Chef', 'شيف', 18, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Baker', 'خباز', 19, 6, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Teacher', 'معلم', 20, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Lecturer', 'محاضر', 21, 7, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Personal Trainer', 'مدرب شخصي', 22, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Nutritionist', 'أخصائي تغذية', 23, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Yoga Instructor', 'مدرب يوجا', 24, 8, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Entrepreneur', 'رائد أعمال', 25, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Consultant', 'استشاري', 26, 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Film Director', 'مخرج أفلام', 27, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Photographer', 'مصور', 28, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('Editor', 'محرر', 29, 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
