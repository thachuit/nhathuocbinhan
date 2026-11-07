import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

public class Main {
    private static final String DATA_FILE = "binh-an-data.properties";
    private static final Scanner INPUT = new Scanner(System.in);
    private static final NumberFormat CURRENCY = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    final List<Medicine> medicines = new ArrayList<>();
    final List<SaleOrder> orders = new ArrayList<>();
    private final Map<Integer, Integer> cart = new LinkedHashMap<>();

    public static void main(String[] args) {
        new Main().run();
    }

    private void run() {
        try {
            loadData();
        } catch (IOException | RuntimeException e) {
            System.err.println("Không thể đọc dữ liệu: " + e.getMessage());
            return;
        }

        System.out.println("=== QUẢN LÝ BÁN THUỐC TẠI NHÀ ===");
        while (true) {
            printMenu();
            int choice = readInt("Chọn chức năng: ", 0, 7);
            try {
                switch (choice) {
                    case 1:
                        showMedicines(medicines);
                        break;
                    case 2:
                        searchMedicines();
                        break;
                    case 3:
                        addToCart();
                        break;
                    case 4:
                        showCart();
                        break;
                    case 5:
                        checkout();
                        break;
                    case 6:
                        showOrderHistory();
                        break;
                    case 7:
                        addMedicine();
                        break;
                    case 0:
                        System.out.println("Đã thoát chương trình.");
                        return;
                    default:
                        break;
                }
            } catch (IOException e) {
                System.err.println("Không thể lưu dữ liệu: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Xem danh sách thuốc");
        System.out.println("2. Tìm thuốc");
        System.out.println("3. Thêm thuốc vào giỏ");
        System.out.println("4. Xem giỏ hàng");
        System.out.println("5. Tạo đơn / thanh toán");
        System.out.println("6. Lịch sử đơn hàng");
        System.out.println("7. Thêm thuốc mới");
        System.out.println("0. Thoát");
    }

    void loadData() throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(DATA_FILE);
        if (!java.nio.file.Files.exists(path)) {
            seedMedicines();
            saveData();
            return;
        }

        Properties data = new Properties();
        try (InputStream input = java.nio.file.Files.newInputStream(path)) {
            data.load(input);
        }

        int medicineCount = readCount(data, "medicine.count");
        for (int i = 0; i < medicineCount; i++) {
            String prefix = "medicine." + i + ".";
            medicines.add(new Medicine(
                    Integer.parseInt(required(data, prefix + "id")),
                    required(data, prefix + "name"),
                    required(data, prefix + "category"),
                    new BigDecimal(required(data, prefix + "price")),
                    Integer.parseInt(required(data, prefix + "stock")),
                    Boolean.parseBoolean(required(data, prefix + "prescription")),
                    LocalDate.parse(required(data, prefix + "expiry"))));
        }

        int orderCount = readCount(data, "order.count");
        for (int i = 0; i < orderCount; i++) {
            String prefix = "order." + i + ".";
            SaleOrder order = new SaleOrder(
                    LocalDateTime.parse(required(data, prefix + "time")),
                    required(data, prefix + "customer"),
                    required(data, prefix + "phone"),
                    required(data, prefix + "address"));
            int itemCount = readCount(data, prefix + "item.count");
            for (int j = 0; j < itemCount; j++) {
                String itemPrefix = prefix + "item." + j + ".";
                order.items.add(new OrderItem(
                        required(data, itemPrefix + "name"),
                        Integer.parseInt(required(data, itemPrefix + "quantity")),
                        new BigDecimal(required(data, itemPrefix + "price"))));
            }
            orders.add(order);
        }
    }

    private int readCount(Properties data, String key) {
        int count = Integer.parseInt(required(data, key));
        if (count < 0) {
            throw new IllegalArgumentException("Số lượng dữ liệu không hợp lệ: " + key);
        }
        return count;
    }

    private String required(Properties data, String key) {
        String value = data.getProperty(key);
        if (value == null) {
            throw new IllegalArgumentException("Thiếu trường dữ liệu: " + key);
        }
        return value;
    }

    void saveData() throws IOException {
        Properties data = new Properties();
        data.setProperty("medicine.count", Integer.toString(medicines.size()));
        for (int i = 0; i < medicines.size(); i++) {
            Medicine medicine = medicines.get(i);
            String prefix = "medicine." + i + ".";
            data.setProperty(prefix + "id", Integer.toString(medicine.id));
            data.setProperty(prefix + "name", medicine.name);
            data.setProperty(prefix + "category", medicine.category);
            data.setProperty(prefix + "price", medicine.price.toPlainString());
            data.setProperty(prefix + "stock", Integer.toString(medicine.stock));
            data.setProperty(prefix + "prescription", Boolean.toString(medicine.prescriptionRequired));
            data.setProperty(prefix + "expiry", medicine.expiry.toString());
        }

        data.setProperty("order.count", Integer.toString(orders.size()));
        for (int i = 0; i < orders.size(); i++) {
            SaleOrder order = orders.get(i);
            String prefix = "order." + i + ".";
            data.setProperty(prefix + "time", order.time.toString());
            data.setProperty(prefix + "customer", order.customer);
            data.setProperty(prefix + "phone", order.phone);
            data.setProperty(prefix + "address", order.address);
            data.setProperty(prefix + "item.count", Integer.toString(order.items.size()));
            for (int j = 0; j < order.items.size(); j++) {
                OrderItem item = order.items.get(j);
                String itemPrefix = prefix + "item." + j + ".";
                data.setProperty(itemPrefix + "name", item.name);
                data.setProperty(itemPrefix + "quantity", Integer.toString(item.quantity));
                data.setProperty(itemPrefix + "price", item.price.toPlainString());
            }
        }

        java.nio.file.Path path = java.nio.file.Paths.get(DATA_FILE);
        java.nio.file.Path temp = java.nio.file.Paths.get(DATA_FILE + ".tmp");
        try (OutputStream output = java.nio.file.Files.newOutputStream(temp)) {
            data.store(output, "Du lieu ung dung ban thuoc");
        }
        try {
            java.nio.file.Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING,
                    java.nio.file.StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            java.nio.file.Files.move(temp, path, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void seedMedicines() {
        medicines.add(new Medicine(1, "Paracetamol 500mg", "Giảm đau, hạ sốt",
                new BigDecimal("15000"), 30, false, LocalDate.of(2028, 12, 31)));
        medicines.add(new Medicine(2, "Vitamin C 500mg", "Vitamin",
                new BigDecimal("25000"), 20, false, LocalDate.of(2028, 6, 30)));
        medicines.add(new Medicine(3, "Cetirizine 10mg", "Dị ứng",
                new BigDecimal("30000"), 15, false, LocalDate.of(2027, 11, 30)));
        medicines.add(new Medicine(4, "Amoxicillin 500mg", "Kháng sinh",
                new BigDecimal("50000"), 10, true, LocalDate.of(2027, 8, 31)));
    }

    private void showMedicines(List<Medicine> list) {
        if (list.isEmpty()) {
            System.out.println("Không tìm thấy thuốc.");
            return;
        }
        System.out.printf("%-5s %-25s %-20s %-15s %-8s %-15s %-12s%n",
                "Mã", "Tên thuốc", "Nhóm", "Giá", "Tồn", "Loại", "Hạn dùng");
        for (Medicine medicine : list) {
            String status = medicine.expiry.isBefore(LocalDate.now()) ? "ĐÃ HẾT HẠN"
                    : medicine.stock == 0 ? "HẾT HÀNG"
                    : medicine.prescriptionRequired ? "Cần đơn" : "Không cần đơn";
            System.out.printf("%-5d %-25s %-20s %-15s %-8d %-15s %-12s%n",
                    medicine.id, medicine.name, medicine.category, money(medicine.price),
                    medicine.stock, status, medicine.expiry);
        }
    }

    private void searchMedicines() {
        String query = readText("Nhập tên hoặc nhóm thuốc cần tìm: ").toLowerCase(Locale.ROOT);
        List<Medicine> matches = new ArrayList<>();
        for (Medicine medicine : medicines) {
            if (medicine.name.toLowerCase(Locale.ROOT).contains(query)
                    || medicine.category.toLowerCase(Locale.ROOT).contains(query)) {
                matches.add(medicine);
            }
        }
        showMedicines(matches);
    }

    private void addToCart() {
        showMedicines(medicines);
        int id = readInt("Nhập mã thuốc (0 để hủy): ", 0, Integer.MAX_VALUE);
        if (id == 0) {
            return;
        }
        Medicine medicine = findMedicine(id);
        if (medicine == null) {
            System.out.println("Không có thuốc với mã này.");
            return;
        }
        if (medicine.expiry.isBefore(LocalDate.now())) {
            System.out.println("Thuốc đã hết hạn, không thể bán.");
            return;
        }
        int currentQuantity = cart.containsKey(id) ? cart.get(id) : 0;
        int quantity = readInt("Số lượng: ", 1, medicine.stock - currentQuantity);
        cart.put(id, currentQuantity + quantity);
        System.out.println("Đã thêm thuốc vào giỏ.");
    }

    private void showCart() {
        if (cart.isEmpty()) {
            System.out.println("Giỏ hàng đang trống.");
            return;
        }
        BigDecimal total = BigDecimal.ZERO;
        System.out.println("=== GIỎ HÀNG ===");
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Medicine medicine = findMedicine(entry.getKey());
            BigDecimal lineTotal = medicine.price.multiply(BigDecimal.valueOf(entry.getValue()));
            total = total.add(lineTotal);
            System.out.printf("%s x%d - %s%n", medicine.name, entry.getValue(), money(lineTotal));
        }
        System.out.println("Tổng cộng: " + money(total));
    }

    private void checkout() throws IOException {
        if (cart.isEmpty()) {
            System.out.println("Giỏ hàng đang trống.");
            return;
        }
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Medicine medicine = findMedicine(entry.getKey());
            if (medicine == null || medicine.stock < entry.getValue()
                    || medicine.expiry.isBefore(LocalDate.now())) {
                System.out.println("Giỏ hàng có thuốc hết hàng hoặc hết hạn. Vui lòng kiểm tra lại.");
                return;
            }
        }

        boolean hasPrescriptionMedicine = false;
        for (Integer id : cart.keySet()) {
            if (findMedicine(id).prescriptionRequired) {
                hasPrescriptionMedicine = true;
                break;
            }
        }
        if (hasPrescriptionMedicine
                && !readYesNo("Giỏ có thuốc kê đơn. Khách đã cung cấp đơn thuốc hợp lệ? (c/k): ")) {
            System.out.println("Đơn hàng chưa được tạo.");
            return;
        }

        String customer = readText("Tên khách hàng: ");
        String phone = readText("Số điện thoại: ");
        String address = readText("Địa chỉ giao hàng: ");
        SaleOrder order = new SaleOrder(LocalDateTime.now(), customer, phone, address);
        for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
            Medicine medicine = findMedicine(entry.getKey());
            order.items.add(new OrderItem(medicine.name, entry.getValue(), medicine.price));
            medicine.stock -= entry.getValue();
        }
        orders.add(order);
        try {
            saveData();
        } catch (IOException e) {
            orders.remove(order);
            for (Map.Entry<Integer, Integer> entry : cart.entrySet()) {
                findMedicine(entry.getKey()).stock += entry.getValue();
            }
            throw e;
        }
        cart.clear();
        printReceipt(order);
    }

    private void printReceipt(SaleOrder order) {
        System.out.println();
        System.out.println("=== HÓA ĐƠN BÁN HÀNG ===");
        System.out.println("Thời gian: " + order.time.format(DATE_TIME_FORMAT));
        System.out.println("Khách hàng: " + order.customer + " | Điện thoại: " + order.phone);
        System.out.println("Địa chỉ: " + order.address);
        for (OrderItem item : order.items) {
            System.out.printf("%s x%d - %s%n", item.name, item.quantity,
                    money(item.price.multiply(BigDecimal.valueOf(item.quantity))));
        }
        System.out.println("Tổng thanh toán: " + money(order.total()));
        System.out.println("Cảm ơn quý khách!");
    }

    private void showOrderHistory() {
        if (orders.isEmpty()) {
            System.out.println("Chưa có đơn hàng.");
            return;
        }
        for (int i = 0; i < orders.size(); i++) {
            SaleOrder order = orders.get(i);
            System.out.printf("%d. %s | %s | %s | %s%n", i + 1,
                    order.time.format(DATE_TIME_FORMAT), order.customer, order.phone, money(order.total()));
        }
    }

    private void addMedicine() throws IOException {
        int id = medicines.isEmpty() ? 1 : medicines.get(medicines.size() - 1).id + 1;
        String name = readText("Tên thuốc: ");
        String category = readText("Nhóm thuốc: ");
        BigDecimal price = readMoney("Giá bán (đ): ");
        int stock = readInt("Số lượng tồn: ", 0, Integer.MAX_VALUE);
        boolean prescription = readYesNo("Thuốc kê đơn? (c/k): ");
        LocalDate expiry = readDate("Hạn dùng (yyyy-MM-dd): ");
        medicines.add(new Medicine(id, name, category, price, stock, prescription, expiry));
        try {
            saveData();
        } catch (IOException e) {
            medicines.remove(medicines.size() - 1);
            throw e;
        }
        System.out.println("Đã thêm thuốc, mã thuốc: " + id);
    }

    private Medicine findMedicine(int id) {
        for (Medicine medicine : medicines) {
            if (medicine.id == id) {
                return medicine;
            }
        }
        return null;
    }

    private String readText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("Thông tin không được để trống.");
        }
    }

    private int readInt(String prompt, int minimum, int maximum) {
        while (true) {
            System.out.print(prompt);
            try {
                int value = Integer.parseInt(INPUT.nextLine().trim());
                if (value >= minimum && value <= maximum) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Ask again for invalid numeric input.
            }
            System.out.println("Vui lòng nhập số từ " + minimum + " đến " + maximum + ".");
        }
    }

    private BigDecimal readMoney(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                BigDecimal value = new BigDecimal(INPUT.nextLine().trim().replace(',', '.'));
                if (value.signum() > 0) {
                    return value.setScale(0, java.math.RoundingMode.HALF_UP);
                }
            } catch (NumberFormatException ignored) {
                // Ask again for invalid monetary input.
            }
            System.out.println("Vui lòng nhập số tiền lớn hơn 0.");
        }
    }

    private LocalDate readDate(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                LocalDate date = LocalDate.parse(INPUT.nextLine().trim());
                if (!date.isBefore(LocalDate.now())) {
                    return date;
                }
            } catch (java.time.format.DateTimeParseException ignored) {
                // Ask again for invalid date input.
            }
            System.out.println("Ngày không hợp lệ hoặc đã qua. Nhập theo dạng yyyy-MM-dd.");
        }
    }

    private boolean readYesNo(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = INPUT.nextLine().trim().toLowerCase(Locale.ROOT);
            if (value.equals("c") || value.equals("co") || value.equals("y")) {
                return true;
            }
            if (value.equals("k") || value.equals("khong") || value.equals("n")) {
                return false;
            }
            System.out.println("Vui lòng nhập c (có) hoặc k (không).");
        }
    }

    private String money(BigDecimal amount) {
        return CURRENCY.format(amount);
    }

    static class Medicine {
        final int id;
        final String name;
        final String category;
        final BigDecimal price;
        int stock;
        final boolean prescriptionRequired;
        final LocalDate expiry;

        Medicine(int id, String name, String category, BigDecimal price, int stock,
                 boolean prescriptionRequired, LocalDate expiry) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.prescriptionRequired = prescriptionRequired;
            this.expiry = expiry;
        }
    }

    static class SaleOrder {
        final LocalDateTime time;
        final String customer;
        final String phone;
        final String address;
        final List<OrderItem> items = new ArrayList<>();

        SaleOrder(LocalDateTime time, String customer, String phone, String address) {
            this.time = time;
            this.customer = customer;
            this.phone = phone;
            this.address = address;
        }

        BigDecimal total() {
            BigDecimal total = BigDecimal.ZERO;
            for (OrderItem item : items) {
                total = total.add(item.price.multiply(BigDecimal.valueOf(item.quantity)));
            }
            return total;
        }
    }

    static class OrderItem {
        final String name;
        final int quantity;
        final BigDecimal price;

        OrderItem(String name, int quantity, BigDecimal price) {
            this.name = name;
            this.quantity = quantity;
            this.price = price;
        }
    }
}
