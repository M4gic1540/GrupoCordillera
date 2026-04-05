package com.main.authservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "auth.external")
public class ExternalIntegrationsProperties {

    private boolean enabled = false;
    private String baseUrl = "http://localhost:8099";
    private String userRegisteredPath = "/api/v1/notifications/user-registered";
    private float failureRateThreshold = 50.0f;
    private int slidingWindowSize = 10;
    private int minimumNumberOfCalls = 5;
    private int permittedCallsInHalfOpenState = 3;
    private long waitDurationOpenStateMs = 15000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUserRegisteredPath() {
        return userRegisteredPath;
    }

    public void setUserRegisteredPath(String userRegisteredPath) {
        this.userRegisteredPath = userRegisteredPath;
    }

    public float getFailureRateThreshold() {
        return failureRateThreshold;
    }

    public void setFailureRateThreshold(float failureRateThreshold) {
        this.failureRateThreshold = failureRateThreshold;
    }

    public int getSlidingWindowSize() {
        return slidingWindowSize;
    }

    public void setSlidingWindowSize(int slidingWindowSize) {
        this.slidingWindowSize = slidingWindowSize;
    }

    public int getMinimumNumberOfCalls() {
        return minimumNumberOfCalls;
    }

    public void setMinimumNumberOfCalls(int minimumNumberOfCalls) {
        this.minimumNumberOfCalls = minimumNumberOfCalls;
    }

    public int getPermittedCallsInHalfOpenState() {
        return permittedCallsInHalfOpenState;
    }

    public void setPermittedCallsInHalfOpenState(int permittedCallsInHalfOpenState) {
        this.permittedCallsInHalfOpenState = permittedCallsInHalfOpenState;
    }

    public long getWaitDurationOpenStateMs() {
        return waitDurationOpenStateMs;
    }

    public void setWaitDurationOpenStateMs(long waitDurationOpenStateMs) {
        this.waitDurationOpenStateMs = waitDurationOpenStateMs;
    }
}
