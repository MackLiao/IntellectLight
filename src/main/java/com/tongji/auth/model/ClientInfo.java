package com.tongji.auth.model;

/**
 * Client information.
 * <p>
 * Records client IP and User-Agent for login auditing, risk control, and activity tracking.
 * This object is typically parsed from HTTP requests by controllers.
 *
 * @param ip        Client IP address (may come from `X-Forwarded-For` or remote address).
 * @param userAgent Client User-Agent string.
 */
public record ClientInfo(String ip, String userAgent) {
}
