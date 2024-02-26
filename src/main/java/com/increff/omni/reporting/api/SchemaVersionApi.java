package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.SchemaVersionDao;
import com.increff.omni.reporting.model.constants.AppName;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
@Transactional(rollbackFor = ApiException.class)
public class SchemaVersionApi extends AbstractApi {

    @Autowired
    private SchemaVersionDao dao;

    public SchemaVersionPojo add(SchemaVersionPojo pojo) throws ApiException {
        validate(pojo);
        dao.persist(pojo);
        return pojo;
    }

    public SchemaVersionPojo getCheck(Integer id) throws ApiException {
        SchemaVersionPojo pojo = dao.select(id);
        checkNotNull(pojo, "No schema present with id : " + id);
        return pojo;
    }

    public List<SchemaVersionPojo> selectAll() {
        return dao.selectAll();
    }

    public SchemaVersionPojo update(SchemaVersionPojo pojo) throws ApiException {
        validateForEdit(pojo);
        SchemaVersionPojo existing = getCheck(pojo.getId());
        existing.setName(pojo.getName());
        dao.update(existing);
        return existing;
    }

    public SchemaVersionPojo getByName(String name) {
        return dao.select("name", name);
    }

    public List<SchemaVersionPojo> getByIds(List<Integer> ids) {
        return dao.selectByIds(ids);
    }

    public List<SchemaVersionPojo> getByAppNames(Set<AppName> appNames) {
        return dao.selectByAppNames(appNames);
    }

    private void validateForEdit(SchemaVersionPojo pojo) throws ApiException {
        SchemaVersionPojo existing = getByName(pojo.getName());
        if(existing != null && !Objects.equals(existing.getId(), pojo.getId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Schema with same name already present");
    }

    private void validate(SchemaVersionPojo pojo) throws ApiException {
        SchemaVersionPojo existing = dao.select("name", pojo.getName());
        checkNull(existing, "Schema already present with name : " + pojo.getName());
    }
}
