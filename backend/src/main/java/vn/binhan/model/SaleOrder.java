package vn.binhan.model;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SaleOrder {
    @Id
    @Column(length = 40)
    private String id;

    @Column(nullable = false)
    private OffsetDateTime createdAt;

    @Column(nullable = false, length = 80)
    private String customerName;

    @Column(nullable = false, length = 20)
    private String customerPhone;

    @Column(nullable = false, length = 240)
    private String shippingAddress;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal total;

    @Column(nullable = false, length = 30)
    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<SaleOrderItem> items = new ArrayList<>();

    protected SaleOrder() {
    }

    public SaleOrder(String id, OffsetDateTime createdAt, String customerName, String customerPhone,
                     String shippingAddress, BigDecimal total, String status) {
        this.id = id;
        this.createdAt = createdAt;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.shippingAddress = shippingAddress;
        this.total = total;
        this.status = status;
    }

    public void addItem(SaleOrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public String getId() { return id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getShippingAddress() { return shippingAddress; }
    public BigDecimal getTotal() { return total; }
    public String getStatus() { return status; }
    public List<SaleOrderItem> getItems() { return items; }
}
