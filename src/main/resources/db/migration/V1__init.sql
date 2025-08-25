-- Initial schema for Daily Embedded Learning System
-- V1__init.sql

-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    timezone VARCHAR(100) NOT NULL DEFAULT 'Asia/Ho_Chi_Minh',
    delivery_hour_local INTEGER NOT NULL DEFAULT 9,
    prefs_json TEXT DEFAULT '{}',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Plan items table
CREATE TABLE plan_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    seq INTEGER NOT NULL,
    topic_title VARCHAR(500) NOT NULL,
    focus_points TEXT,
    learning_goal TEXT,
    difficulty VARCHAR(50) DEFAULT 'intermediate',
    platforms VARCHAR(200),
    language VARCHAR(10) DEFAULT 'en',
    experience_level VARCHAR(50) DEFAULT 'intermediate',
    output_type VARCHAR(20) DEFAULT 'both',
    authoritative_links TEXT,
    tags VARCHAR(500),
    notes TEXT,
    status VARCHAR(20) DEFAULT 'PLANNED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT NOW(),
    CONSTRAINT fk_plan_items_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT unique_user_date_seq UNIQUE (user_id, date, seq),
    CONSTRAINT check_seq_positive CHECK (seq >= 1),
    CONSTRAINT check_status CHECK (status IN ('PLANNED', 'SENT', 'SKIPPED')),
    CONSTRAINT check_output_type CHECK (output_type IN ('overview', 'deepdive', 'both')),
    CONSTRAINT check_language CHECK (language = 'en')
);

-- Lessons table
CREATE TABLE lessons (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    date DATE NOT NULL,
    seq INTEGER NOT NULL,
    topic_snapshot_json TEXT,
    overview_md TEXT,
    deep_dive_md TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_lessons_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Email logs table
CREATE TABLE email_logs (
    id BIGSERIAL PRIMARY KEY,
    lesson_id BIGINT,
    type VARCHAR(20) NOT NULL,
    to_email VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider_msg_id VARCHAR(500),
    error TEXT,
    sent_at TIMESTAMP WITH TIME ZONE,
    CONSTRAINT fk_email_logs_lesson FOREIGN KEY (lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    CONSTRAINT check_email_type CHECK (type IN ('OVERVIEW', 'DEEPDIVE', 'DIGEST')),
    CONSTRAINT check_email_status CHECK (status IN ('SENT', 'FAILED'))
);

-- Settings table
CREATE TABLE settings (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    email_mode VARCHAR(20) DEFAULT 'DIGEST_AND_SPLIT',
    max_deep_dives_per_day INTEGER DEFAULT 5,
    model VARCHAR(50) DEFAULT 'gpt-3.5-turbo',
    temperature DECIMAL(3,2) DEFAULT 0.7,
    max_tokens INTEGER DEFAULT 2000,
    cc_list_json TEXT DEFAULT '[]',
    CONSTRAINT fk_settings_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT check_email_mode CHECK (email_mode IN ('DIGEST_AND_SPLIT', 'SINGLE')),
    CONSTRAINT check_temperature CHECK (temperature >= 0.0 AND temperature <= 2.0),
    CONSTRAINT check_max_tokens CHECK (max_tokens > 0)
);

-- Indexes for better performance
CREATE INDEX idx_plan_items_user_date ON plan_items(user_id, date);
CREATE INDEX idx_plan_items_status ON plan_items(status);
CREATE INDEX idx_lessons_user_date ON lessons(user_id, date);
CREATE INDEX idx_email_logs_lesson_id ON email_logs(lesson_id);
CREATE INDEX idx_email_logs_sent_at ON email_logs(sent_at);
CREATE INDEX idx_users_email ON users(email);

-- Comments for documentation
COMMENT ON TABLE users IS 'User accounts with delivery preferences and timezone settings';
COMMENT ON TABLE plan_items IS 'Daily learning topics planned for each user with sequencing';
COMMENT ON TABLE lessons IS 'Generated lesson content (overview and deep-dive) from LLM';
COMMENT ON TABLE email_logs IS 'Email delivery tracking with status and error logging';
COMMENT ON TABLE settings IS 'Per-user configuration for email delivery and LLM parameters';
