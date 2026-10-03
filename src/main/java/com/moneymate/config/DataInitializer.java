package com.moneymate.config;

import com.moneymate.model.Category;
import com.moneymate.model.User;
import com.moneymate.repository.CategoryRepository;
import com.moneymate.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Value("${admin.email:${ADMIN_EMAIL:admin@gmail.com}}")
    private String adminEmail;

    @Value("${admin.password:${ADMIN_PASSWORD:admin123}}")
    private String adminPassword;

    @Value("${admin.name:${ADMIN_NAME:Admin}}")
    private String adminName;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        initAdminUser();
        initDefaultCategories();
    }

    private void initAdminUser() {
        String cleanEmail = (adminEmail != null && !adminEmail.trim().isEmpty())
                ? adminEmail.trim().toLowerCase() : "admin@gmail.com";
        String cleanName = (adminName != null && !adminName.trim().isEmpty())
                ? adminName.trim() : "Admin";
        String cleanPassword = (adminPassword != null && !adminPassword.trim().isEmpty())
                ? adminPassword.trim() : "admin123";

        Optional<User> adminOpt = userRepository.findByEmail(cleanEmail);
        if (adminOpt.isEmpty()) {
            User admin = new User(
                    cleanName,
                    cleanEmail,
                    passwordEncoder.encode(cleanPassword),
                    "ADMIN",
                    "Active",
                    "+91 9898989898",
                    cleanName.substring(0, 1).toUpperCase(),
                    LocalDate.now(),
                    BigDecimal.ZERO
            );
            userRepository.save(admin);
            logger.info("Administrator account initialized with BCrypt password.");
        } else {
            User admin = adminOpt.get();
            // If stored password was plain text from previous seed, upgrade it to BCrypt
            if (!admin.getPassword().startsWith("$2a$") && !admin.getPassword().startsWith("$2b$")) {
                admin.setPassword(passwordEncoder.encode(admin.getPassword()));
                userRepository.save(admin);
                logger.info("Existing admin password upgraded to BCrypt hash.");
            }
        }
    }

    private void initDefaultCategories() {
        if (categoryRepository.count() == 0) {
            categoryRepository.saveAll(Arrays.asList(
                    new Category("Food", "Utensils", "EXPENSE", "#EF4444", 0, BigDecimal.ZERO),
                    new Category("Transport", "Car", "EXPENSE", "#10B981", 0, BigDecimal.ZERO),
                    new Category("Shopping", "ShoppingBag", "EXPENSE", "#EC4899", 0, BigDecimal.ZERO),
                    new Category("Bills", "FileText", "EXPENSE", "#F59E0B", 0, BigDecimal.ZERO),
                    new Category("Education", "GraduationCap", "EXPENSE", "#8B5CF6", 0, BigDecimal.ZERO),
                    new Category("Health", "Heart", "EXPENSE", "#EF4444", 0, BigDecimal.ZERO),
                    new Category("Entertainment", "Gamepad2", "EXPENSE", "#3B82F6", 0, BigDecimal.ZERO),
                    new Category("Others", "MoreHorizontal", "EXPENSE", "#6B7280", 0, BigDecimal.ZERO),
                    new Category("Salary", "Briefcase", "INCOME", "#10B981", 0, BigDecimal.ZERO),
                    new Category("Freelance", "Laptop", "INCOME", "#06B6D4", 0, BigDecimal.ZERO)
            ));
            logger.info("Default financial categories initialized.");
        }
    }
}
