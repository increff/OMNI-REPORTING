package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.CustomReportAccessDao;
import com.increff.omni.reporting.pojo.CustomReportAccessPojo;
//import com.nextscm.commons.spring.server.AbstractApi;
import com.increff.omni.reporting.commons.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class CustomReportAccessApi extends AbstractApi {

    @Autowired
    private CustomReportAccessDao dao;

    public void addCustomReportAccessPojo(CustomReportAccessPojo pojo) {
        CustomReportAccessPojo ex = getByReportAndOrg(pojo.getReportId(), pojo.getOrgId());
        if (Objects.nonNull(ex))
            return;
        dao.persist(pojo);
    }

    public List<CustomReportAccessPojo> getByOrgId(Integer orgId) {
        return dao.selectMultiple("orgId", orgId);
    }

    public List<CustomReportAccessPojo> getAllByReportId(Integer reportId) {
        return dao.selectMultiple("reportId", reportId);
    }

    public CustomReportAccessPojo getByReportAndOrg(Integer reportId, Integer orgId) {
        return dao.selectByOrgAndReport(reportId, orgId);
    }

    public void deleteByReportId(Integer reportId) {
        List<CustomReportAccessPojo> pojoList = dao.selectMultiple("reportId", reportId);
        pojoList.forEach(p -> dao.remove(p));
    }

    public void deleteById(Integer id) {
        CustomReportAccessPojo pojo = dao.select(id);
        if (Objects.isNull(pojo))
            return;
        dao.remove(pojo);
    }
}
