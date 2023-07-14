package com.increff.omni.reporting.api;

import com.increff.omni.reporting.commons.AbstractApi;
//import com.nextscm.commons.spring.server.AbstractApi;
import com.nextscm.commons.spring.audit.api.AuditApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AbstractAuditApi extends AbstractApi {

    @Autowired
    private AuditApi auditApi;

    public void saveAudit(String objectId, String objectType, String action, String description, String actor) {
        try {
            // TODO: 14/07/23 check actor removal 
            auditApi.save(objectId, objectType, action, description);
        } catch (Exception e) {
            log.error("Error in adding audit log with object ID : " + objectId + ", action : " + action +
                    ", description : " + description + ", actor : " + actor);
        }
    }

}
