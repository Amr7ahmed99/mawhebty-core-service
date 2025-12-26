-- =========================================================
-- V1_1__insert_enum_constants.sql
-- Inserts all enum constants into related lookup tables
-- =========================================================

-- =========================================
-- Permissions
-- =========================================
CREATE TABLE IF NOT EXISTS user_permissions (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO user_permissions (id, name)
VALUES
 (1, 'READ_POSTS'),
 (2, 'READ_PROFILE'),
 (3, 'READ_FEEDS'),
 (4, 'VIEW_TALENTS'),
 (5, 'VIEW_RESEARCHERS'),
 (6, 'LIKE'),
 (7, 'COMMENT'),
 (8, 'FOLLOW'),
 (9, 'SAVE_POSTS'),
 (10, 'MANAGE_TALENT_PROFILE'),
 (11, 'RECEIVE_CONTRACTS'),
 (12, 'UPLOAD_CONTENT'),
 (13, 'MANAGE_RESEARCHER_PROFILE'),
 (14, 'SEND_CONTRACTS'),
 (15, 'SEARCH'),
 (16, 'EDIT_TALENT_SHARED_CONTENT'),
 (17, 'BOOST_LISTINGS')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- User Roles
-- =========================================
CREATE TABLE IF NOT EXISTS user_role (
    id SERIAL PRIMARY KEY,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO user_role (id, created_at, updated_at, name)
VALUES
 (1, NOW(), NOW(), 'TALENT'),
 (2, NOW(), NOW(), 'RESEARCHER')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- Participation Types
-- =========================================
CREATE TABLE IF NOT EXISTS participation_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO participation_types (id, name)
VALUES
 (1, 'PROJECT_IDEA'),
 (2, 'PERSONAL_TALENT'),
 (3, 'PATENT')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- Subscription Status
-- =========================================
CREATE TABLE IF NOT EXISTS subscription_status (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO subscription_status (id, name)
VALUES
 (1, 'ACTIVE'),
 (2, 'EXPIRED'),
 (3, 'CANCELLED'),
 (4, 'PENDING_RENEWAL')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- User Types
-- =========================================
CREATE TABLE IF NOT EXISTS user_types (
    id SERIAL PRIMARY KEY,
    type VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO user_types (id, type)
VALUES
 (1, 'INDIVIDUAL'),
 (2, 'COMPANY')
ON CONFLICT (id) DO NOTHING;

-- =========================================
-- Gender Enum
-- =========================================
CREATE TABLE IF NOT EXISTS genders (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO genders (name)
VALUES
 ('MALE'),
 ('FEMALE')
ON CONFLICT (name) DO NOTHING;

-- =========================================
-- Post Status
-- =========================================
CREATE TABLE IF NOT EXISTS post_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO post_statuses (name)
VALUES
 ('DRAFT'),
 ('PENDING_MODERATION'),
 ('PUBLISHED'),
 ('REJECTED')
ON CONFLICT (name) DO NOTHING;

-- =========================================
-- Post Visibility
-- =========================================
CREATE TABLE IF NOT EXISTS post_visibility (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO post_visibility (name)
VALUES
 ('PUBLIC'),
 ('PRIVATE'),
 ('FOLLOWERS_ONLY')
ON CONFLICT (name) DO NOTHING;

-- =========================================
-- Moderation Types
-- =========================================
CREATE TABLE IF NOT EXISTS moderation_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(100) UNIQUE NOT NULL
);

INSERT INTO moderation_types (name)
VALUES
 ('USER_REGISTRATION'),
 ('MEDIA_CONTENT'),
 ('PROFILE_UPDATE'),
 ('DOCUMENT_VERIFICATION')
ON CONFLICT (name) DO NOTHING;

-- =========================================
-- Media Moderation Status
-- =========================================
CREATE TABLE IF NOT EXISTS media_moderation_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO media_moderation_statuses (name)
VALUES
 ('PENDING'),
 ('APPROVED'),
 ('REJECTED')
ON CONFLICT (name) DO NOTHING;

-- =========================================
-- OTP Types
-- =========================================
CREATE TABLE IF NOT EXISTS otp_types (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO otp_types (name)
VALUES
 ('REGISTRATION'),
 ('PASSWORD_RESET'),
 ('LOGIN')
ON CONFLICT (name) DO NOTHING;

-- =========================================
-- User Status Enum
-- =========================================
CREATE TABLE IF NOT EXISTS user_statuses (
    id SERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL
);

INSERT INTO user_statuses (name)
VALUES
 ('DRAFT'),
 ('PENDING_MODERATION'),
 ('ACTIVE'),
 ('REJECTED'),
 ('SUSPENDED')
ON CONFLICT (name) DO NOTHING;


CREATE TABLE IF NOT EXISTS permission_role (
    id SERIAL PRIMARY KEY,
    permission_id INTEGER NOT NULL,
    role_id INTEGER NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_permission
        FOREIGN KEY (permission_id)
        REFERENCES user_permissions (id)
        ON DELETE CASCADE,

    CONSTRAINT fk_role
        FOREIGN KEY (role_id)
        REFERENCES user_role (id)
        ON DELETE CASCADE,

    CONSTRAINT uq_permission_role UNIQUE (permission_id, role_id)
);


-- =======================================================
-- 3️⃣ Insert permissions for RESEARCHER
-- =======================================================
INSERT INTO permission_role (permission_id, role_id, created_at, updated_at)
SELECT p.id, (SELECT id FROM user_role WHERE name= 'RESEARCHER'), NOW(), NOW()
FROM user_permissions p
WHERE p.name IN (
    'READ_PROFILE',
    'READ_FEEDS',
    'READ_POSTS',
    'SEARCH',
    'VIEW_TALENTS',
    'VIEW_RESEARCHERS',
    'LIKE',
    'COMMENT',
    'FOLLOW',
    'SAVE_POSTS',
    'MANAGE_RESEARCHER_PROFILE',
    'SEND_CONTRACTS',
    'BOOST_LISTINGS'
)
ON CONFLICT DO NOTHING;

-- =======================================================
-- 3️⃣ Insert permissions for TALENT
-- =======================================================
INSERT INTO permission_role (permission_id, role_id, created_at, updated_at)
SELECT p.id, (SELECT id FROM user_role WHERE name= 'TALENT'), NOW(), NOW()
FROM user_permissions p
WHERE p.name IN (
    'READ_PROFILE',
    'READ_FEEDS',
    'READ_POSTS',
    'SEARCH',
    'VIEW_TALENTS',
    'VIEW_RESEARCHERS',
    'LIKE',
    'COMMENT',
    'FOLLOW',
    'SAVE_POSTS',
    'MANAGE_TALENT_PROFILE',
    'RECEIVE_CONTRACTS',
    'UPLOAD_CONTENT'
)
ON CONFLICT DO NOTHING;
