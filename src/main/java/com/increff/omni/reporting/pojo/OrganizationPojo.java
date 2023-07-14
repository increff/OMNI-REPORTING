package com.increff.omni.reporting.pojo;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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
