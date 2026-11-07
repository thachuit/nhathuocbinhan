package vn.binhan.repository;

import vn.binhan.model.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, String> {
    List<SaleOrder> findAllByOrderByCreatedAtDesc();
}
