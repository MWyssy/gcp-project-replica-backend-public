package com.northcoders.customer;

import com.northcoders.jwt.JWTUtil;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;

import java.util.List;

@RestController
@RequestMapping("api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final JWTUtil jwtUtil;
    private final MeterRegistry meterRegistry;
    private final Counter newCustomersCounter;
    private final Counter deletedCustomersCounter;
    private final Gauge customersGauge;

    public CustomerController(CustomerService customerService,
            JWTUtil jwtUtil,
            MeterRegistry meterRegistry) {
        this.customerService = customerService;
        this.jwtUtil = jwtUtil;
        this.meterRegistry = meterRegistry;
        this.newCustomersCounter = Counter.builder("customapp.new_customers_counter")
                .description("Number of new customers added")
                .register(meterRegistry);
        this.deletedCustomersCounter = Counter.builder("customapp.deleted_customers_counter")
                .description("Number of customers deleted")
                .register(meterRegistry);
        this.customersGauge = Gauge.builder("customapp.customers_gauge", this, CustomerController::getCustomerCount)
                .description("Number of customers")
                .register(meterRegistry);
    }

    private static double getCustomerCount(CustomerController controller) {
        return controller.customerService.getAllCustomers().size();
    }

    @GetMapping
    public List<CustomerDTO> getCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("{customerId}")
    public CustomerDTO getCustomer(
            @PathVariable("customerId") Integer customerId) {
        return customerService.getCustomer(customerId);
    }

    @PostMapping
    public ResponseEntity<?> registerCustomer(
            @RequestBody CustomerRegistrationRequest request) {
        customerService.addCustomer(request);
        newCustomersCounter.increment();
        String jwtToken = jwtUtil.issueToken(request.email(), "ROLE_USER");
        return ResponseEntity.ok()
                .header(HttpHeaders.AUTHORIZATION, jwtToken)
                .build();
    }

    @DeleteMapping("{customerId}")
    public void deleteCustomer(
            @PathVariable("customerId") Integer customerId) {
        customerService.deleteCustomerById(customerId);
        deletedCustomersCounter.increment();
    }

    @PutMapping("{customerId}")
    public void updateCustomer(
            @PathVariable("customerId") Integer customerId,
            @RequestBody CustomerUpdateRequest updateRequest) {
        customerService.updateCustomer(customerId, updateRequest);
    }

}
