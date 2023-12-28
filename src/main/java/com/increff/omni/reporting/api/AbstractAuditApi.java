package com.increff.omni.reporting.api;

import com.increff.commons.springboot.audit.api.AuditApi;
import com.increff.commons.springboot.server.AbstractApi;
import lombok.extern.log4j.Log4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j
@Service
public class AbstractAuditApi extends AbstractApi {

    @Autowired
    private AuditApi auditApi;

    public void saveAudit(String objectId, String objectType, String action, String description, String actor) {
        try {
            auditApi.save(objectId, objectType, action, description, actor);
        } catch (Exception e) {
            log.error("Error in adding audit log with object ID : " + objectId + ", action : " + action +
                    ", description : " + description + ", actor : " + actor);
        }
    }

}
