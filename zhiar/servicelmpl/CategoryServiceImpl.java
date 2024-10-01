package com.zhiar.servicelmpl;

import com.google.common.base.Strings;
import com.zhiar.JWT.JwtFilter;
import com.zhiar.POJO.Category;
import com.zhiar.constents.CafeConstents;
import com.zhiar.dao.CategoryDao;
import com.zhiar.service.CategoryService;
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
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    JwtFilter jwtFilter;

    @Override
    public ResponseEntity<String> addNewCategory(Map<String, String> reqeustMap) {
        try{
            if (jwtFilter.isAdmin()){
                if (validateCategoryMap(reqeustMap,false)){
                    categoryDao.save(getCategoryFromMap(reqeustMap,false));
                    return CafeUtils.getResponseEntity("Category Added successfully",HttpStatus.OK);
                }
            }else{
                return CafeUtils.getResponseEntity(CafeConstents.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private boolean validateCategoryMap(Map<String, String> reqeustMap, boolean validateId) {
        if (reqeustMap.containsKey("name")){
            if (reqeustMap.containsKey("id")&& validateId){
                return true;
            }else if (!validateId){
                return true;
            }
        }
        return false;
    }

    private Category getCategoryFromMap(Map<String,String>requestMap,boolean isAdd){
        Category category = new Category();
        if (isAdd){
            category.setId(Integer.parseInt(requestMap.get("id")));
        }
        category.setName(requestMap.get("name"));
        return category;
    }

    @Override
    public ResponseEntity<List<Category>> getAllCategory(String filterValue) {
        try {
            if (!Strings.isNullOrEmpty(filterValue)&& filterValue.equalsIgnoreCase("true")){
                log.info("Inside if");
                return new ResponseEntity<List<Category>>(categoryDao.getAllCategory(),HttpStatus.OK);
            }
            return new ResponseEntity<>(categoryDao.findAll(),HttpStatus.OK);
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return new ResponseEntity<List<Category>>(new ArrayList<>(),HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> updateCategory(Map<String, String> requestMap) {
        try {
            if (jwtFilter.isAdmin()){
                if(validateCategoryMap(requestMap,true)){
                    Optional optional = categoryDao.findById(Integer.parseInt(requestMap.get("id")));
                    if (!optional.isEmpty()){
                        categoryDao.save(getCategoryFromMap(requestMap,true));
                        return CafeUtils.getResponseEntity("Category Updated Successfully",HttpStatus.OK);
                    }else {
                        CafeUtils.getResponseEntity("Category id does not exist",HttpStatus.OK);
                    }
                }
                return CafeUtils.getResponseEntity(CafeConstents.INVALID_DATA,HttpStatus.BAD_REQUEST);
            }else {
                return CafeUtils.getResponseEntity(CafeConstents.UNAUTHORIZED_ACCESS,HttpStatus.UNAUTHORIZED);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }
        return CafeUtils.getResponseEntity(CafeConstents.SOMETHING_WENT_WRONG,HttpStatus.INTERNAL_SERVER_ERROR);
    }
}