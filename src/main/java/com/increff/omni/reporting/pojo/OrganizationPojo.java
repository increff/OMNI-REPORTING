package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

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
