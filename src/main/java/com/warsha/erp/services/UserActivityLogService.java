package com.warsha.erp.services;

import com.warsha.erp.entities.UserActivityLog;
import com.warsha.erp.repository.UserActivityLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserActivityLogService {

    @Autowired
    private UserActivityLogRepository userActivityLogRepository;

    public void logCustomerLogin(Long customerId, HttpServletRequest request) {
        UserActivityLog log = new UserActivityLog();
        log.setCustomerId(customerId);
        log.setLoginTime(LocalDateTime.now());
        
        if (request != null) {
            String userAgent = request.getHeader("User-Agent");
            log.setUserAgent(userAgent);
            
            String ipAddress = request.getHeader("X-Forwarded-For");
            if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
            log.setIpAddress(ipAddress);
            
            // Basic client platform extraction
            String clientPlatform = "Unknown";
            if (userAgent != null) {
                if (userAgent.toLowerCase().contains("android")) {
                    clientPlatform = "Android";
                } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
                    clientPlatform = "iOS";
                } else if (userAgent.toLowerCase().contains("windows")) {
                    clientPlatform = "Windows";
                } else if (userAgent.toLowerCase().contains("mac")) {
                    clientPlatform = "Mac";
                } else if (userAgent.toLowerCase().contains("linux")) {
                    clientPlatform = "Linux";
                }
            }
            log.setClientPlatform(clientPlatform);
        }

        userActivityLogRepository.save(log);
    }
}
