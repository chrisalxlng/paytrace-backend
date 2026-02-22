package dev.christopherlang.paytrace.common;

import io.swagger.v3.oas.models.media.Schema;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Configuration;

import java.time.Year;

@Configuration
public class OpenApiConfig {
    static {
        SpringDocUtils.getConfig().replaceWithSchema(Year.class,
            new Schema<String>()
                .type("string")
                .example("2023")
        );
    }
}
