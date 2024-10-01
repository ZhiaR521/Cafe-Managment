package com.zhiar.POJO;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import java.io.Serializable;

@NamedQuery(name = "Category.getAllCategory",
        query = "SELECT c FROM Category c WHERE c.id IN (SELECT p.category.id FROM Product p WHERE p.status = 'true')")
@Entity
@Table(name = "category")
@Data
@DynamicInsert
@DynamicUpdate
public class Category implements Serializable {

    private static final Long SerialVersionUID=1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @Column(name = "name")
    private String name;

}
