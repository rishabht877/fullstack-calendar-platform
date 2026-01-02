-- Fix database character encoding to support emojis
-- Run this on your AWS RDS database

-- 1. Alter the database to use utf8mb4
ALTER DATABASE calendar_db CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- 2. Alter the events table to use utf8mb4
ALTER TABLE events CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 3. Alter the calendars table to use utf8mb4
ALTER TABLE calendars CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. Alter the users table to use utf8mb4
ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Verify the changes
SHOW CREATE TABLE events;
SHOW CREATE TABLE calendars;
SHOW CREATE TABLE users;
