package com.zhiar.utils;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;


public class CafeUtils {
    private CafeUtils() {}

    public static ResponseEntity<String> getResponseEntity(String responseMessage, HttpStatus httpStatus) {
        return ResponseEntity
                .status(httpStatus)
                .body("{\"Message\":\"" + responseMessage + "\"}");
    }

    public static String getUUID(){
        Date date = new Date();
        long time = date.getTime();
        return "BILL-"+time;
    }
}
