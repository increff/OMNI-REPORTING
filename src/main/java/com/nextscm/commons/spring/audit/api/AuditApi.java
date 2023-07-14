package com.nextscm.commons.spring.audit.api;

import com.nextscm.commons.spring.audit.dao.DaoProvider;
import com.nextscm.commons.spring.audit.model.AuditData;
import com.nextscm.commons.spring.audit.model.UserPrincipal;
import com.nextscm.commons.spring.audit.pojo.AuditPojo;
import com.nextscm.commons.spring.common.ApiException;
import com.nextscm.commons.spring.common.ApiStatus;
import com.nextscm.commons.spring.common.ConvertUtil;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class AuditApi {

    private DaoProvider provider;

    public void setProvider(DaoProvider provider){
        this.provider = provider;
    }

    @Transactional
    public void save(String objectId, String objectType , String action, String description) {
        AuditPojo auditPojo = new AuditPojo();
        auditPojo.setAction(action.toLowerCase());
        auditPojo.setObjectType(objectType.toLowerCase());
        auditPojo.setTimestamp(new Date());
        auditPojo.setObjectId(objectId);
        auditPojo.setDescription(description);
        auditPojo.setActor(getUserName());
        provider.save(auditPojo);
    }

    //To be used with applications that are using auth-client
    @Transactional
    public String getUserName(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null){
            return "ANONYMOUS";
        }
        Object principal = authentication.getPrincipal();
        UserPrincipal userPrincipal = ConvertUtil.convert(principal, UserPrincipal.class);
        return  userPrincipal.getUsername();
    }

    //To be used in case actor value to be explicitly passed
    @Transactional
    public void save(String objectId, String objectType , String action, String description, String actor) {
        AuditPojo auditPojo = new AuditPojo();
        auditPojo.setAction(action.toLowerCase());
        auditPojo.setObjectType(objectType.toLowerCase());
        auditPojo.setTimestamp(new Date());
        auditPojo.setObjectId(objectId);
        auditPojo.setDescription(description);
        auditPojo.setActor(actor);
        provider.save(auditPojo);
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<AuditPojo> getAuditLogsByTime(Date startTimestamp, Date endTimestamp) throws ApiException {
        if(startTimestamp == null && endTimestamp == null){
            throw new ApiException(ApiStatus.BAD_DATA, "Neither startDate nor endDate is present");
        }
        if(startTimestamp.compareTo(endTimestamp)>0){
            throw new ApiException(ApiStatus.BAD_DATA,"StartDate is ahead of endDate");
        }
        if(endTimestamp == null){
            endTimestamp = new Date();
        }
        if (startTimestamp == null){
            startTimestamp = new Date(1); //epoch Time
        }
        List<AuditPojo> auditPojoList = provider.getAuditLogsByTime(startTimestamp, endTimestamp);
        if (auditPojoList.size() == 0) {
            throw new ApiException(ApiStatus.NOT_FOUND, "No Data present between " + startTimestamp +
                    " and " + endTimestamp);
        }
        return auditPojoList;
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<AuditPojo> getAuditLogsByActor(String actor) throws ApiException {
        List<AuditPojo> auditPojoList = provider.selectMultiple("actor", actor);
        if (auditPojoList.size() == 0) {
            throw new ApiException(ApiStatus.NOT_FOUND, "No Data present by actor: " + actor);
        }
        return auditPojoList;
    }

    @Transactional(rollbackFor = ApiException.class)
    public List<AuditPojo> getAuditLogsByAction(String action) throws ApiException {
        List<AuditPojo> auditPojoList = provider.selectMultiple("action", action);
        if (auditPojoList.size() == 0) {
            throw new ApiException(ApiStatus.NOT_FOUND, "No Data present by action: " + action);
        }
        return auditPojoList;
    }
    @Transactional(rollbackFor = ApiException.class)
    public List<AuditData> getAuditLogsByIdAndType(String objectId, String objectType) throws ApiException {
        List<AuditPojo> auditPojoList = provider.selectByIdAndType(objectId, objectType);
        if (auditPojoList.size() == 0) {
            throw new ApiException(ApiStatus.NOT_FOUND, "No Data present for the combination of Id: " + objectId+" and objectType: "+objectType);
        }

        return ConvertUtil.convert(auditPojoList, AuditData.class);
    }
}
