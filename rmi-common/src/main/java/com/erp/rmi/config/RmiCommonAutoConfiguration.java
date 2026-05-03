package com.erp.rmi.config;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@AutoConfiguration
@EnableConfigurationProperties(RmiBootstrapProperties.class)
public class RmiCommonAutoConfiguration {
}
