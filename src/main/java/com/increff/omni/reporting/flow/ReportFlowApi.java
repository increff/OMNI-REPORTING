package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.model.constants.InputControlScope;
import com.increff.omni.reporting.model.constants.ReportType;
import com.increff.omni.reporting.pojo.*;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.server.AbstractApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.helper.FlowApiHelper.validateValidationType;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportFlowApi extends AbstractApi {

    @Autowired
    private SchemaApi schemaApi;

    @Autowired
    private OrgSchemaApi orgSchemaApi;

    @Autowired
    private DirectoryApi directoryApi;

    @Autowired
    private CustomReportAccessApi customReportAccessApi;

    @Autowired
    private ReportApi api;

    @Autowired
    private ReportQueryApi queryApi;

    @Autowired
    private ReportControlsApi reportControlsApi;

    @Autowired
    private InputControlApi inputControlApi;

    public ReportPojo addReport(ReportPojo pojo) throws ApiException {
        validate(pojo);
        return api.add(pojo);
    }

    public ReportPojo editReport(ReportPojo pojo) throws ApiException {
        validateForEdit(pojo);
        return api.edit(pojo);
    }

    public ReportQueryPojo upsertQuery(ReportQueryPojo pojo) throws ApiException {
        api.getCheck(pojo.getId());
        return queryApi.upsertQuery(pojo);
    }

    public void mapControlToReport(ReportControlsPojo pojo) throws ApiException {
        validateControlReportMapping(pojo);
        reportControlsApi.add(pojo);
    }

    public List<ReportPojo> getAll(Integer orgId) throws ApiException {

        OrgSchemaPojo orgSchemaPojo = orgSchemaApi.getCheckByOrgId(orgId);
        //All standard
        List<ReportPojo> standard = api.getByTypeAndSchema(ReportType.STANDARD, orgSchemaPojo.getSchemaId());

        //All custom
        List<CustomReportAccessPojo> customAccess = customReportAccessApi.getByOrgId(orgId);
        List<Integer> customIds = customAccess.stream().map(CustomReportAccessPojo::getReportId).collect(Collectors.toList());

        List<ReportPojo> custom = api.getByIdsAndSchema(customIds, orgSchemaPojo.getSchemaId());

        //combined 2 list
        standard.addAll(custom);
        return standard;
    }

    private void validateControlReportMapping(ReportControlsPojo pojo) throws ApiException {
        //valid report
        api.getCheck(pojo.getReportId());
        //valid control - Global
        InputControlPojo icPojo = inputControlApi.getCheck(pojo.getControlId());
        validateValidationType(icPojo.getType(), pojo.getValidationType());
        if(icPojo.getScope().equals(InputControlScope.LOCAL))
            throw new ApiException(ApiStatus.BAD_DATA, "Only Global Control can be mapped to a report");
    }

    private void validateForEdit(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaApi.getCheck(pojo.getSchemaId());

        ReportPojo existing = api.getCheck(pojo.getId());

        //validating if requested name is already present
        if(!pojo.getName().equals(existing.getName())){
            ReportPojo existingByName = api.getByName(pojo.getName());
            if(existingByName != null)
                throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name");
        }
    }

    private void validate(ReportPojo pojo) throws ApiException {
        directoryApi.getCheck(pojo.getDirectoryId());
        schemaApi.getCheck(pojo.getSchemaId());

        //get all
        ReportPojo existing = api.getByName(pojo.getName());
        if(existing != null)
            throw new ApiException(ApiStatus.BAD_DATA, "Report already present with same name");

    }

}
