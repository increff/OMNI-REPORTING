package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.model.constants.ValidationType;
import com.increff.omni.reporting.model.form.InputControlForm;
import com.nextscm.commons.spring.common.ApiException;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static com.increff.omni.reporting.helper.InputControlTestHelper.getInputControlForm;

public class InputControlDtoTest extends AbstractTest {

    @Autowired
    private InputControlDto dto;

    @Test
    public void testAddInputControl() throws ApiException {

    }
}
