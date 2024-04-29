package com.increff.omni.reporting.pojo;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "favourite", uniqueConstraints = {@UniqueConstraint(columnNames = {"orgId", "userId"})})
public class FavouritePojo extends AbstractVersionedPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false)
    private Integer orgId;
    @Column
    private Integer userId;

    @Column(nullable = false)
    private Integer favId;

}
