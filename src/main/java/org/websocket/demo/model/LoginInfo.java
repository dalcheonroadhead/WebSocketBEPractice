package org.websocket.demo.model;

import lombok.Builder;
import lombok.Getter;


// id 및 jwt 토큰을 전달할 DTO
@Getter
public class LoginInfo {
    private String name;
    private String token;

    @Builder
    public LoginInfo (String name, String token){
        this.name = name;
        this.token = token;
    }
}
