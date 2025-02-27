package com.example.module_exchange.redisData.orderBook;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class OrderBookDTO {

    @JsonProperty("output2")
    private StockPriceDTO price;

    @JsonProperty("output1")
    private OrderBookDetailDTO orderBookDetails;

}
