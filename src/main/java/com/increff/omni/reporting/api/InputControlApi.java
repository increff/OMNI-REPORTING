package com.increff.omni.reporting.api;

import com.increff.omni.reporting.dao.InputControlDao;
import com.increff.omni.reporting.dao.InputControlQueryDao;
import com.increff.omni.reporting.dao.InputControlValuesDao;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.InputControlType;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.increff.omni.reporting.pojo.InputControlQueryPojo;
import com.increff.omni.reporting.pojo.InputControlValuesPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Transactional(rollbackFor = Exception.class)
public class InputControlApi extends AbstractApi {

    @Autowired
    private InputControlDao dao;

    @Autowired
    private InputControlQueryDao queryDao;

    @Autowired
    private InputControlValuesDao valuesDao;


    public InputControlPojo add(InputControlPojo pojo, InputControlQueryPojo queryPojo,
                                List<InputControlValuesPojo> valuesPojo) throws ApiException {
        validateControlAddition(pojo);
        dao.persist(pojo);
        addQueryOrValues(queryPojo, pojo, valuesPojo);
        return pojo;
    }

    public InputControlPojo update(InputControlPojo pojo, InputControlQueryPojo queryPojo,
                                   List<InputControlValuesPojo> valuesList) throws ApiException {
        InputControlPojo ex = getCheck(pojo.getId());
        validateControlAdditionForEdit(pojo);
        validateTypeTransition(ex, pojo);
        copyToExisting(ex, pojo);
        dao.update(ex);
        InputControlQueryPojo exQuery = selectControlQuery(pojo.getId());
        if (Objects.nonNull(exQuery)) {
            queryDao.remove(exQuery.getId());
            // flush is required as in single transaction batch processing, delete and insert immediately fails sometimes
            queryDao.flush();
        }
        List<InputControlValuesPojo> valuesPojoList = valuesDao.selectMultiple("controlId", pojo.getId());
        valuesPojoList.forEach(v -> valuesDao.remove(v.getId()));
        // flush is required as in single transaction batch processing, delete and insert immediately fails sometimes
        valuesDao.flush();
        addQueryOrValues(queryPojo, pojo, valuesList);
        return pojo;
    }

    public List<InputControlPojo> selectByIds(List<Integer> ids) {
        if (CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.selectMultiple(ids);
    }

    public List<InputControlPojo> getByScope(InputControlScope scope) {
        return dao.selectMultiple("scope", scope);
    }

    public InputControlPojo getByScopeAndDisplayName(InputControlScope scope, String displayName) {
        return dao.selectByScopeAndDisplayName(scope, displayName);
    }

    public InputControlPojo getByScopeAndParamName(InputControlScope scope, String paramName) {
        return dao.selectByScopeAndParamName(scope, paramName);
    }

    public InputControlPojo getCheck(Integer id) throws ApiException {
        InputControlPojo pojo = dao.select(id);
        checkNotNull(pojo, "No control present for id : " + id);
        return pojo;
    }

    public List<InputControlQueryPojo> selectControlQueries(List<Integer> controlIds) {
        if (CollectionUtils.isEmpty(controlIds))
            return new ArrayList<>();
        return queryDao.selectMultiple(controlIds);
    }

    public InputControlQueryPojo selectControlQuery(Integer controlId) {
        return queryDao.select("controlId", controlId);
    }

    public List<InputControlValuesPojo> selectControlValues(List<Integer> controlIds) {
        if (CollectionUtils.isEmpty(controlIds))
            return new ArrayList<>();
        return valuesDao.selectMultiple(controlIds);
    }

    private void validateControlAddition(InputControlPojo pojo) throws ApiException {
        InputControlPojo existingByName = getByScopeAndDisplayName(InputControlScope.GLOBAL, pojo.getDisplayName());

        InputControlPojo existingByParam = getByScopeAndParamName(InputControlScope.GLOBAL, pojo.getParamName());

        if (existingByName != null || existingByParam != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Cannot create input control with same" + " display name or param name");
    }

    private void addQueryOrValues(InputControlQueryPojo queryPojo, InputControlPojo pojo, List<InputControlValuesPojo> valuesList) {
        if (Objects.nonNull(queryPojo)) {
            queryPojo.setControlId(pojo.getId());
            queryDao.persist(queryPojo);
        }

        if (!CollectionUtils.isEmpty(valuesList)) {
            valuesList.forEach(v -> {
                v.setControlId(pojo.getId());
                valuesDao.persist(v);
            });
        }
    }

    private void validateControlAdditionForEdit(InputControlPojo pojo) throws ApiException {
        InputControlPojo existingByName = getByScopeAndDisplayName(InputControlScope.GLOBAL, pojo.getDisplayName());

        InputControlPojo existingByParam = getByScopeAndParamName(InputControlScope.GLOBAL, pojo.getParamName());

        if ((existingByName != null && !existingByName.getId().equals(pojo.getId())) || (existingByParam != null && !existingByParam.getId().equals(pojo.getId())))
            throw new ApiException(ApiStatus.BAD_DATA, "Cannot create input control with same" + " display name or param name");
    }

    private void validateTypeTransition(InputControlPojo ex, InputControlPojo pojo) throws ApiException {
        if(ex.getType().equals(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT) &&
                !pojo.getType().equals(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT)) {
            throw new ApiException(ApiStatus.BAD_DATA, "Access controlled multi select can't be updated to any other " +
                    "type");
        } else if(pojo.getType().equals(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT) &&
                !ex.getType().equals(InputControlType.ACCESS_CONTROLLED_MULTI_SELECT)) {
            throw new ApiException(ApiStatus.BAD_DATA, "No other control can be migrated to access controlled multi " +
                    "select");
        }
    }

    private void copyToExisting(InputControlPojo ex, InputControlPojo pojo) {
        ex.setType(pojo.getType());
        ex.setParamName(pojo.getParamName());
        ex.setDisplayName(pojo.getDisplayName());
    }
}
