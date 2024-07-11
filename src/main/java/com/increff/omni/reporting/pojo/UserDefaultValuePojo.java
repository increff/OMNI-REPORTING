//package com.increff.omni.reporting.pojo;
//
//import com.increff.commons.springboot.db.pojo.AbstractVersionedPojo;
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.Setter;
//
//@Entity
//@Getter
//@Setter
//@Table(name = "user_default_value", indexes = {@Index(name = "idx_dashboard_id_user_id_control_param_name", columnList = "dashboardId, userId, paramName")})
//public class UserDefaultValuePojo extends AbstractVersionedPojo{
//    @Id
//    @TableGenerator(name = "user_default_value", pkColumnValue = "user_default_value")
//    @GeneratedValue(strategy = GenerationType.TABLE, generator = "user_default_value")
//    private Integer id;
//    @Column(nullable = false)
//    private Integer userId;
//    @Column(nullable = false)
//    private Integer dashboardId;
//    @Column(nullable = false)
//    private String paramName;
//    @Column(nullable = false, columnDefinition = "LONGTEXT")
//    private String defaultValue;
//}
