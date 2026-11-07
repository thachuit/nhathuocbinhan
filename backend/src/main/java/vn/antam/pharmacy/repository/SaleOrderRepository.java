package vn.antam.pharmacy.repository;

import vn.antam.pharmacy.model.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, String> {
    List<SaleOrder> findAllByOrderByCreatedAtDesc();
}
