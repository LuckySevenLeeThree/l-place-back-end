package com.lslt.l_place.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(title = "L-Place API", version = "1.0", description = "L-Place 프로젝트 API 문서")
)
public class SwaggerConfig {
}