package org.main.v1.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

// Nested embedded ID class
@Deprecated
@Embeddable
public class AuthTokenId implements Serializable {

    @Column(name = "token")
    private String tokenId;
    
    private String userId;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAtId;

    public AuthTokenId() {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthTokenId that = (AuthTokenId) o;
        return Objects.equals(tokenId, that.tokenId) &&
                Objects.equals(userId, that.userId) &&
                Objects.equals(expiresAtId, that.expiresAtId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tokenId, userId, expiresAtId);
    }

    public String getTokenId() {
        return tokenId;
    }

    public void setTokenId(String tokenId) {
        this.tokenId = tokenId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public LocalDateTime getExpiresAtId() {
        return expiresAtId;
    }

    public void setExpiresAtId(LocalDateTime expiresAtId) {
        this.expiresAtId = expiresAtId;
    }
}
