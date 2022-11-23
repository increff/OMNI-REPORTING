package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.ReportQueryForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class ReportQueryData extends ReportQueryForm {
    private ZonedDateTime updatedAt;
}
