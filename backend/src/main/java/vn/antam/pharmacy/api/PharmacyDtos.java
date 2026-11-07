package vn.antam.pharmacy.api;

import vn.antam.pharmacy.model.Medicine;
import vn.antam.pharmacy.model.SaleOrder;
import vn.antam.pharmacy.model.SaleOrderItem;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

public final class PharmacyDtos {
    private PharmacyDtos() {
    }

    public static final class MedicineResponse {
        public final Long id;
        public final String name;
        public final String category;
        public final String description;
        public final BigDecimal price;
        public final int stock;
        public final boolean prescription;
        public final String expiry;
        public final String icon;
        public final String tone;

        public MedicineResponse(Medicine medicine) {
            this.id = medicine.getId();
            this.name = medicine.getName();
            this.category = medicine.getCategory();
            this.description = medicine.getDescription();
            this.price = medicine.getPrice();
            this.stock = medicine.getStock();
            this.prescription = medicine.isPrescription();
            this.expiry = medicine.getExpiry().toString();
            this.icon = medicine.getIcon();
            this.tone = medicine.getTone();
        }
    }

    public static final class CreateOrderRequest {
        @Valid
        public CustomerRequest customer;

        @NotEmpty(message = "Đơn hàng phải có ít nhất một sản phẩm")
        @Size(max = 50, message = "Đơn hàng tối đa 50 dòng sản phẩm")
        @Valid
        public List<OrderLineRequest> items;

        public boolean prescriptionConfirmed;
    }

    public static final class CustomerRequest {
        @NotBlank(message = "Tên khách hàng không được để trống")
        @Size(max = 80, message = "Tên khách hàng tối đa 80 ký tự")
        public String name;

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
        public String phone;

        @NotBlank(message = "Địa chỉ giao hàng không được để trống")
        @Size(max = 240, message = "Địa chỉ tối đa 240 ký tự")
        public String address;
    }

    public static final class OrderLineRequest {
        @javax.validation.constraints.NotNull(message = "Thiếu mã thuốc")
        @Min(value = 1, message = "Mã thuốc không hợp lệ")
        public Long medicineId;

        @Min(value = 1, message = "Số lượng phải lớn hơn 0")
        @Max(value = 10000, message = "Số lượng mỗi dòng tối đa 10000")
        public int quantity;
    }

    public static final class OrderResponse {
        public final String id;
        public final OffsetDateTime createdAt;
        public final List<OrderItemResponse> items = new ArrayList<>();
        public final BigDecimal total;
        public final String status;

        public OrderResponse(SaleOrder order) {
            this.id = order.getId();
            this.createdAt = order.getCreatedAt().withOffsetSameInstant(ZoneOffset.UTC);
            this.total = order.getTotal();
            this.status = order.getStatus();
            for (SaleOrderItem item : order.getItems()) {
                this.items.add(new OrderItemResponse(item));
            }
        }
    }

    public static final class OrderItemResponse {
        public final String name;
        public final int quantity;
        public final BigDecimal price;

        public OrderItemResponse(SaleOrderItem item) {
            this.name = item.getMedicineName();
            this.quantity = item.getQuantity();
            this.price = item.getUnitPrice();
        }
    }
}
