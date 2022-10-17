package com.increff.omni.reporting.config;

import com.increff.account.client.UserPrincipal;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@ContextConfiguration(classes = {TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
public abstract class AbstractTest {


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        setSecurityContext();
    }

    private void setSecurityContext() {
        Authentication authentication = Mockito.mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(1);
        principal.setDomainName("increff");
        principal.setId(1);
        principal.setUsername("test_user");
        principal.setAppName("saas");
        principal.setEmail("test_email@increff.com");
        principal.setCountry("India");
        principal.setFullName("TEST USER");
        principal.setRoles(Collections.singletonList("app.admin"));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }
}