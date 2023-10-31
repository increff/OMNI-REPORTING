package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DashboardDao;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(rollbackFor = Exception.class)
public class DashboardApi extends AbstractApi {

    @Autowired
    private DashboardDao dao;

    public DashboardPojo add(DashboardPojo pojo) throws ApiException {
        dao.persist(pojo);
        return pojo;
    }

    public DashboardPojo getCheck(Integer id, Integer orgId) throws ApiException {
        DashboardPojo pojo = getCheck(id);
        if(!pojo.getOrgId().equals(orgId))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard does not belong to orgId: " + orgId);
        return pojo;
    }

    private DashboardPojo getCheck(Integer id) throws ApiException {
        DashboardPojo pojo = dao.getCheck(id);
        checkNotNull(pojo, "Dashboard does not exist id: " + id);
        return pojo;
    }
}
