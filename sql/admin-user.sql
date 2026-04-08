-- Admin user with BCrypt encoded password (password: adminpass)
INSERT INTO user (username, email, password, role) VALUES 
('admin', 'admin@healthcare.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAGe5RmwzQ8vW0K.0gD./rLq3h17C', 'admin');

-- Enable JPA to run (restart backend)
-- Password: adminpass
