package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.ConnectionDao;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class ConnectionApi extends AbstractAuditApi {

    @Autowired
    private ConnectionDao dao;

    public ConnectionPojo add(ConnectionPojo pojo) throws ApiException {
        validate(pojo);
        dao.persist(pojo);
        return pojo;
    }

    public ConnectionPojo update(ConnectionPojo pojo) throws ApiException {
        validateForEdit(pojo);
        ConnectionPojo existing = getCheck(pojo.getId());
        copyToExisting(pojo, existing);
        dao.update(existing);
        return existing;
    }

    public List<ConnectionPojo> selectAll(){
        return dao.selectAll();
    }

    public ConnectionPojo getCheck(Integer id) throws ApiException {
        ConnectionPojo pojo = dao.select(id);
        checkNotNull(pojo, "No Connection present with id : " + id);
        return pojo;
    }

    public ConnectionPojo getByName(String name) {
        return dao.select("name", name);
    }

    private void validateForEdit(ConnectionPojo pojo) throws ApiException {
        ConnectionPojo existing = getByName(pojo.getName());
        if(existing != null && !Objects.equals(existing.getId(), pojo.getId()))
            throw new ApiException(ApiStatus.BAD_DATA, "Connection with same name already present");
    }

    private void validate(ConnectionPojo pojo) throws ApiException {
        ConnectionPojo existing = dao.select("name", pojo.getName());
        checkNull(existing, "Connection with same name already present");
    }

    private static void copyToExisting(ConnectionPojo pojo, ConnectionPojo existing) {
        existing.setName(pojo.getName());
        existing.setHost(pojo.getHost());
        existing.setUsername(pojo.getUsername());
        existing.setPassword(pojo.getPassword());
    }

}
