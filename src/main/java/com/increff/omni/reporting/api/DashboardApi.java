package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.DashboardDao;
import com.increff.omni.reporting.pojo.DashboardPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.commons.springboot.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = ApiException.class)
public class DashboardApi extends AbstractApi {

    @Autowired
    private DashboardDao dao;

    public DashboardPojo add(DashboardPojo pojo) throws ApiException {
        DashboardPojo existing = getByOrgIdName(pojo.getOrgId(), pojo.getName());
        if(Objects.nonNull(existing))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard already exists with name: " + pojo.getName() + " for orgId: " + pojo.getOrgId());
        dao.persist(pojo);
        return pojo;
    }

    public DashboardPojo update(Integer id, DashboardPojo updated) throws ApiException {
        DashboardPojo existing = getCheck(id);
        existing.setName(updated.getName());
        return existing;
    }

    public void delete(Integer id) throws ApiException {
        DashboardPojo pojo = getCheck(id);
        dao.remove(pojo);
    }

    public DashboardPojo getCheck(Integer id, Integer orgId) throws ApiException {
        DashboardPojo pojo = getCheck(id);
        if(!pojo.getOrgId().equals(orgId))
            throw new ApiException(ApiStatus.BAD_DATA, "Dashboard does not belong to orgId: " + orgId);
        return pojo;
    }

    public List<DashboardPojo> getByOrgId(Integer orgId) {
        return dao.getByOrgId(orgId);
    }
    public DashboardPojo getByOrgIdName(Integer orgId, String name) {
        return dao.getByOrgIdName(orgId, name);
    }

    private DashboardPojo getCheck(Integer id) throws ApiException {
        DashboardPojo pojo = dao.select(id);
        checkNotNull(pojo, "Dashboard does not exist id: " + id);
        return pojo;
    }
}
