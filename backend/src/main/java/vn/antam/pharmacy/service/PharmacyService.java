package vn.antam.pharmacy.service;

import vn.antam.pharmacy.api.PharmacyDtos.CreateOrderRequest;
import vn.antam.pharmacy.api.PharmacyDtos.MedicineResponse;
import vn.antam.pharmacy.api.PharmacyDtos.OrderLineRequest;
import vn.antam.pharmacy.api.PharmacyDtos.OrderResponse;
import vn.antam.pharmacy.model.Medicine;
import vn.antam.pharmacy.model.SaleOrder;
import vn.antam.pharmacy.model.SaleOrderItem;
import vn.antam.pharmacy.repository.MedicineRepository;
import vn.antam.pharmacy.repository.SaleOrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PharmacyService {
    private final MedicineRepository medicineRepository;
    private final SaleOrderRepository orderRepository;

    public PharmacyService(MedicineRepository medicineRepository, SaleOrderRepository orderRepository) {
        this.medicineRepository = medicineRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional(readOnly = true)
    public List<MedicineResponse> getMedicines() {
        return medicineRepository.findAll().stream()
                .map(MedicineResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getOrders() {
        return orderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OrderResponse::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {
        if (request.customer == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Thiếu thông tin người nhận hàng");
        }

        Map<Long, Integer> quantities = new LinkedHashMap<>();
        for (OrderLineRequest line : request.items) {
            if (line.medicineId == null || line.quantity < 1) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Thông tin sản phẩm không hợp lệ");
            }
            int current = quantities.containsKey(line.medicineId) ? quantities.get(line.medicineId) : 0;
            if (line.quantity > Integer.MAX_VALUE - current) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "Số lượng sản phẩm vượt giới hạn");
            }
            quantities.put(line.medicineId, current + line.quantity);
        }

        List<Long> medicineIds = new ArrayList<>(quantities.keySet());
        List<Medicine> lockedMedicines = medicineRepository.findAllByIdForUpdate(medicineIds);
        Map<Long, Medicine> byId = lockedMedicines.stream()
                .collect(Collectors.toMap(Medicine::getId, medicine -> medicine));

        if (byId.size() != medicineIds.size()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "Có sản phẩm không còn tồn tại");
        }

        boolean hasPrescriptionMedicine = false;
        BigDecimal total = BigDecimal.ZERO;
        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Medicine medicine = byId.get(entry.getKey());
            if (medicine.getExpiry().isBefore(LocalDate.now(ZoneOffset.UTC))) {
                throw new ApiException(HttpStatus.CONFLICT, medicine.getName() + " đã hết hạn");
            }
            if (medicine.getStock() < entry.getValue()) {
                throw new ApiException(HttpStatus.CONFLICT, medicine.getName() + " không đủ tồn kho");
            }
            hasPrescriptionMedicine |= medicine.isPrescription();
            total = total.add(medicine.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
        }

        if (hasPrescriptionMedicine && !request.prescriptionConfirmed) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Cần xác nhận khách hàng đã cung cấp đơn thuốc hợp lệ");
        }

        String id = "AT-" + UUID.randomUUID().toString().replace("-", "")
                .substring(0, 12).toUpperCase();
        SaleOrder order = new SaleOrder(id, OffsetDateTime.now(ZoneOffset.UTC),
                request.customer.name.trim(), request.customer.phone.trim(),
                request.customer.address.trim(), total, "Đang chuẩn bị");

        for (Map.Entry<Long, Integer> entry : quantities.entrySet()) {
            Medicine medicine = byId.get(entry.getKey());
            int quantity = entry.getValue();
            medicine.setStock(medicine.getStock() - quantity);
            order.addItem(new SaleOrderItem(medicine.getName(), quantity, medicine.getPrice()));
        }

        return new OrderResponse(orderRepository.save(order));
    }
}
