package vn.binhan.config;

import vn.binhan.model.Medicine;
import vn.binhan.repository.MedicineRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

@Configuration
public class MedicineDataInitializer {
    @Bean
    public CommandLineRunner seedMedicines(MedicineRepository medicines) {
        return args -> {
            if (medicines.count() == 0) {
                medicines.saveAll(Arrays.asList(
                        new Medicine("Paracetamol 500mg", "Giảm đau, hạ sốt",
                                "Hộp 10 vỉ x 10 viên", new BigDecimal("25000"), 28,
                                false, LocalDate.of(2028, 12, 31), "✚", "mint"),
                        new Medicine("Vitamin C 500mg", "Vitamin & bổ sung",
                                "Tuýp 10 viên sủi", new BigDecimal("42000"), 16,
                                false, LocalDate.of(2028, 6, 30), "☼", "peach"),
                        new Medicine("Cetirizine 10mg", "Dị ứng",
                                "Hộp 10 vỉ x 10 viên", new BigDecimal("35000"), 20,
                                false, LocalDate.of(2027, 11, 30), "✳", "lavender"),
                        new Medicine("Nước muối sinh lý", "Chăm sóc cá nhân",
                                "Chai 500ml", new BigDecimal("12000"), 34,
                                false, LocalDate.of(2028, 3, 31), "◉", "blue"),
                        new Medicine("Men vi sinh BioCare", "Tiêu hóa",
                                "Hộp 20 gói", new BigDecimal("89000"), 12,
                                false, LocalDate.of(2027, 9, 30), "❋", "mint"),
                        new Medicine("Amoxicillin 500mg", "Kháng sinh",
                                "Hộp 10 vỉ x 10 viên", new BigDecimal("65000"), 8,
                                true, LocalDate.of(2027, 8, 31), "✚", "peach")
                ));
            }
        };
    }
}
