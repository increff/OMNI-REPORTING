package com.increff.omni.reporting.dto;

import com.increff.omni.reporting.model.data.OrgConnectionData;
import com.increff.omni.reporting.model.data.OrgSchemaData;
import com.increff.omni.reporting.pojo.ConnectionPojo;
import com.increff.omni.reporting.pojo.OrgConnectionPojo;
import com.increff.omni.reporting.pojo.OrgSchemaPojo;
import com.increff.omni.reporting.pojo.SchemaPojo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OrganizationDtoHelper {

    public static List<OrgSchemaData> getOrgSchemaDataList(List<OrgSchemaPojo> pojos, List<SchemaPojo> allPojos) {
        Map<Integer, SchemaPojo> idToPojoMap = new HashMap<>();
        allPojos.forEach(a -> idToPojoMap.put(a.getId(), a));
        return pojos.stream().map(p -> {
            SchemaPojo pojo = idToPojoMap.get(p.getSchemaId());
            return getOrgSchemaData(p, pojo);
        }).collect(Collectors.toList());
    }

    public static List<OrgConnectionData> getOrgConnectionDataList(List<OrgConnectionPojo> pojos, List<ConnectionPojo> allPojos) {
        Map<Integer, ConnectionPojo> idToPojoMap = new HashMap<>();
        allPojos.forEach(a -> idToPojoMap.put(a.getId(), a));
        return pojos.stream().map(p -> {
            ConnectionPojo pojo = idToPojoMap.get(p.getConnectionId());
            return getOrgConnectionData(p, pojo);
        }).collect(Collectors.toList());
    }

    public static OrgSchemaData getOrgSchemaData(OrgSchemaPojo pojo, SchemaPojo schemaPojo) {
        OrgSchemaData data = new OrgSchemaData();
        data.setOrgId(pojo.getOrgId());
        data.setSchemaId(pojo.getSchemaId());
        data.setSchemaName(schemaPojo.getName());
        return data;
    }

    public static OrgConnectionData getOrgConnectionData(OrgConnectionPojo pojo, ConnectionPojo connectionPojo) {
        OrgConnectionData data = new OrgConnectionData();
        data.setOrgId(pojo.getOrgId());
        data.setConnectionId(pojo.getConnectionId());
        data.setConnectionName(connectionPojo.getName());
        return data;
    }
}
