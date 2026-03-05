package com.chat.auth.authservice.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "last_seen")
    private Instant lastSeen;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
        this.lastSeen = Instant.now();
        this.status = UserStatus.ONLINE;
    }

    public enum UserStatus {
        ONLINE, OFFLINE, AWAY
    }
}
