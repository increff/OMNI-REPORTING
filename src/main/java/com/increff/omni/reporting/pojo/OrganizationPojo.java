package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "organization")
public class OrganizationPojo extends AbstractVersionedPojo{


    @Id
    private Integer id;

    @Column(nullable = false)
    private String name;
}
