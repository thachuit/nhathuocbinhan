package vn.antam.binhan.repository;

import vn.antam.binhan.model.SaleOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleOrderRepository extends JpaRepository<SaleOrder, String> {
    List<SaleOrder> findAllByOrderByCreatedAtDesc();
}
