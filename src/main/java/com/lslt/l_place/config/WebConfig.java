package com.lslt.l_place.config;


import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * CORS 설정을 정의합니다.
     * React 개발 서버에서의 요청을 허용하며, 필요한 HTTP 메서드만 허용합니다.
     *
     * @param registry CORS 설정을 등록할 레지스트리
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 URL 패턴에 대해 CORS 허용
                .allowedOrigins("*") // Swagger UI에서 요청 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 필요한 HTTP 메서드
                .allowedHeaders("*") // 모든 헤더 허용
                .exposedHeaders("Authorization") // 필요한 헤더 추가
                .allowCredentials(false); // 자격 증명 허용 설정
    }

}
