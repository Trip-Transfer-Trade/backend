package com.example.module_exchange.redisData.orderBook;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stocks")
public class OrderBookController {

    private final OrderBookService orderBookService;

    public OrderBookController(OrderBookService orderBookService) {
        this.orderBookService = orderBookService;
    }

    @GetMapping("/{code}")
    public OrderBookDTO getOrderBook(@PathVariable String code) {
        return orderBookService.getOrderBook(code);
    }
}
