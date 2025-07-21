package com.example.moduleapi.annotation.swagger;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameters({
        @Parameter(
                name = "Authorization",
                description = "AccessToken Header",
                required = true,
                in = ParameterIn.HEADER
        ),
        @Parameter(
                name = "Authorization_Refresh",
                description = "RefreshToken Cookie",
                required = true,
                in = ParameterIn.COOKIE
        ),
        @Parameter(
                name = "Authorization_ino",
                description = "ino Cookie",
                required = true,
                in = ParameterIn.COOKIE
        )
})
@SecurityRequirements({
        @SecurityRequirement(name = "Authorization"),
        @SecurityRequirement(name = "Authorization_Refresh"),
        @SecurityRequirement(name = "Authorization_ino")
})
public @interface DefaultApiResponse {
}
