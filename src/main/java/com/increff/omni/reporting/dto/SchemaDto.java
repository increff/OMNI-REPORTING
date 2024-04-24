package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.SchemaVersionApi;
import com.increff.omni.reporting.model.data.SchemaVersionData;
import com.increff.omni.reporting.model.form.SchemaVersionForm;
import com.increff.omni.reporting.pojo.SchemaVersionPojo;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ConvertUtil;
import com.increff.commons.springboot.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SchemaDto extends AbstractDtoApi {

    @Autowired
    private SchemaVersionApi api;

    public SchemaVersionData add(SchemaVersionForm form) throws ApiException {
        checkValid(form);
        SchemaVersionPojo pojo = ConvertUtil.convert(form, SchemaVersionPojo.class);
        api.add(pojo);
        return ConvertUtil.convert(pojo, SchemaVersionData.class);
    }

    public SchemaVersionData update(Integer id, SchemaVersionForm form) throws ApiException {
        checkValid(form);
        SchemaVersionPojo pojo = ConvertUtil.convert(form, SchemaVersionPojo.class);
        pojo.setId(id);
        pojo = api.update(pojo);
        return ConvertUtil.convert(pojo, SchemaVersionData.class);
    }

    public List<SchemaVersionData> selectAll(){
        List<SchemaVersionPojo> pojos = api.selectAll();
        pojos.sort(Comparator.comparing(SchemaVersionPojo::getAppName));
        return ConvertUtil.convert(pojos, SchemaVersionData.class);
    }

}
