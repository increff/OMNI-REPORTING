package com.increff.omni.reporting.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.increff.account.client.SecurityUtil;
import com.increff.account.client.UserPrincipal;
import com.increff.commons.jwt.JwtUtil;
import com.increff.commons.springboot.common.ApiException;
import com.increff.commons.springboot.common.ApiStatus;
import com.increff.omni.reporting.pojo.OrganizationPojo;
import com.increff.omni.reporting.util.EncryptionDecryptionUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Log4j2
@Component
public class EmbedDataApi {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private OrganizationApi organizationApi;

    @Autowired
    private EncryptionDecryptionUtil encryptionUtil;

    private final ObjectMapper objectMapper = new ObjectMapper();


    public String generateEmbedToken(String encryptedCredentials) throws ApiException {
        try {
            String decryptedJson = encryptionUtil.decrypt(encryptedCredentials);
            Map<String, Object> credentials = objectMapper.readValue(
                decryptedJson,
                new TypeReference<Map<String, Object>>() {}
            );
            String userId = (String) credentials.get("userId");
            String orgName = (String) credentials.get("orgName");

            if (userId == null || orgName == null) {
                throw new ApiException(ApiStatus.BAD_DATA,
                    "userId and orgName are required in encrypted payload");
            }

            OrganizationPojo organizationPojo = organizationApi.getByName(orgName);
            if (organizationPojo == null) {
                throw new ApiException(ApiStatus.BAD_DATA,
                    "Organization not found with name: " + orgName);
            }

            String jwtToken = jwtUtil.generateToken(userId, "", orgName);
            return encryptionUtil.encrypt(jwtToken);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to generate embed token", e);
            throw new ApiException(ApiStatus.UNKNOWN_ERROR,
                "Failed to generate embed token: " + e.getMessage());
        }
    }

    public Map<String, Object> verifyTokenAndGetContext() throws ApiException {
        try {
            UserPrincipal principal = SecurityUtil.getPrincipal();

            if (principal == null) {
                throw new ApiException(ApiStatus.BAD_DATA, "No authenticated user found");
            }

            String userId = principal.getUsername();
            String orgName = principal.getFullName();
            String email = principal.getEmail();
            OrganizationPojo org = organizationApi.getByName(orgName);
            if (org == null) {
                throw new ApiException(ApiStatus.BAD_DATA,
                    "Organization not found: " + orgName);
            }

            Map<String, Object> context = new HashMap<>();
            context.put("userId", userId);
            context.put("orgName", orgName);
            context.put("orgId", org.getId());
            context.put("email", email != null ? email : "");
            context.put("status", "authenticated");
            context.put("authenticatedAt", System.currentTimeMillis());

            return context;

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying token context", e);
            throw new ApiException(ApiStatus.UNKNOWN_ERROR, "Failed to verify token: " + e.getMessage());
        }
    }
}