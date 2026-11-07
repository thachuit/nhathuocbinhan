package vn.binhan.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "medicines")
public class Medicine {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 100)
    private String category;

    @Column(nullable = false, length = 200)
    private String description;

    @Column(nullable = false, precision = 14, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock;

    @Column(nullable = false)
    private boolean prescription;

    @Column(nullable = false)
    private LocalDate expiry;

    @Column(nullable = false, length = 16)
    private String icon;

    @Column(nullable = false, length = 16)
    private String tone;

    protected Medicine() {
    }

    public Medicine(String name, String category, String description, BigDecimal price, int stock,
                    boolean prescription, LocalDate expiry, String icon, String tone) {
        this.name = name;
        this.category = category;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.prescription = prescription;
        this.expiry = expiry;
        this.icon = icon;
        this.tone = tone;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public int getStock() { return stock; }
    public boolean isPrescription() { return prescription; }
    public LocalDate getExpiry() { return expiry; }
    public String getIcon() { return icon; }
    public String getTone() { return tone; }
    public void setStock(int stock) { this.stock = stock; }
}
