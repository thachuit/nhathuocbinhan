package vn.antam.binhan.service;

import vn.antam.binhan.api.StoreDtos.CreateOrderRequest;
import vn.antam.binhan.api.StoreDtos.CustomerRequest;
import vn.antam.binhan.api.StoreDtos.OrderLineRequest;
import vn.antam.binhan.model.Medicine;
import vn.antam.binhan.model.SaleOrder;
import vn.antam.binhan.repository.MedicineRepository;
import vn.antam.binhan.repository.SaleOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StoreServiceTest {
    @Mock
    private MedicineRepository medicineRepository;
    @Mock
    private SaleOrderRepository orderRepository;

    private StoreService service;
    private Medicine medicine;

    @BeforeEach
    void setUp() throws Exception {
        service = new StoreService(medicineRepository, orderRepository);
        medicine = new Medicine("Vitamin C", "Vitamin", "Tuýp 10 viên",
                new BigDecimal("42000"), 5, false, LocalDate.of(2099, 12, 31),
                "☼", "peach");
        Field id = Medicine.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(medicine, 1L);
    }

    @Test
    void createsOrderAndDecreasesStock() {
        when(medicineRepository.findAllByIdForUpdate(Collections.singletonList(1L)))
                .thenReturn(Collections.singletonList(medicine));
        when(orderRepository.save(any(SaleOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        CreateOrderRequest request = request(2, false);

        var response = service.createOrder(request);

        assertEquals(3, medicine.getStock());
        assertEquals(new BigDecimal("84000"), response.total);
        assertEquals(1, response.items.size());
        assertEquals("Vitamin C", response.items.get(0).name);
        verify(orderRepository).save(any(SaleOrder.class));
    }

    @Test
    void rejectsOrderWhenStockIsInsufficient() {
        when(medicineRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(Collections.singletonList(medicine));

        ApiException error = assertThrows(ApiException.class,
                () -> service.createOrder(request(6, false)));

        assertEquals(HttpStatus.CONFLICT, error.getStatus());
        assertEquals(5, medicine.getStock());
        verify(orderRepository, never()).save(any(SaleOrder.class));
    }

    @Test
    void requiresPrescriptionConfirmation() throws Exception {
        Medicine prescriptionMedicine = new Medicine("Amoxicillin", "Kháng sinh", "Hộp 10 viên",
                new BigDecimal("65000"), 4, true, LocalDate.of(2099, 12, 31),
                "✚", "peach");
        Field id = Medicine.class.getDeclaredField("id");
        id.setAccessible(true);
        id.set(prescriptionMedicine, 2L);
        when(medicineRepository.findAllByIdForUpdate(anyList()))
                .thenReturn(Collections.singletonList(prescriptionMedicine));

        ApiException error = assertThrows(ApiException.class,
                () -> service.createOrder(request(1, false, 2L)));

        assertEquals(HttpStatus.BAD_REQUEST, error.getStatus());
        assertEquals(4, prescriptionMedicine.getStock());
        verify(orderRepository, never()).save(any(SaleOrder.class));
    }

    private CreateOrderRequest request(int quantity, boolean prescriptionConfirmed) {
        return request(quantity, prescriptionConfirmed, 1L);
    }

    private CreateOrderRequest request(int quantity, boolean prescriptionConfirmed, Long medicineId) {
        CreateOrderRequest request = new CreateOrderRequest();
        request.customer = new CustomerRequest();
        request.customer.name = "Nguyen Van A";
        request.customer.phone = "0900000000";
        request.customer.address = "Quan 1";
        OrderLineRequest line = new OrderLineRequest();
        line.medicineId = medicineId;
        line.quantity = quantity;
        request.items = Collections.singletonList(line);
        request.prescriptionConfirmed = prescriptionConfirmed;
        return request;
    }
}
