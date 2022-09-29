package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.ConnectionApi;
import com.increff.omni.reporting.model.data.ConnectionData;
import com.increff.omni.reporting.model.form.ConnectionForm;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ConnectionDto extends AbstractDtoApi {

    @Autowired
    private ConnectionApi api;

    public ConnectionData add(ConnectionForm form) throws ApiException {
        checkValid(form);
        ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
        pojo = api.create(pojo);
        return ConvertUtil.convert(pojo, ConnectionData.class);
    }

    public ConnectionData update(Integer id, ConnectionForm form) throws ApiException {
        checkValid(form);
        ConnectionPojo pojo = ConvertUtil.convert(form, ConnectionPojo.class);
        pojo.setId(id);
        pojo = api.update(pojo);
        return ConvertUtil.convert(pojo, ConnectionData.class);
    }

    public List<ConnectionData> selectAll() {
        List<ConnectionPojo> pojos = api.selectAll();
        return ConvertUtil.convert(pojos, ConnectionData.class);
    }

}
