package com.github.spring.sample.application.config;

import java.util.LinkedHashMap;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.terasoluna.gfw.common.exception.ExceptionCodeResolver;
import org.terasoluna.gfw.common.exception.ExceptionLogger;
import org.terasoluna.gfw.common.exception.SimpleMappingExceptionCodeResolver;
import org.terasoluna.gfw.web.exception.ExceptionLoggingFilter;

import com.github.spring.sample.domain.config.DomainConfig;

@Configuration
@PropertySource(value = { "classpath*:/META-INF/spring/*.properties" }, ignoreResourceNotFound = true)
@Import(value = { DomainConfig.class })
public class AppConfig {

	@Bean
	public MessageSource messageSource() {
		ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
		messageSource.setBasenames("i18n/application-messages");
		return messageSource;
	}

	@Bean
	public MessageSourceAccessor messageSourceAccessor() {
		return new MessageSourceAccessor(messageSource());
	}

	@Bean
	public ExceptionCodeResolver exceptionCodeResolver() {
		SimpleMappingExceptionCodeResolver simpleMappingExceptionCodeResolver = new SimpleMappingExceptionCodeResolver();

		LinkedHashMap<String, String> exceptionMappings = new LinkedHashMap<>();
		exceptionMappings.put("ResourceNotFoundException", "e.xx.fw.5001");
		exceptionMappings.put("InvalidTransactionTokenException",
				"e.xx.fw.7001");
		exceptionMappings.put("BusinessException", "e.xx.fw.8001");
		exceptionMappings.put("DataAccessException", "e.xx.fw.9002");
		simpleMappingExceptionCodeResolver
				.setExceptionMappings(exceptionMappings);

		simpleMappingExceptionCodeResolver
				.setDefaultExceptionCode("e.xx.fw.9001");

		return simpleMappingExceptionCodeResolver;
	}

	@Bean
	public ExceptionLogger exceptionLogger() {
		ExceptionLogger exceptionLogger = new ExceptionLogger();
		exceptionLogger.setExceptionCodeResolver(exceptionCodeResolver());

		return exceptionLogger;
	}

	@Bean
	public ExceptionLoggingFilter exceptionLoggingFilter() {
		ExceptionLoggingFilter exceptionLoggingFilter = new ExceptionLoggingFilter();
		exceptionLoggingFilter.setExceptionLogger(exceptionLogger());
		return exceptionLoggingFilter;
	}
}
