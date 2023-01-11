package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.OrganizationDao;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class OrganizationApi extends AbstractApi {

    @Autowired
    private OrganizationDao dao;

    public OrganizationPojo add(OrganizationPojo pojo) throws ApiException {
        validate(pojo);
        dao.persist(pojo);
        return pojo;
    }

    public OrganizationPojo getCheck(Integer id) throws ApiException {
        OrganizationPojo pojo = dao.select(id);
        checkNotNull(pojo, "No org present with id : " + id);
        return pojo;
    }

    public OrganizationPojo update(OrganizationPojo pojo) throws ApiException {
        // validating
        OrganizationPojo existing = getCheck(pojo.getId());
        OrganizationPojo existingWithName = dao.select("name", pojo.getName());
        checkNull(existingWithName, "Organization already present with requested name");
        existing.setName(pojo.getName());
        dao.update(existing);
        return existing;
    }

    public List<OrganizationPojo> getAll() {
        return dao.selectAll();
    }

    public OrganizationPojo getByName(String name) {
        return dao.select("name", name);
    }

    private void validate(OrganizationPojo pojo) throws ApiException {
        OrganizationPojo existing = dao.select(pojo.getId());
        checkNull(existing, "Organization already present with requested id");

        existing = getByName(pojo.getName());
        checkNull(existing, "Organization already present with requested name");
    }


}
