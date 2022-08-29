package com.increff.omni.reporting.api;

import com.increff.omni.reporting.constants.InputControlScope;
import com.increff.omni.reporting.dao.InputControlDao;
import com.increff.omni.reporting.pojo.InputControlPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@Transactional(rollbackFor = ApiException.class)
public class InputControlApi extends AbstractApi {

    @Autowired
    private InputControlDao dao;

    public InputControlPojo add(InputControlPojo pojo){
        dao.persist(pojo);
        return pojo;
    }

    public List<InputControlPojo> selectMultiple(List<Integer> ids){
        if(CollectionUtils.isEmpty(ids))
            return new ArrayList<>();
        return dao.selectMultiple(ids);
    }

    public InputControlPojo getByScopeAndDisplayName(InputControlScope scope, String displayName){
        return null;
    }

    public InputControlPojo getByScopeAndParamName(InputControlScope scope, String displayName){
        return null;
    }

}
