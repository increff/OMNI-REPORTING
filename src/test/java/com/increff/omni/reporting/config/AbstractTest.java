package com.increff.omni.reporting.config;

import com.increff.account.client.UserPrincipal;
import com.increff.omni.reporting.model.constants.AppResourceKeys;
import com.increff.omni.reporting.model.constants.ChartType;
import com.increff.omni.reporting.model.constants.PipelineType;
import com.increff.omni.reporting.model.form.PipelineForm;
import com.increff.service.encryption.EncryptionClient;
import com.increff.service.encryption.data.CryptoData;
import com.increff.service.encryption.form.CryptoForm;
import com.nextscm.commons.spring.client.AppClientException;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@ContextConfiguration(classes = {TestConfig.class})
@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@Transactional
public abstract class AbstractTest {

    public Integer orgId = 100001;

    @Value("${testdb.username}")
    protected String username;

    @Value("${testdb.password}")
    protected String password;

    @Mock
    protected EncryptionClient encryptionClient;

    @Before
    public void setUp() throws AppClientException {
        MockitoAnnotations.initMocks(this);
        setSecurityContext();
        Mockito.when(encryptionClient.encode(Mockito.any(CryptoForm.class))).thenReturn(getCryptoData());
        Mockito.when(encryptionClient.decode(Mockito.any())).thenReturn(getDecryptedCryptoData());
    }

    private CryptoData getCryptoData() {
        CryptoData cryptoData = new CryptoData();
        cryptoData.setValue("UUID");
        return cryptoData;
    }

    private CryptoData getDecryptedCryptoData() {
        CryptoData cryptoData = new CryptoData();
        cryptoData.setValue("nextscm@fashion");
        return cryptoData;
    }

    private void setSecurityContext() {
        Authentication authentication = Mockito.mock(Authentication.class);
        UserPrincipal principal = new UserPrincipal();
        principal.setDomainId(orgId);
        principal.setDomainName("increff");
        principal.setId(100001);
        principal.setUsername("test_user");
        principal.setAppName("saas");
        principal.setEmail("test_email@increff.com");
        principal.setCountry("India");
        principal.setFullName("TEST USER");
        Map<String, Map<String, List<String>>> resourceRoles = new HashMap<>();
        Map<String, List<String>> fulfillmentLocationResourceMap = new HashMap<>();
        fulfillmentLocationResourceMap.put("w1", new ArrayList<>());
        fulfillmentLocationResourceMap.put("w2", new ArrayList<>());
        resourceRoles.put(AppResourceKeys.fulfillmentLocationKey, fulfillmentLocationResourceMap);
        resourceRoles.put(AppResourceKeys.clientKey, fulfillmentLocationResourceMap);
        resourceRoles.put(AppResourceKeys.restrictedResourceKey, fulfillmentLocationResourceMap);
        principal.setResourceRoles(resourceRoles);
        principal.setRoles(Arrays.asList("app.admin", "report.admin"));
        SecurityContext securityContext = Mockito.mock(SecurityContext.class, Mockito.withSettings().serializable());
        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(securityContext.getAuthentication().getPrincipal()).thenReturn(principal);
        SecurityContextHolder.setContext(securityContext);
    }
}