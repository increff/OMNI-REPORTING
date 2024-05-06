package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "favourite", uniqueConstraints = {@UniqueConstraint(columnNames = {"orgId", "userId"})})
public class FavouritePojo extends AbstractVersionedPojo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private Integer orgId;
    @Column
    private Integer userId;

    @Column(nullable = false)
    private Integer favId;

}
