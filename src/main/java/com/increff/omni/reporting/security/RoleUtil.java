package com.increff.omni.reporting.security;

import com.increff.omni.reporting.model.constants.Roles;
import lombok.extern.log4j.Log4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

@Log4j
@Service
public class RoleUtil {
    // intellij shows unused but is used in securityConfig.access method
    // that needs to be changed when renaming this method

    public boolean hasAdminOrStandardOrCustom(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        List<String> allowedRoles = Arrays.asList(
                Roles.APP_ADMIN.getRole(), Roles.REPORT_ADMIN.getRole(), Roles.REPORT_STANDARD.getRole(), Roles.REPORT_CUSTOM.getRole());
        log.debug("user authorities : " + authorities);
        for (GrantedAuthority authority : authorities) {
            if(allowedRoles.contains(authority.getAuthority().toLowerCase())){
                log.debug("user role contains check success : " + authority.getAuthority());
                return true; // true if any allowed roles are substring of any authority in user authorities
            }
        }
        log.debug("user role contains check failure : " + authorities);
        return false;
    }

}
