package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.InputControlDao;
import com.increff.omni.reporting.dao.InputControlQueryDao;
import com.increff.omni.reporting.dao.InputControlValuesDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQuery;
import com.increff.omni.reporting.pojo.InputControlValues;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(rollbackFor = Exception.class)
public class InputControlApi extends AbstractApi {

    @Autowired
    private InputControlDao dao;

    @Autowired
    private InputControlQueryDao queryDao;

    @Autowired
    private InputControlValuesDao valuesDao;


    public InputControlPojo add(InputControlPojo pojo, InputControlQuery queryPojo,
                                List<InputControlValues> valuesPojo) throws ApiException {

        validateControlAddition(pojo);
        dao.persist(pojo);

        if(queryPojo != null){
            queryPojo.setControlId(pojo.getId());
            queryDao.persist(queryPojo);
        }

        if(!CollectionUtils.isEmpty(valuesPojo)){
            valuesPojo.forEach(v -> {
                v.setControlId(pojo.getId());
                valuesDao.persist(v);
            });
        }
        return pojo;
    }

    public List<InputControlPojo> selectMultiple(List<Integer> ids){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.selectMultiple(ids);
    }

    public List<InputControlPojo> getByScope(InputControlScope scope){
        return dao.selectMultiple("scope",scope);
    }

    public InputControlPojo getByScopeAndDisplayName(InputControlScope scope, String displayName){
        return null;
    }

    public InputControlPojo getByScopeAndParamName(InputControlScope scope, String displayName){
        return null;
    }

    public InputControlPojo getCheck(Integer id) throws ApiException {
        InputControlPojo pojo = dao.select(id);
        checkNotNull(pojo, "No control present for id : " + id);
        return pojo;
    }

    public List<InputControlQuery> selectControlQueries(List<Integer> controlIds){
        if(CollectionUtils.isEmpty(controlIds))
            return new ArrayList<>();
        return queryDao.selectMultiple(controlIds);
    }

    public List<InputControlValues> selectControlValues(List<Integer> controlIds){
        if(CollectionUtils.isEmpty(controlIds))
            return new ArrayList<>();
        return valuesDao.selectMultiple(controlIds);
    }

    private void validateControlAddition(InputControlPojo pojo) throws ApiException {
        InputControlPojo existingByName =
                getByScopeAndDisplayName(InputControlScope.GLOBAL, pojo.getDisplayName());

        InputControlPojo existingByParam =
                getByScopeAndParamName(InputControlScope.GLOBAL, pojo.getParamName());

        if(existingByName != null || existingByParam != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Cannot create input control with same" +
                    " display name or param name");
    }

}
