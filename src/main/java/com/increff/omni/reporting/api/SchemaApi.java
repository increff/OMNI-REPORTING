package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.SchemaDao;
import com.increff.omni.reporting.dto.SchemaDto;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.omni.reporting.pojo.SchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class SchemaApi extends AbstractApi {

    @Autowired
    private SchemaDao dao;

    public SchemaPojo add(SchemaPojo pojo) throws ApiException {
        validate(pojo);
        dao.persist(pojo);
        return pojo;
    }

    public SchemaPojo getCheck(Integer id) throws ApiException {
        SchemaPojo pojo = dao.select(id);
        checkNotNull(pojo, "No schema present with id : " + id);
        return pojo;
    }

    public List<SchemaPojo> selectAll() {
        return dao.selectAll();
    }

    public SchemaPojo update(SchemaPojo pojo) throws ApiException {
        validateForEdit(pojo);
        SchemaPojo existing = getCheck(pojo.getId());
        existing.setName(pojo.getName());
        dao.update(existing);
        return existing;
    }

    private void validateForEdit(SchemaPojo pojo) throws ApiException {
        SchemaPojo existing = dao.select("name", pojo.getName());
        if(existing != null && !Objects.equals(existing.getId(), pojo.getId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Schema with same name already present");
    }

    private void validate(SchemaPojo pojo) throws ApiException {
        SchemaPojo existing = dao.select("name", pojo.getName());
        checkNull(existing, "Schema already present with name : " + pojo.getName());
    }
}
