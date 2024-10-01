package com.zhiar.servicelmpl;

import com.zhiar.JWT.JwtFilter;
import com.zhiar.POJO.Bill;
import com.zhiar.constents.CafeConstents;
import com.zhiar.dao.BillDao;
import com.zhiar.service.BillService;
import com.zhiar.utils.CafeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@Slf4j
@Service
public class BillServiceImpl implements BillService {

    @Autowired
    BillDao billDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> genereteReport(Map<String, Object> requestMap) {
        log.info("Inside generateReport");
        try {
            String fileName;
            if (validateRequestMap(requestMap)) {
                if (requestMap.containsKey("isGenerate") && !(boolean) requestMap.get("isGenerate")) {
                    fileName = (String) requestMap.get("uuid");
                } else {
                    fileName = CafeUtils.getUUID();
                    requestMap.put("uuid", fileName);
                    insertBill(requestMap);
                }

                String data = "Name: " + requestMap.get("name") + "\n" +
                        "Contact Number: " + requestMap.get("contactNumber") + "\n" +
                        "Email: " + requestMap.get("email") + "\n" +
                        "Payment Method: " + requestMap.get("paymentMethod");

                // Send back the report data
                return CafeUtils.getResponseEntity(data, HttpStatus.OK);
            } else {
                return CafeUtils.getResponseEntity("required data not found", HttpStatus.BAD_REQUEST);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    private void insertBill(Map<String, Object> requestMap) {
        try {
            Bill bill = new Bill();
            bill.setUuid((String) requestMap.get("uuid"));
            bill.setName((String) requestMap.get("name"));
            bill.setEmail((String) requestMap.get("email"));
            bill.setContactNumber((String) requestMap.get("contactNumber"));
            bill.setPaymentMethod((String) requestMap.get("paymentMethod"));
            bill.setTotal(Integer.parseInt((String) requestMap.get("totalAmount")));
            bill.setProductDetaill((String) requestMap.get("productDetails"));
            bill.setCreatedBy(jwtFilter.getCurrentUser());
            billDao.save(bill);

        } catch (Exception ex) {
            ex.printStackTrace();
            log.error("Error while inserting bill", ex);
        }
    }
    private boolean validateRequestMap(Map<String, Object> requestMap) {
        return requestMap.containsKey("name") && requestMap.containsKey("contactNumber") &&
                requestMap.containsKey("email") && requestMap.containsKey("paymentMethod") &&
                requestMap.containsKey("productDetails") && requestMap.containsKey("totalAmount");
    }

    @Override
    public ResponseEntity<List<Bill>> getBills() {
        List<Bill> list = new ArrayList<>();
        if (jwtFilter.isAdmin()){
            list = billDao.getAllBills();

        }else {
            list = billDao.getBillByUserName(jwtFilter.getCurrentUser());
        }
        return new ResponseEntity<>(list,HttpStatus.OK);
    }

    @Override
    public ResponseEntity<String> deleteBill(Integer id) {
        try {
            Optional optional = billDao.findById(id);
            if (!optional.isEmpty()){
                billDao.deleteById(id);
                return CafeUtils.getResponseEntity("Bill deleted successfully",HttpStatus.OK);
            }else {
                return CafeUtils.getResponseEntity("Bill Id does not exist",HttpStatus.OK);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
