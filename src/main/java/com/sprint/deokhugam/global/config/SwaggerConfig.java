package com.sprint.deokhugam.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Deokhugam API")
                .description("Deokhugam API 문서")
                .version("v1.0")
            )
            .servers(List.of(
                new Server()
                    .url("")
                    .description("Generated server url")
            ))
            .tags(List.of(
                new Tag().name("알림 관리").description("알림 관련 API"),
                new Tag().name("도서 관리").description("도서 관련 API"),
                new Tag().name("댓글 관리").description("댓글 관련 API"),
                new Tag().name("사용자 관리").description("사용자 관련 API"),
                new Tag().name("리뷰 관리").description("리뷰 관련 API")
            ));

    }
}
