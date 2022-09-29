package com.increff.omni.reporting.dto;

import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import com.nextscm.commons.spring.server.DtoHelper;

import java.util.Collection;

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
