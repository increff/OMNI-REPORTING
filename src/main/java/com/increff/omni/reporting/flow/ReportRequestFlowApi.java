package com.increff.omni.reporting.flow;

import com.increff.commons.queryexecutor.data.QueryRequestData;
import com.increff.commons.queryexecutor.form.GetRequestForm;
import com.increff.omni.reporting.api.*;
import com.increff.omni.reporting.pojo.*;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.increff.omni.reporting.dto.CommonDtoHelper.getStatusMapping;

@Service
public class ReportRequestFlowApi extends AbstractFlowApi {

    @Autowired
    private ReportRequestApi api;
    @Autowired
    private ReportApi reportApi;
    @Autowired
    private InputControlApi controlApi;
    @Autowired
    private ReportControlsApi reportControlsApi;
    @Autowired
    private ReportInputParamsApi reportInputParamsApi;
    @Autowired
    private ConnectionApi connectionApi;
    @Autowired
    private OrgConnectionApi orgConnectionApi;
    @Autowired
    private ReportValidationGroupApi reportValidationGroupApi;
    @Autowired
    private QueryExecutorClientApi executorClientApi;

    private final static Integer MAX_OPEN_REPORT_REQUESTS = 5;

    @Transactional(rollbackFor = ApiException.class)
    public void requestReport(ReportRequestPojo pojo, List<ReportInputParamsPojo> reportInputParamsPojoList)
            throws ApiException {
        List<ReportRequestPojo> pendingReports = api.getPendingByUserId(pojo.getUserId());
        if (!CollectionUtils.isEmpty(pendingReports) && pendingReports.size() >= MAX_OPEN_REPORT_REQUESTS)
            throw new ApiException(ApiStatus.BAD_DATA, "Wait for existing reports to get executed");
        ReportPojo reportPojo = reportApi.getCheck(pojo.getReportId());
        validate(reportPojo, reportInputParamsPojoList);
        requestReportWithoutValidation(pojo, reportInputParamsPojoList);
    }

    @Transactional(rollbackFor = ApiException.class)
    public void requestReportWithoutValidation(ReportRequestPojo pojo,
                                               List<ReportInputParamsPojo> reportInputParamsPojoList) {
        api.add(pojo);
        reportInputParamsPojoList.forEach(r -> r.setReportRequestId(pojo.getId()));
        reportInputParamsApi.add(reportInputParamsPojoList);
    }

    public void updatePendingRequestStatus(List<Integer> pendingRequestIds,
                                           List<ReportRequestPojo> reportRequestPojoList,
                                           int userId,
                                           Map<Integer, Integer> referenceIdToSequenceNumber) throws ApiException {
        if(pendingRequestIds.isEmpty())
            return;
        GetRequestForm form = new GetRequestForm();
        List<Long> requestIds = pendingRequestIds.stream().map(Long::valueOf).collect(Collectors.toList());
        form.setReferenceIds(requestIds);
        form.setUserId(userId);
        List<QueryRequestData> data = executorClientApi.getQueryRequestDataList(form);
        for(QueryRequestData d : data) {
            Optional<ReportRequestPojo> requestPojo =
                    reportRequestPojoList.stream().filter(r -> r.getId().equals(d.getReferenceId().intValue())).findFirst();
            if(requestPojo.isPresent()) {
                // This happens separately in a separate transaction
                api.updateStatus(requestPojo.get().getId(), getStatusMapping(d.getStatus()),
                        requestPojo.get().getUrl(), d.getNoOfRows(), d.getFileSize(), d.getFailureReason(), d.getUpdatedAt());
                referenceIdToSequenceNumber.put(requestPojo.get().getId(), d.getSequenceNumber());
            }
        }
    }
}
