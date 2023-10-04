package foodsearch.config;

import edu.fudan.common.security.jwt.JWTFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import static org.springframework.web.cors.CorsConfiguration.ALL;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    String admin = "ADMIN";

    /**
     * load password encoder
     *
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * allow cors domain
     * header By default, only six fields can be taken from the header, and the
     * other fields can only be specified in the header.
     * credentials Cookies are not sent by default and can only be true if a Cookie
     * is needed
     * Validity of this request
     *
     * @return WebMvcConfigurer
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins(ALL)
                        .allowedMethods(ALL)
                        .allowedHeaders(ALL)
                        .allowCredentials(false)
                        .maxAge(3600);
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity.httpBasic(AbstractHttpConfigurer::disable)
                // close default csrf
                .csrf(AbstractHttpConfigurer::disable)
                // close session
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/foodservice/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/foodservice/orders/*").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/foodservice/orders").hasAnyRole(admin)
                        .requestMatchers(HttpMethod.POST, "/api/v1/foodservice/orders").hasAnyRole(admin)
                        .requestMatchers("/swagger-ui.html", "/webjars/**", "/images/**",
                                "/configuration/**", "/swagger-resources/**", "/v2/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new JWTFilter(), UsernamePasswordAuthenticationFilter.class);

        // close cache
        httpSecurity.headers(h -> h.cacheControl(Customizer.withDefaults()));

        return httpSecurity.build();
    }
}
