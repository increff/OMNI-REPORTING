package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.DashboardForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class DashboardListData extends DashboardForm {

    private Integer id;
    private ZonedDateTime createdAt;
    private ZonedDateTime updatedAt;
}
