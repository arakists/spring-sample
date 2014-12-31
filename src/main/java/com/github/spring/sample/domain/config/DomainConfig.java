package com.github.spring.sample.domain.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = { "com.github.spring.sample.domain" })
public class DomainConfig {

}
