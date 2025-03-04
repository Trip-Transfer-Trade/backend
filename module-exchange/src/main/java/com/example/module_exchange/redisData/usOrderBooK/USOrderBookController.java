package com.example.module_exchange.redisData.usOrderBooK;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/exchanges/us")
public class USOrderBookController {

    private final USOrderBookService usOrderBookService;

    public USOrderBookController(USOrderBookService usOrderBookService) {
        this.usOrderBookService = usOrderBookService;
    }

    @GetMapping("/{code}")
    public USOrderBookDTO getOrderBook(@PathVariable String code) {
        return usOrderBookService.getUSOrderBook(code);
    }
}
