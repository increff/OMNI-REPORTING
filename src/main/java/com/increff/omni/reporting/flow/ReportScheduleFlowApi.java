package com.increff.omni.reporting.flow;

import com.increff.omni.reporting.api.AbstractAuditApi;
import com.increff.omni.reporting.api.ReportScheduleApi;
import com.increff.omni.reporting.pojo.ReportScheduleEmailsPojo;
import com.increff.omni.reporting.pojo.ReportSchedulePojo;
import com.nextscm.commons.lang.StringUtil;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.JsonUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Service
@Transactional(rollbackFor = ApiException.class)
public class ReportScheduleFlowApi extends AbstractAuditApi {

    @Autowired
    private ReportScheduleApi reportScheduleApi;

    private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public void add(ReportSchedulePojo pojo, List<String> sendTo) throws ApiException {
        reportScheduleApi.add(pojo);
        addEmails(pojo, sendTo);
    }

    public void edit(ReportSchedulePojo pojo, List<String> sendTo) throws ApiException {
        reportScheduleApi.edit(pojo);
        reportScheduleApi.removeExistingEmails(pojo.getId());
        addEmails(pojo, sendTo);
    }

    public void editEnableOrDeletedFlag(ReportSchedulePojo pojo) throws ApiException {
        reportScheduleApi.edit(pojo);
    }

    private void addEmails(ReportSchedulePojo pojo, List<String> sendTo) throws ApiException {
        List<ReportScheduleEmailsPojo> emailsPojos = new ArrayList<>();
        sendTo.forEach(e -> {
            if(!StringUtil.isEmpty(e) && VALID_EMAIL_ADDRESS_REGEX.matcher(e).find()) {
                ReportScheduleEmailsPojo emailsPojo = new ReportScheduleEmailsPojo();
                emailsPojo.setSendTo(e);
                emailsPojo.setScheduleId(pojo.getId());
                emailsPojos.add(emailsPojo);
            }
        });
        if(emailsPojos.isEmpty())
            throw new ApiException(ApiStatus.BAD_DATA, "No valid emails given, " + JsonUtil.serialize(sendTo));
        reportScheduleApi.addEmails(emailsPojos);
    }
}
