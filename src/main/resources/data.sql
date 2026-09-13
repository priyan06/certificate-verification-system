-- Clear existing tables
DELETE FROM activity_logs;
DELETE FROM certificates;
DELETE FROM certificate_templates;
DELETE FROM students;
DELETE FROM users;

-- BCrypt hashed password for 'admin123': $2a$10$eD42.q4Yf7Lg7fW7D7n0ueX7sJ/9Z7K6p3D0g7X7u3J1X7k5G.1m.
-- BCrypt hashed password for 'student123': $2a$10$eD42.q4Yf7Lg7fW7D7n0ueX7sJ/9Z7K6p3D0g7X7u3J1X7k5G.1m.

-- 1. Admin User
INSERT INTO users (id, username, password, role, enabled, password_change_required, created_at, updated_at) VALUES
(1, 'admin@cert.com', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50CRVtCI881aT2aSpRny', 'ADMIN', true, false, NOW(), NOW());

-- 2. Student Users
INSERT INTO users (id, username, password, role, enabled, password_change_required, created_at, updated_at) VALUES
(2, 'STU1001', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50CRVtCI881aT2aSpRny', 'STUDENT', true, false, NOW(), NOW()),
(3, 'STU1002', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50CRVtCI881aT2aSpRny', 'STUDENT', true, false, NOW(), NOW()),
(4, 'STU1003', '$2a$10$8.UnVuG9HHgffUDAlk8qfOUVGkqRzgVym50CRVtCI881aT2aSpRny', 'STUDENT', true, false, NOW(), NOW());

-- 3. Student Details
INSERT INTO students (id, user_id, roll_number, register_number, name, email, mobile_number, course, department, batch, date_of_birth, address, status, created_at, updated_at) VALUES
(1, 2, 'STU1001', 'REG2024001', 'Rahul Sharma', 'rahul.sharma@example.com', '9876543210', 'Master of Computer Applications (MCA)', 'Computer Applications', '2024-2026', '2001-05-15', 'Chennai, Tamil Nadu', 'ACTIVE', NOW(), NOW()),
(2, 3, 'STU1002', 'REG2024002', 'Priya Patel', 'priya.patel@example.com', '9876543211', 'B.Tech Information Technology', 'Information Technology', '2022-2026', '2002-08-20', 'Bangalore, Karnataka', 'ACTIVE', NOW(), NOW()),
(3, 4, 'STU1003', 'REG2024003', 'Amit Kumar', 'amit.kumar@example.com', '9876543212', 'Bachelor of Computer Applications (BCA)', 'Computer Applications', '2023-2026', '2003-02-10', 'Hyderabad, Telangana', 'ACTIVE', NOW(), NOW());

-- 4. Certificate Templates
INSERT INTO certificate_templates (id, template_name, certificate_title, description, signatory_one_name, signatory_one_designation, signatory_one_image, signatory_two_name, signatory_two_designation, signatory_two_image, logo_path, background_path, active, created_at, updated_at) VALUES
(1, 'Course Completion Certificate', 'CERTIFICATE OF COMPLETION', 'This certificate is awarded for successfully completing the Master of Computer Applications program with distinction.', 'Thomasine Mosley', 'Chief Ecologist', '', 'Willa Payne', 'Company Director', '', '/images/default-logo.png', '', true, NOW(), NOW()),
(2, 'Workshop Participation Certificate', 'CERTIFICATE OF PARTICIPATION', 'This certificate is awarded for active participation in the Advanced Java Programming Workshop.', 'Dr. S. R. Ramanathan', 'Director Academic Affairs', '', 'Prof. Meenakshi Sundaram', 'Head of Department', '', '/images/default-logo.png', '', true, NOW(), NOW());

-- 5. Certificates (1 active valid, 1 active valid, 1 revoked)
INSERT INTO certificates (id, certificate_number, verification_code, student_id, template_id, certificate_title, description, issue_date, status, qr_code_path, qr_code_data, certificate_file_path, revoked_at, revocation_reason, created_at, updated_at) VALUES
(1, 'CERT-2026-000001', '31f8b76d-1234-4567-89ab-cdef01234567', 1, 1, 'CERTIFICATE OF COMPLETION', 'Completed Master of Computer Applications (MCA) with First Class with Distinction.', '2026-05-15', 'ACTIVE', '', '', '', NULL, NULL, NOW(), NOW()),
(2, 'CERT-2026-000002', '550e8400-e29b-41d4-a716-446655440000', 2, 2, 'CERTIFICATE OF PARTICIPATION', 'Participated in AI & Cloud Computing Workshop 2026.', '2026-06-01', 'ACTIVE', '', '', '', NULL, NULL, NOW(), NOW()),
(3, 'CERT-2026-000003', '7c9e6679-7425-40de-944b-e07fc1f90ae7', 3, 1, 'CERTIFICATE OF COMPLETION', 'Revoked certificate due to administrative correction.', '2026-04-10', 'REVOKED', '', '', '', '2026-04-15 10:30:00', 'Duplicate record issued in error', NOW(), NOW());
