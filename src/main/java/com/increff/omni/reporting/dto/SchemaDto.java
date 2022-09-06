package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.SchemaApi;
import com.increff.omni.reporting.model.data.SchemaData;
import com.increff.omni.reporting.model.form.SchemaForm;
import com.increff.omni.reporting.pojo.SchemaPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SchemaDto extends AbstractDtoApi {

    @Autowired
    private SchemaApi api;

    public SchemaData add(SchemaForm form) throws ApiException {
        checkValid(form);
        SchemaPojo pojo = ConvertUtil.convert(form, SchemaPojo.class);
        api.add(pojo);
        return ConvertUtil.convert(pojo, SchemaData.class);
    }

    public List<SchemaData> selectAll(){
        List<SchemaPojo> pojos = api.selectAll();
        return ConvertUtil.convert(pojos, SchemaData.class);
    }

}
