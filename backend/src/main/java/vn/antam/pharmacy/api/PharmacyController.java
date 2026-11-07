package vn.antam.pharmacy.api;

import vn.antam.pharmacy.api.PharmacyDtos.CreateOrderRequest;
import vn.antam.pharmacy.api.PharmacyDtos.MedicineResponse;
import vn.antam.pharmacy.api.PharmacyDtos.OrderResponse;
import vn.antam.pharmacy.service.PharmacyService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api")
public class PharmacyController {
    private final PharmacyService pharmacyService;

    public PharmacyController(PharmacyService pharmacyService) {
        this.pharmacyService = pharmacyService;
    }

    @GetMapping("/medicines")
    public List<MedicineResponse> getMedicines() {
        return pharmacyService.getMedicines();
    }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders() {
        return pharmacyService.getOrders();
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return pharmacyService.createOrder(request);
    }
}
