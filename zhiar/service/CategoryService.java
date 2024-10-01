package com.zhiar.service;

import com.zhiar.POJO.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CategoryService {

    ResponseEntity<String> addNewCategory(Map<String,String> reqeustMap);

    ResponseEntity<List<Category>> getAllCategory(String filterValue);

    ResponseEntity<String>updateCategory(Map<String,String>requestMap);

}
