package com.example.module_exchange.redisData.usOrderBooK;

import com.example.module_utility.response.Response;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Response<USOrderBookDTO>> getOrderBook(@PathVariable String code) {
        USOrderBookDTO response = usOrderBookService.getUSOrderBook(code);
        return ResponseEntity.ok(Response.success(response));
    }
}
