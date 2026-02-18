# Detailed Code Changes

## File 1: order-management-service/pom.xml

### Changes Made
Added the following dependencies after `spring-boot-starter-actuator`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.11.5</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.11.5</version>
    <scope>runtime</scope>
</dependency>
```

---

## File 2: order-management-service/src/main/resources/application.yml

### Changes Made
Added JWT secret configuration at the end:

```yaml
jwt:
  secret: "${JWT_SECRET:MySecretKeyForJwtTokenGenerationThatIsAtLeast256BitsLongForHs256AlgorithmSecurity}"
```

**Note**: The secret must match the one used in auth-service and api-gateway

---

## File 3: order-management-service/src/main/java/com/example/order/OrderManagementApplication.java

### Changes Made
- Import added: `import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;`
- Annotation added: `@EnableMethodSecurity(prePostEnabled = true)`

```java
@SpringBootApplication
@EnableAsync
@EnableMethodSecurity(prePostEnabled = true)  // Added this line
public class OrderManagementApplication {
    // ... rest of code
}
```

---

## File 4: order-management-service/src/main/java/com/example/order/config/SecurityConfig.java (NEW FILE)

```java
package com.example.order.config;

import com.example.order.security.JwtAuthenticationFilter;
import com.example.order.security.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUtil jwtUtil;

    public SecurityConfig(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .anyRequest().authenticated()
            )
            .addFilterBefore(new JwtAuthenticationFilter(jwtUtil), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## File 5: order-management-service/src/main/java/com/example/order/security/JwtUtil.java (NEW FILE)

```java
package com.example.order.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret:MySecretKeyForJwtTokenGenerationThatIsAtLeast256BitsLongForHs256AlgorithmSecurity}")
    private String secret;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public boolean isTokenExpired(String token) {
        return this.getAllClaimsFromToken(token).getExpiration().before(new Date());
    }

    public boolean isTokenValid(String token) {
        try {
            this.getAllClaimsFromToken(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public String extractRole(String token) {
        Object role = getAllClaimsFromToken(token).get("role");
        return role != null ? role.toString() : "USER";
    }
}
```

---

## File 6: order-management-service/src/main/java/com/example/order/security/JwtAuthenticationFilter.java (NEW FILE)

```java
package com.example.order.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.isTokenValid(token)) {
                String username = jwtUtil.extractUsername(token);
                String role = jwtUtil.extractRole(token);
                
                // Create authorities from the role
                Collection<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
                
                // Create authentication token
                UsernamePasswordAuthenticationToken authentication = 
                    new UsernamePasswordAuthenticationToken(username, null, authorities);
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## File 7: order-management-service/src/main/java/com/example/order/controller/OrderController.java

### Changes Made
- Import added: `import org.springframework.security.access.prepost.PreAuthorize;`

#### Method 1: create() - Updated
```java
@PostMapping
@PreAuthorize("hasRole('ADMIN')")  // Added this annotation
public ResponseEntity<OrderDto> create(@Valid @RequestBody CreateOrderRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED).body(orderService.createOrder(request));
}
```

#### Method 2: getById() - No Changes
```java
@GetMapping("/{id}")
public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
    return orderService.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

#### Method 3: getByOrderNumber() - No Changes
```java
@GetMapping("/number/{orderNumber}")
public ResponseEntity<OrderDto> getByOrderNumber(@PathVariable String orderNumber) {
    return orderService.findByOrderNumber(orderNumber)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

#### Method 4: listOrders() - Updated
```java
@GetMapping
@PreAuthorize("hasRole('ADMIN')")  // Added this annotation
public List<OrderDto> listOrders(@RequestParam(required = false) String customerId) {
    if (customerId != null && !customerId.isEmpty()) {
        return orderService.findByCustomer(customerId);
    }
    return orderService.findAll();
}
```

#### Method 5: listByCustomer() - No Changes
```java
@GetMapping("/customer/{customerId}")
public List<OrderDto> listByCustomer(@PathVariable String customerId) {
    return orderService.findByCustomer(customerId);
}
```

#### Method 6: updateStatus() - Updated
```java
@PatchMapping("/{id}/status/{status}")
@PreAuthorize("hasRole('ADMIN')")  // Added this annotation
public ResponseEntity<OrderDto> updateStatus(
        @PathVariable Long id,
        @PathVariable OrderStatusEnum status) {
    return orderService.updateStatus(id, status)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

#### Method 7: cancel() - No Changes
```java
@PostMapping("/{id}/cancel")
public ResponseEntity<OrderDto> cancel(@PathVariable Long id) {
    return orderService.cancel(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
}
```

---

## Summary of Changes

| Component | Type | Change |
|-----------|------|--------|
| pom.xml | Modified | Added Spring Security and JWT dependencies |
| application.yml | Modified | Added JWT secret property |
| OrderManagementApplication.java | Modified | Added @EnableMethodSecurity annotation |
| SecurityConfig.java | Created | New security configuration class |
| JwtUtil.java | Created | New JWT utility class |
| JwtAuthenticationFilter.java | Created | New authentication filter |
| OrderController.java | Modified | Added @PreAuthorize annotations |

## Total Files Changed: 7
- Modified: 4 files
- Created: 3 files
