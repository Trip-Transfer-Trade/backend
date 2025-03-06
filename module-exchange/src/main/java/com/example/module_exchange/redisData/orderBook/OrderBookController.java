package com.example.module_exchange.redisData.orderBook;

import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchanges")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<Response<OrderBookDTO>> getOrderBook(@PathVariable String code) {
        OrderBookDTO response = orderBookService.getOrderBook(code);
        return ResponseEntity.ok(Response.success(response));
    }
}
