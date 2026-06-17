package com.dyxia.nexuserp.controller;

import com.dyxia.nexuserp.model.TimeTracking;
import com.dyxia.nexuserp.service.TimeCalculationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.UUID;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class TimeTrackingControllerSecurityTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private TimeCalculationService timeCalculationService;

    @BeforeEach
    public void setup() {
        this.mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "employee@nexus.com", roles = {"EMPLOYEE"})
    public void testOverrideEndpointForbiddenForEmployee() throws Exception {
        UUID trackingId = UUID.randomUUID();
        System.out.println("SIMULATION RBAC: Attempt override as EMPLOYEE -> Expecting 403 Forbidden");
        mockMvc.perform(post("/api/v1/tracking/override/" + trackingId))
                .andExpect(status().isForbidden());
        System.out.println("SIMULATION RBAC: Attempt override as EMPLOYEE -> Got 403 Forbidden [SUCCESS]");
    }

    @Test
    @WithMockUser(username = "hr@nexus.com", roles = {"HR_ADMIN"})
    public void testOverrideEndpointAllowedForHr() throws Exception {
        UUID trackingId = UUID.randomUUID();
        Mockito.when(timeCalculationService.overrideLatePunchIn(trackingId))
                .thenReturn(new TimeTracking());

        System.out.println("SIMULATION RBAC: Attempt override as HR_ADMIN -> Expecting 200 OK");
        mockMvc.perform(post("/api/v1/tracking/override/" + trackingId))
                .andExpect(status().isOk());
        System.out.println("SIMULATION RBAC: Attempt override as HR_ADMIN -> Got 200 OK [SUCCESS]");
    }
}
