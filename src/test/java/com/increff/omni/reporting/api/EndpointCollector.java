package com.increff.omni.reporting.api;

import com.increff.omni.reporting.config.AbstractTest;
import com.increff.omni.reporting.security.ReportAppAccessFilter;
import com.nextscm.commons.spring.server.AbstractApi;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.HashMap;
import java.util.Map;



import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Component
public class EndpointCollector extends AbstractTest {

    @Autowired
    private RequestMappingHandlerMapping requestMappingHandlerMapping;


    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ReportAppAccessFilter reportAppAccessFilter;

    public void setup() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(reportAppAccessFilter) // Example filter
                .build();
    }

    public Map<Class<?>, Map<RequestMappingInfo, HandlerMethod>> collectEndpointsByController() {
        Map<Class<?>, Map<RequestMappingInfo, HandlerMethod>> controllerMappings = new HashMap<>();

        Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
            RequestMappingInfo mapping = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();
            Class<?> controllerClass = handlerMethod.getBeanType();

            controllerMappings.putIfAbsent(controllerClass, new HashMap<>());
            controllerMappings.get(controllerClass).put(mapping, handlerMethod);
        }

        return controllerMappings;
    }

        @Test
        public void testEndpointsByController() throws Exception {
            setup();

            Map<Class<?>, Map<RequestMappingInfo, HandlerMethod>> controllerMappings = collectEndpointsByController();

            for (Map.Entry<Class<?>, Map<RequestMappingInfo, HandlerMethod>> controllerEntry : controllerMappings.entrySet()) {
                Class<?> controllerClass = controllerEntry.getKey();
                System.out.println("Controller Class: " + controllerClass.getName());

                Map<RequestMappingInfo, HandlerMethod> mappings = controllerEntry.getValue();
                for (Map.Entry<RequestMappingInfo, HandlerMethod> mappingEntry : mappings.entrySet()) {
                    RequestMappingInfo mapping = mappingEntry.getKey();
                    System.out.println("\tMapping: " + mapping.getPatternsCondition().getPatterns());
                    HandlerMethod handlerMethod = mappingEntry.getValue();

                    Set<String> methods = mapping.getMethodsCondition().getMethods().stream().collect(Collectors.mapping(Enum::name, Collectors.toSet()));
                    for (String method : methods) {
                        String[] patterns = mapping.getPatternsCondition().getPatterns().toArray(new String[0]);
                        for (String pattern : patterns) {
                            switch (method) {
                                case "GET":
                                    mockMvc.perform(get(pattern))
                                            .andExpect(status().isOk());
                                    break;
                                case "POST":
                                    mockMvc.perform(post(pattern))
                                            .andExpect(status().isOk());
                                    break;
                                case "PUT":
                                    mockMvc.perform(put(pattern))
                                            .andExpect(status().isOk());
                                    break;
                                case "DELETE":
                                    mockMvc.perform(delete(pattern))
                                            .andExpect(status().isOk());
                                    break;
                                // Add more cases for other HTTP methods as needed
                                default:
                                    break;
                            }
                        }
                    }

//                    String[] patterns = mapping.getPatternsCondition().getPatterns().toArray(new String[0]);
//                    for (String pattern : patterns) {
//
//
//
//                        mockMvc.perform(get(pattern))
//                                .andExpect(status().isOk());
//                    }
                }
            }
        }
    }


