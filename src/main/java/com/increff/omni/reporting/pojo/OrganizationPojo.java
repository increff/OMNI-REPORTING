package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

//import javax.persistence.Column;
//import javax.persistence.Entity;
//import javax.persistence.Id;
//import javax.persistence.Table;
import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "organization")
public class OrganizationPojo extends AbstractVersionedPojo {


    @Id
    private Integer id;

    @Column(nullable = false, unique = true)
    private String name;
}
