package com.increff.omni.reporting.model.data;

import com.increff.omni.reporting.model.form.DirectoryForm;
import lombok.Getter;
import lombok.Setter;

import java.time.ZonedDateTime;

@Setter
@Getter
public class DirectoryData extends DirectoryForm {

    private Integer id;
    private ZonedDateTime updatedAt;
    private String directoryPath;

}
