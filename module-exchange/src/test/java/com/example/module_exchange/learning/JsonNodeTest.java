package com.example.module_exchange.learning;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

//@SpringBootTest
public class JsonNodeTest {

//    @Autowired
    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void jsonNodeTest() throws JsonProcessingException {
        String json = "{\n" +
                "    \"status\": 200,\n" +
                "    \"message\": \"success\",\n" +
                "    \"data\": {\n" +
                "        \"output1\": {\n" +
                "            \"last\": \"239.0700\",\n" +
                "            \"base\": \"239.0700\"\n" +
                "        },\n" +
                "        \"output2\": {\n" +
                "            \"paskPrices\": {\n" +
                "                \"pask1\": \"0.0000\",\n" +
                "                \"pask2\": \"0.0000\",\n" +
                "                \"pask3\": \"0.0000\",\n" +
                "                \"pask4\": \"0.0000\",\n" +
                "                \"pask10\": \"0.0000\",\n" +
                "                \"pask5\": \"0.0000\",\n" +
                "                \"pask6\": \"0.0000\",\n" +
                "                \"pask7\": \"0.0000\",\n" +
                "                \"pask8\": \"0.0000\",\n" +
                "                \"pask9\": \"0.0000\"\n" +
                "            },\n" +
                "            \"pbidPrices\": {\n" +
                "                \"pbid5\": \"0.0000\",\n" +
                "                \"pbid6\": \"0.0000\",\n" +
                "                \"pbid7\": \"0.0000\",\n" +
                "                \"pbid8\": \"0.0000\",\n" +
                "                \"pbid9\": \"0.0000\",\n" +
                "                \"pbid10\": \"0.0000\",\n" +
                "                \"pbid1\": \"0.0000\",\n" +
                "                \"pbid2\": \"0.0000\",\n" +
                "                \"pbid3\": \"0.0000\",\n" +
                "                \"pbid4\": \"0.0000\"\n" +
                "            },\n" +
                "            \"vaskQuantities\": {\n" +
                "                \"vask1\": \"0\",\n" +
                "                \"vask2\": \"0\",\n" +
                "                \"vask7\": \"0\",\n" +
                "                \"vask8\": \"0\",\n" +
                "                \"vask9\": \"0\",\n" +
                "                \"vask10\": \"0\",\n" +
                "                \"vask3\": \"0\",\n" +
                "                \"vask4\": \"0\",\n" +
                "                \"vask5\": \"0\",\n" +
                "                \"vask6\": \"0\"\n" +
                "            },\n" +
                "            \"vbidQuantities\": {\n" +
                "                \"vbid3\": \"0\",\n" +
                "                \"vbid4\": \"0\",\n" +
                "                \"vbid5\": \"0\",\n" +
                "                \"vbid6\": \"0\",\n" +
                "                \"vbid10\": \"0\",\n" +
                "                \"vbid1\": \"0\",\n" +
                "                \"vbid2\": \"0\",\n" +
                "                \"vbid7\": \"0\",\n" +
                "                \"vbid8\": \"0\",\n" +
                "                \"vbid9\": \"0\"\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "}";

        JsonNode jsonNode = objectMapper.readTree(json);
        String currencyPrice = jsonNode.path("data").path("output1").path("last").asText();
        System.out.println(currencyPrice);

    }
}
