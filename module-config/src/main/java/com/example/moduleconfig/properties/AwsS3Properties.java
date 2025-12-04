package com.example.moduleconfig.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws.s3")
@Getter
@Setter
public class AwsS3Properties {
    private String bucket;
}
