package com.schwarz.finance.indicators.controller;

import com.schwarz.finance.indicators.service.FinanceServiceInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class FinanceController {

    private final FinanceServiceInterface financeServiceInterface;

    @Autowired
    public FinanceController(FinanceServiceInterface financeServiceInterface) {
        this.financeServiceInterface = financeServiceInterface;
    }


    @GetMapping(
            value = "/getdata",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<Object> getData() {
        TreeMap<Integer, Double> data = new TreeMap<>();
        Map<Integer,Map<String, Double>> result = new HashMap<>();

        // fill data
        Integer firstTimestamp = Math.toIntExact(System.currentTimeMillis() / 1000);
        data.put(firstTimestamp, Math.random() * 5);
        for (int i = 1; i <= 99; i++) {
            Integer timestamp = Math.toIntExact((System.currentTimeMillis() / 1000) + i);
            data.put(timestamp, Math.random() * 5);
        }
        
        for (Map.Entry<Integer, Double> entry : data.entrySet()) {
            Integer timestamp = entry.getKey();
            Double value = entry.getValue();
            Map <String, Double> outputMap = new HashMap<>();

            SortedMap<Integer, Double> subMap = data.subMap(firstTimestamp, timestamp);


            FinanceServiceInterface financeService = financeServiceInterface.createInstance(subMap);
            Double ewm = financeService.ewm(26.0, true, 26);
            Double macd = financeService.MACD(12, 26);
            Double macdSignal = financeService.signal(9);
            Double histogram = financeService.histogram();

            outputMap.put("Last Data:", value);
            outputMap.put("ewm(26)", ewm);
            outputMap.put("macd(12,26,9)", macd);
            outputMap.put("macdSignal(9)", macdSignal);
            outputMap.put("histogram", histogram);

            result.put(timestamp, outputMap);
        }

        return new ResponseEntity<Object>(result, HttpStatus.OK);
    }


    @GetMapping("/")
    ResponseEntity<String> test() {
        return new ResponseEntity<String>("It's all right!", HttpStatus.OK);
    }
}
