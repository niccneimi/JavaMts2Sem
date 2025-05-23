package com.example.util;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtil {
    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
    };

    public static String getClientIpAddress(HttpServletRequest request) {
        if (request == null) {
            return "unknown";
        }
        
        for (String header : IP_HEADERS) {
            String ipAddress = request.getHeader(header);
            if (ipAddress != null && ipAddress.length() > 0 && !"unknown".equalsIgnoreCase(ipAddress)) {
                int commaIndex = ipAddress.indexOf(",");
                if (commaIndex > 0) {
                    ipAddress = ipAddress.substring(0, commaIndex);
                }
                return ipAddress;
            }
        }
        
        return request.getRemoteAddr();
    }
} 