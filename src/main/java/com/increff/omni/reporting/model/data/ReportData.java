package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.ReportForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class ReportData extends ReportForm {

    private Integer id;
    private ZonedDateTime updatedAt;
    private String schemaVersionName;
    private String directoryName;
    private String directoryPath;

}
