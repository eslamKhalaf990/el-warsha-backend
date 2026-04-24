package com.warsha.erp.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "UserActivityLogs")
public class UserActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogID")
    private Long logId;

    @Column(name = "CustomerID")
    private Long customerId;

    @Column(name = "LoginTime")
    private LocalDateTime loginTime;

    @Column(name = "ClientPlatform")
    private String clientPlatform;

    @Column(name = "IPAddress")
    private String ipAddress;

    @Column(name = "UserAgent")
    private String userAgent;
}
