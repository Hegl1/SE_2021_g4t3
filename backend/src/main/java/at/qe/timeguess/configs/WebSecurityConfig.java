package at.qe.timeguess.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configures the security settings of the api.
 * This includes the encryption of passwords, the authorization for the endpoints with JWT tokens
 * and the configuration of CORS.
 */
@Configuration
@EnableWebSecurity()
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

	@Autowired
	private JwtRequestFilter jwtRequestFilter;

	@Autowired
	private JwtAuthenticationHandler authenticationHandler;

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

	@Override
    protected void configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity.csrf().disable().authorizeRequests()
            .antMatchers("/h2-console/**")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/users/login")
            .permitAll()
            .antMatchers("/dice/**")
            .permitAll()
            .and().authorizeRequests().anyRequest().authenticated().and()
            .exceptionHandling().authenticationEntryPoint(authenticationHandler).and()
            // this disables session creation on Spring Security
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        //for h2console to work
        httpSecurity.headers().frameOptions().disable();
    }
}
