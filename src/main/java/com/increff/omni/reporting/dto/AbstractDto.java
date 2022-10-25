package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.mysql.jdbc.Driver;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import com.nextscm.commons.spring.server.DtoHelper;
import lombok.extern.log4j.Log4j;

import java.sql.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Log4j
public abstract class AbstractDto extends AbstractDtoApi {

    protected static int getOrgId() {
        return getPrincipal().getDomainId();
    }

    protected static int getUserId() {
        return getPrincipal().getId();
    }

    private static UserPrincipal getPrincipal() {
        return SecurityUtil.getPrincipal();
    }
}
