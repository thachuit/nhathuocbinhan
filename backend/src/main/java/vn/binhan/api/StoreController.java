package vn.binhan.api;

import vn.binhan.api.StoreDtos.CreateOrderRequest;
import vn.binhan.api.StoreDtos.MedicineResponse;
import vn.binhan.api.StoreDtos.OrderResponse;
import vn.binhan.service.StoreService;
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
public class StoreController {
    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    @GetMapping("/medicines")
    public List<MedicineResponse> getMedicines() {
        return storeService.getMedicines();
    }

    @GetMapping("/orders")
    public List<OrderResponse> getOrders() {
        return storeService.getOrders();
    }

    @PostMapping("/orders")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return storeService.createOrder(request);
    }
}
