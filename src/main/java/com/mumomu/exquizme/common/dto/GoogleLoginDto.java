package com.mumomu.exquizme.common.dto;

// AccessToken을 활용해 JWT의 Payload 부분인 사용자 정보를 Response받는 VO

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoogleLoginDto {
    private String iss;
    private String azp;
    private String aud;
    private String sub;
    private String email;
    private String hd;
    private String emailVerified;
    private String atHash;
    private String name;
    private String picture;
    private String givenName;
    private String familyName;
    private String locale;
    private String iat;
    private String exp;
    private String alg;
    private String kid;
    private String typ;
}