-- Admin account seeding is dynamically handled in DataInitializer with BCrypt encoding and environment variables

-- Default Base Categories (Initial zeroed counts)
INSERT INTO categories (id, name, icon, type, color, transaction_count, budget_limit) VALUES
(1, 'Food', 'Utensils', 'EXPENSE', '#EF4444', 0, 0.00),
(2, 'Transport', 'Car', 'EXPENSE', '#10B981', 0, 0.00),
(3, 'Shopping', 'ShoppingBag', 'EXPENSE', '#EC4899', 0, 0.00),
(4, 'Bills', 'FileText', 'EXPENSE', '#F59E0B', 0, 0.00),
(5, 'Education', 'GraduationCap', 'EXPENSE', '#8B5CF6', 0, 0.00),
(6, 'Health', 'Heart', 'EXPENSE', '#EF4444', 0, 0.00),
(7, 'Entertainment', 'Gamepad2', 'EXPENSE', '#3B82F6', 0, 0.00),
(8, 'Others', 'MoreHorizontal', 'EXPENSE', '#6B7280', 0, 0.00),
(9, 'Salary', 'Briefcase', 'INCOME', '#10B981', 0, 0.00),
(10, 'Freelance', 'Laptop', 'INCOME', '#06B6D4', 0, 0.00);
