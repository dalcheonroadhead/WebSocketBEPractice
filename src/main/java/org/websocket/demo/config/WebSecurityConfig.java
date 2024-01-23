package org.websocket.demo.config;

/*
* WebSecurityConfigurerAdapter 는 deprecated 되었음. 따라서 그 대안으로
* Component-based Security Configuration을 사용하는 것이 권장됨. configure() -> filterChain
*
* */

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());

        http.csrf(AbstractHttpConfigurer::disable) // 기본적으로 ON인 csrf 취약점 보안을 해제한다.
        // .header((s) -> s.frameOptions((a) -> a.sameOrigin()))
                .headers((header) -> header.frameOptions(HeadersConfigurer.FrameOptionsConfig::sameOrigin)) // SockJS는 기본적으로 HTML ifram 요소를 통한 전송을 허용하지 않도록 설정되는데, 해당 설정을 해제한다.
                .formLogin(Object::toString) // 권한 없이 페이지 접근 시 로그인 페이지로 이동 시킴
                .authorizeHttpRequests((request) -> request.requestMatchers("/chat/*").hasRole("USER") // chat으로 시작하는 리소스에 대한 접근 권한 설정
                        .anyRequest().permitAll()); // 나머지 리소스에 대한 접근 설정
        return  http.build();
    }


    /*
    *  테스트를 위해 In-Memory에 계정을 임의로 생성한다.
    *  서비스에서 사용시에는 DB 데이터를 이용하도록 수정이 필요
    * */

    @Bean
    public InMemoryUserDetailsManager userDetailService() {
        UserDetails user = User.builder()
                .username("user1")
                .password(passwordEncoder().encode("1234"))
                .roles("USER")
                .build();

        UserDetails user2 = User.builder()
                .username("user2")
                .password(passwordEncoder().encode("1234"))
                .roles("USER")
                .build();

        UserDetails guest = User.builder()
                .username("guest")
                .password(passwordEncoder().encode("1234"))
                .roles("GUEST")
                .build();

        return new InMemoryUserDetailsManager(user,user2,guest);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
