package com.savt.listopia.security.auth;

import lombok.Builder;
import lombok.Data;

// Session saklanması için şimdilik database kullanılabilir, ya da karar değiştirirsek
// bu veriyi imzalayıp cookie'nin içine yerleştirip ekstra database ihityacını azaltabiliriz.
// Birden fazla sessino aynı userid'ye sahip olabilir.
// mesela telefondan ve bilgisayardan aynı hesaba girmiştir.
@Data
@Builder
public class Session {
    private String sessionId;
    private String userId;
    private long createdAt;
    private long expiresAt;
}
