package com.increff.omni.reporting.api;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class ControlValidationApi {


}
