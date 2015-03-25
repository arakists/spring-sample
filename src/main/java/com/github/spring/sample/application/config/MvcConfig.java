package com.github.spring.sample.application.config;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.DefaultServletHandlerConfigurer;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewResolverRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;
import org.springframework.web.servlet.support.RequestDataValueProcessor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.terasoluna.gfw.common.exception.ExceptionCodeResolver;
import org.terasoluna.gfw.common.exception.ExceptionLogger;
import org.terasoluna.gfw.web.exception.HandlerExceptionResolverLoggingInterceptor;
import org.terasoluna.gfw.web.exception.SystemExceptionResolver;
import org.terasoluna.gfw.web.logging.TraceLoggingInterceptor;
import org.terasoluna.gfw.web.mvc.support.CompositeRequestDataValueProcessor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenInterceptor;
import org.terasoluna.gfw.web.token.transaction.TransactionTokenRequestDataValueProcessor;

@Configuration
@PropertySource(value = { "classpath:/META-INF/spring/mvc.properties" }, ignoreResourceNotFound = true)
@ComponentScan(basePackages = { "com.github.spring.sample.application.controller" })
@EnableWebMvc
@EnableAspectJAutoProxy
public class MvcConfig extends WebMvcConfigurerAdapter {

	@Inject
	ExceptionCodeResolver exceptionCodeResolver;

	@Inject
	ExceptionLogger exceptionLogger;

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/resources/**")
				.addResourceLocations("/resources/").setCachePeriod(60 * 60);
	}

	@Override
	public void configureViewResolvers(ViewResolverRegistry registry) {
		registry.viewResolver(getInternalResourceViewResolver());
	}

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new TraceLoggingInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns("/resources/**", "/**/*.html");
		registry.addInterceptor(new TransactionTokenInterceptor())
				.addPathPatterns("/**")
				.excludePathPatterns("/resources/**", "/**/*.html");
	}

	@Override
	public void configureDefaultServletHandling(
			DefaultServletHandlerConfigurer configurer) {
		configurer.enable();
	}

	@Bean
	public RequestDataValueProcessor requestDataValueProcessor() {
		CompositeRequestDataValueProcessor compositeRequestDataValueProcessor = new CompositeRequestDataValueProcessor(
				new TransactionTokenRequestDataValueProcessor());
		return compositeRequestDataValueProcessor;
	}

	@Bean
	public InternalResourceViewResolver getInternalResourceViewResolver() {
		InternalResourceViewResolver resolver = new InternalResourceViewResolver();
		resolver.setPrefix("/WEB-INF/views/");
		resolver.setSuffix(".jsp");
		return resolver;
	}

	@Override
	public void configureHandlerExceptionResolvers(
			List<HandlerExceptionResolver> exceptionResolvers) {
		SystemExceptionResolver systemExceptionResolver = new SystemExceptionResolver();
		systemExceptionResolver.setExceptionCodeResolver(exceptionCodeResolver);
		systemExceptionResolver.setOrder(3);

		Properties exceptionMappings = new Properties();
		exceptionMappings.put("ResourceNotFoundException",
				"common/error/resourceNotFoundError");
		exceptionMappings
				.put("BusinessException", "common/error/businessError");
		exceptionMappings.put("InvalidTransactionTokenException",
				"common/error/transactionTokenError");
		exceptionMappings.put("DataAccessException",
				"common/error/dataAccessError");
		systemExceptionResolver.setExceptionMappings(exceptionMappings);

		Properties statusCodes = new Properties();
		statusCodes.setProperty("common/error/resourceNotFoundError", "404");
		statusCodes.setProperty("common/error/businessError", "409");
		statusCodes.setProperty("common/error/transactionTokenError", "409");
		statusCodes.setProperty("common/error/ataAccessError", "500");
		systemExceptionResolver.setStatusCodes(statusCodes);

		systemExceptionResolver.setDefaultErrorView("common/error/systemError");
		systemExceptionResolver.setDefaultStatusCode(500);

		exceptionResolvers.add(systemExceptionResolver);
		super.configureHandlerExceptionResolvers(exceptionResolvers);
	}

	@Bean
	public HandlerExceptionResolverLoggingInterceptor handlerExceptionResolverLoggingInterceptor() {
		HandlerExceptionResolverLoggingInterceptor exceptionResolverLoggingInterceptor = new HandlerExceptionResolverLoggingInterceptor();
		exceptionResolverLoggingInterceptor.setExceptionLogger(exceptionLogger);

		return exceptionResolverLoggingInterceptor;
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}
}
