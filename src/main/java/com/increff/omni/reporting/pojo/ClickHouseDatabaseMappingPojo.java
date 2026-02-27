package com.increff.omni.reporting.pojo;

import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
import lombok.Getter;
import lombok.Setter;

import jakarta.persistence.*;

@Entity
@Setter
@Getter
@Table(name = "clickHouseDatabaseMapping", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"connectionId", "databaseName"})
})
public class ClickHouseDatabaseMappingPojo extends AbstractVersionedPojo {

    @Id
    @TableGenerator(name = "click_house_database_mapping", pkColumnValue = "click_house_database_mapping", initialValue = 100000)
    @GeneratedValue(strategy = GenerationType.TABLE, generator = "click_house_database_mapping")
    private Integer id;
    @Column(nullable = false)
    private Integer connectionId;
    @Column(nullable = false)
    private String databaseName;

}
