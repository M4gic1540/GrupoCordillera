package com.main.authservice.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.main.authservice.config.ExternalIntegrationsProperties;
import com.main.authservice.external.ConnectorFactory;
import com.main.authservice.external.ErpConnector;
import com.main.authservice.external.ExternalConnector;
import com.main.authservice.external.PosConnector;
import com.main.authservice.model.Role;
import com.main.authservice.model.User;

class ExternalIntegrationFactoryMethodTest {

    @Test
    void factoryShouldCreatePosConnector() {
        ExternalConnector connector = ConnectorFactory.createConnector("POS");
        assertInstanceOf(PosConnector.class, connector);
    }

    @Test
    void factoryShouldCreateErpConnector() {
        ExternalConnector connector = ConnectorFactory.createConnector("ERP");
        assertInstanceOf(ErpConnector.class, connector);
    }

    @Test
    void factoryShouldCreateConnectorCaseInsensitiveForPos() {
        ExternalConnector connector = ConnectorFactory.createConnector("pos");
        assertInstanceOf(PosConnector.class, connector);
    }

    @Test
    void factoryShouldCreateConnectorCaseInsensitiveForErp() {
        ExternalConnector connector = ConnectorFactory.createConnector("erp");
        assertInstanceOf(ErpConnector.class, connector);
    }

    @Test
    void factoryShouldThrowForUnknownConnectorType() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> ConnectorFactory.createConnector("CRM")
        );
        assertTrue(ex.getMessage().contains("Tipo de conector desconocido"));
    }

    @Test
    void factoryShouldThrowForNullConnectorType() {
        assertThrows(NullPointerException.class, () -> ConnectorFactory.createConnector(null));
    }

    @Test
    void notifyUserRegisteredShouldDoNothingWhenIntegrationsDisabled() {
        ExternalIntegrationsProperties properties = baseProperties(false);
        ExternalIntegrationService service = new ExternalIntegrationService(properties);

        assertDoesNotThrow(() -> service.notifyUserRegistered(sampleUser(), "CRM"));
    }

    @Test
    void notifyUserRegisteredShouldUsePosConnectorWhenEnabled() {
        ExternalIntegrationsProperties properties = baseProperties(true);
        ExternalIntegrationService service = new ExternalIntegrationService(properties);

        assertDoesNotThrow(() -> service.notifyUserRegistered(sampleUser(), "POS"));
    }

    @Test
    void notifyUserRegisteredShouldUseErpConnectorWhenEnabled() {
        ExternalIntegrationsProperties properties = baseProperties(true);
        ExternalIntegrationService service = new ExternalIntegrationService(properties);

        assertDoesNotThrow(() -> service.notifyUserRegistered(sampleUser(), "ERP"));
    }

    @Test
    void notifyUserRegisteredShouldNotPropagateUnknownConnectorErrors() {
        ExternalIntegrationsProperties properties = baseProperties(true);
        ExternalIntegrationService service = new ExternalIntegrationService(properties);

        assertDoesNotThrow(() -> service.notifyUserRegistered(sampleUser(), "UNKNOWN"));
    }

    @Test
    void getCircuitBreakerStatusShouldExposeExpectedName() {
        ExternalIntegrationsProperties properties = baseProperties(true);
        ExternalIntegrationService service = new ExternalIntegrationService(properties);

        Map<String, Object> status = service.getCircuitBreakerStatus();

        assertEquals("auth-external-calls", status.get("name"));
    }

    @Test
    void getCircuitBreakerStatusShouldExposeExpectedKeys() {
        ExternalIntegrationsProperties properties = baseProperties(true);
        ExternalIntegrationService service = new ExternalIntegrationService(properties);

        Map<String, Object> status = service.getCircuitBreakerStatus();

        assertTrue(status.containsKey("state"));
        assertTrue(status.containsKey("failureRate"));
        assertTrue(status.containsKey("bufferedCalls"));
        assertTrue(status.containsKey("failedCalls"));
        assertTrue(status.containsKey("successfulCalls"));
    }

    private ExternalIntegrationsProperties baseProperties(boolean enabled) {
        ExternalIntegrationsProperties properties = new ExternalIntegrationsProperties();
        properties.setEnabled(enabled);
        properties.setFailureRateThreshold(50.0f);
        properties.setSlidingWindowSize(10);
        properties.setMinimumNumberOfCalls(5);
        properties.setPermittedCallsInHalfOpenState(3);
        properties.setWaitDurationOpenStateMs(1000L);
        return properties;
    }

    private User sampleUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("factory@test.com");
        user.setRole(Role.USER);
        user.setCreatedAt(Instant.now());
        return user;
    }
}
