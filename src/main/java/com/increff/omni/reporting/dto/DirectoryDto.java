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
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getDirectoryPath;
import static com.increff.omni.reporting.dto.CommonDtoHelper.getIdToPojoMap;

@Service
public class DirectoryDto extends AbstractDtoApi {

    @Autowired
    private DirectoryApi api;

    public DirectoryData add(DirectoryForm form) throws ApiException {
        checkValid(form);
        DirectoryPojo pojo = ConvertUtil.convert(form, DirectoryPojo.class);
        pojo = api.add(pojo);
        return convertToDirectoryData(Collections.singletonList(pojo)).get(0);
    }

    public DirectoryData update(Integer id, DirectoryForm form) throws ApiException {
        checkValid(form);
        DirectoryPojo pojo = ConvertUtil.convert(form, DirectoryPojo.class);
        pojo.setId(id);
        pojo = api.update(pojo);
        return convertToDirectoryData(Collections.singletonList(pojo)).get(0);
    }

    public List<DirectoryData> getAllDirectories() throws ApiException {
        List<DirectoryPojo> directoryPojoList = api.getAll();
        return convertToDirectoryData(directoryPojoList);
    }

    private List<DirectoryData> convertToDirectoryData(List<DirectoryPojo> pojos) throws ApiException {
        List<DirectoryPojo> allPojos = api.getAll();
        Map<Integer, DirectoryPojo> idToDirectoryPojoList = getIdToPojoMap(allPojos);
        List<DirectoryData> dataList = new ArrayList<>();
        for(DirectoryPojo pojo : pojos) {
            DirectoryData data = ConvertUtil.convert(pojo, DirectoryData.class);
            data.setDirectoryPath(getDirectoryPath(pojo.getId(), idToDirectoryPojoList));
            dataList.add(data);
        }
        return dataList;
    }

}
