package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.api.DirectoryApi;
import com.increff.omni.reporting.model.data.DirectoryData;
import com.increff.omni.reporting.model.form.DirectoryForm;
import com.increff.omni.reporting.pojo.DirectoryPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ConvertUtil;
import com.nextscm.commons.spring.server.AbstractDtoApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DirectoryDto extends AbstractDtoApi {

    @Autowired
    private DirectoryApi api;

    public DirectoryData add(DirectoryForm form) throws ApiException {
        checkValid(form);
        DirectoryPojo pojo = ConvertUtil.convert(form, DirectoryPojo.class);
        pojo = api.add(pojo);
        return ConvertUtil.convert(pojo, DirectoryData.class);
    }

    public DirectoryData update(Integer id, DirectoryForm form) throws ApiException {
        checkValid(form);
        DirectoryPojo pojo = ConvertUtil.convert(form, DirectoryPojo.class);
        pojo.setId(id);
        pojo = api.update(pojo);
        return ConvertUtil.convert(pojo, DirectoryData.class);
    }

    public List<DirectoryData> getAllDirectories() {
        List<DirectoryPojo> directoryPojoList = api.getAll();
        List<DirectoryData> dataList = new ArrayList<>();
        directoryPojoList.forEach(p -> dataList.add(ConvertUtil.convert(p, DirectoryData.class)));
        return dataList;
    }

}
