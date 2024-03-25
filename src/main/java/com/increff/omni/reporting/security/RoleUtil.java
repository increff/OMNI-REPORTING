package com.increff.omni.reporting.security;

import com.increff.omni.reporting.model.constants.Roles;
import lombok.extern.log4j.Log4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Log4j
@Service
public class RoleUtil {
    // intellij shows unused but is used in securityConfig.access method
    // that needs to be changed when renaming this method

    public boolean hasAdminOrStandardOrCustom(Authentication authentication) {
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        log.debug("user authorities : " + authorities);
        for (GrantedAuthority authority : authorities) {
            if (authority.getAuthority().toLowerCase().equals(Roles.APP_ADMIN.getRole()) ||
                    authority.getAuthority().toLowerCase().equals(Roles.REPORT_ADMIN.getRole()) ||
                    authority.getAuthority().toLowerCase().contains(Roles.REPORT_STANDARD.getRole()) ||
                    authority.getAuthority().toLowerCase().contains(Roles.REPORT_CUSTOM.getRole())) {
                log.debug("user role contains check success : " + authority.getAuthority());
                return true;
            }
        }
        log.debug("user role contains check failure : " + authorities);
        return false;
    }

}
