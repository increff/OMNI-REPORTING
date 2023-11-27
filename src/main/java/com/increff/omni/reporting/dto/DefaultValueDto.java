package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DashboardApi;
import com.increff.omni.reporting.api.DashboardChartApi;
import com.increff.omni.reporting.api.DefaultValueApi;
import com.increff.omni.reporting.model.data.DefaultValueData;
import com.increff.omni.reporting.model.data.InputControlData;
import com.increff.omni.reporting.model.form.DefaultValueForm;
import com.increff.omni.reporting.pojo.DefaultValuePojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import io.swagger.models.auth.In;
import lombok.Setter;
import lombok.extern.log4j.Log4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@Log4j
@Setter
public class DefaultValueDto extends AbstractDto {



}
