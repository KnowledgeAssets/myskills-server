package io.knowledgeassets.myskills.server.security;

import io.knowledgeassets.myskills.server.user.command.UserCommandService;
import io.knowledgeassets.myskills.server.user.query.UserQueryService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.oauth2.resource.ResourceServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
import org.springframework.security.oauth2.provider.token.store.jwk.JwkTokenStore;

import java.util.Set;

@Configuration
@EnableResourceServer
public class SecurityConfiguration extends ResourceServerConfigurerAdapter {
	private final ResourceServerProperties resourceServerProperties;
	private final MySkillsProperties mySkillsProperties;
	private final String[] publicResourcesPatterns;

	public SecurityConfiguration(ResourceServerProperties resourceServerProperties,
								 MySkillsProperties mySkillsProperties,
								 @Value("${springfox.documentation.swagger.v2.path}") String apiSpecPath) {
		this.resourceServerProperties = resourceServerProperties;
		this.mySkillsProperties = mySkillsProperties;
		this.publicResourcesPatterns = new String[]{
				"/",
				"/favicon.ico",
				"/csrf",
				"/error",
				"/swagger-ui.html",
				"/webjars/**",
				"/swagger-resources/**",
				apiSpecPath
		};
	}

	@Bean
	public UserAuthenticationConverter userAuthenticationConverter(UserQueryService userQueryService,
																   UserCommandService userCommandService) {
		return new UserCreatingUserAuthenticationConverter(userQueryService, userCommandService,
				Set.copyOf(mySkillsProperties.getSecurity().getDefaultRoles()));
	}

	@Bean
	public AccessTokenConverter accessTokenConverter(UserQueryService userQueryService,
													 UserCommandService userCommandService) {
		DefaultAccessTokenConverter accessTokenConverter = new DefaultAccessTokenConverter();
		accessTokenConverter.setUserTokenConverter(userAuthenticationConverter(userQueryService, userCommandService));
		return accessTokenConverter;
	}

	@Bean
	public TokenStore tokenStore(UserQueryService userQueryService, UserCommandService userCommandService) {
		return new JwkTokenStore(resourceServerProperties.getJwk().getKeySetUri(),
				accessTokenConverter(userQueryService, userCommandService));
	}

	@Override
	public void configure(ResourceServerSecurityConfigurer resources) throws Exception {
		resources.resourceId(resourceServerProperties.getResourceId());
	}

	@Override
	public void configure(HttpSecurity http) throws Exception {
		http.authorizeRequests()
				.antMatchers(publicResourcesPatterns).permitAll()
				.anyRequest().authenticated()
				// TODO: Use cookie-based CSRF token repository for production
//				.and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
				.and().csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
	}

}
