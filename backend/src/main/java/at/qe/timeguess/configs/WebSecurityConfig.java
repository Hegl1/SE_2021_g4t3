package at.qe.timeguess.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.session.SessionManagementFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.Arrays;

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

    @Autowired
    private CorsFilter corsFilter;

    @Override
    protected void configure(final HttpSecurity httpSecurity) throws Exception {
        httpSecurity.cors().configurationSource(request ->
        {
            CorsConfiguration cors = new CorsConfiguration(); //configuration that fixes cors problems
            cors.setAllowedMethods(
                Arrays.asList(HttpMethod.DELETE.name(), HttpMethod.GET.name(), HttpMethod.POST.name(),HttpMethod.PUT.name()));
            cors.applyPermitDefaultValues();

            return cors;

        }).and().csrf().disable().authorizeRequests()
            .antMatchers("/h2-console/**")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/users/login")
            .permitAll()
            .antMatchers(HttpMethod.POST, "/users/register")
            .permitAll()
            .antMatchers("/websocket/**")
            .permitAll()
            .antMatchers("/dice/**")
            .permitAll()
            .and().authorizeRequests().anyRequest().authenticated().and()
            .exceptionHandling().authenticationEntryPoint(authenticationHandler).and()
            // this disables session creation on Spring Security
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        httpSecurity.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);
        httpSecurity.addFilterBefore(corsFilter, SessionManagementFilter.class);

        //for h2console to work
        httpSecurity.headers().frameOptions().disable();
    }
}
