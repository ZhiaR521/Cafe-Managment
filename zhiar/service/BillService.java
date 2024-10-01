package com.zhiar.service;

import com.zhiar.POJO.Bill;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface BillService {

    ResponseEntity<String> genereteReport(Map<String,Object> requestMap);

    ResponseEntity<List<Bill>> getBills();

    ResponseEntity<String> deleteBill (Integer id);

}
