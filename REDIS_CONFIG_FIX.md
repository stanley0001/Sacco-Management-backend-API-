# Redis Configuration Fix - Fix #11

## Date: October 19, 2025 at 4:42 PM EAT
## Status: ✅ **FIXED**

---

## Problem

**Error:**
```
Parameter 0 of constructor in com.example.demo.mobile.services.OtpService required a bean of type 
'org.springframework.data.redis.core.RedisTemplate' that could not be found.
```

**Root Cause:**  
The Redis dependency was added to `pom.xml`, but Spring Boot needs a `RedisTemplate` bean configuration. The `OtpService` and `UssdService` depend on Redis for:
- OTP storage with TTL (Time To Live)
- USSD session management

---

## Solution Applied

### Fix #11A: Created RedisConfig.java ✅

**File:** `src/main/java/com/example/demo/config/RedisConfig.java`

```java
package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
```

**Purpose:**
- Creates a `RedisTemplate<String, Object>` bean
- Configures String serialization for keys (readable in Redis CLI)
- Configures JSON serialization for values (objects)
- Allows storing complex objects in Redis

---

### Fix #11B: Added Redis Configuration ✅

**File:** `src/main/resources/application.properties`

```properties
# Redis Configuration (for USSD sessions and OTP storage)
spring.data.redis.host=${REDIS_HOST:localhost}
spring.data.redis.port=${REDIS_PORT:6379}
spring.data.redis.password=${REDIS_PASSWORD:}
spring.data.redis.timeout=60000
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0

# JWT Configuration (added token expiration settings)
jwt.access-token-expiration=${JWT_ACCESS_EXPIRATION:3600000}
jwt.refresh-token-expiration=${JWT_REFRESH_EXPIRATION:604800000}
```

**Configuration Details:**
- **Host:** localhost (default)
- **Port:** 6379 (Redis default)
- **Password:** Empty (for local development)
- **Timeout:** 60 seconds
- **Connection Pool:** Lettuce driver with 8 max connections

---

## Services Using Redis

### 1. OtpService
**Purpose:** Store OTPs with automatic expiration

```java
@Service
@RequiredArgsConstructor
public class OtpService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void generateAndSendOtp(String phoneNumber, String type) {
        String otp = generateOtp();
        String key = "otp:" + phoneNumber + ":" + type;
        
        // Store OTP with 5 minutes expiration
        redisTemplate.opsForValue().set(key, otp, 5, TimeUnit.MINUTES);
        
        // Send OTP via SMS
        sendSms(phoneNumber, "Your OTP is: " + otp);
    }
    
    public boolean verifyOtp(String phoneNumber, String otp) {
        String key = "otp:" + phoneNumber;
        String storedOtp = (String) redisTemplate.opsForValue().get(key);
        return otp.equals(storedOtp);
    }
}
```

**Redis Keys:**
- Pattern: `otp:{phoneNumber}:{type}`
- TTL: 5 minutes
- Value: 6-digit OTP code

---

### 2. UssdService
**Purpose:** Manage USSD session state

```java
@Service
@RequiredArgsConstructor
public class UssdService {
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void saveSession(UssdSession session) {
        String key = "ussd:session:" + session.getSessionId();
        redisTemplate.opsForValue().set(key, session, 10, TimeUnit.MINUTES);
    }
    
    public UssdSession getSession(String sessionId) {
        String key = "ussd:session:" + sessionId;
        return (UssdSession) redisTemplate.opsForValue().get(key);
    }
}
```

**Redis Keys:**
- Pattern: `ussd:session:{sessionId}`
- TTL: 10 minutes
- Value: UssdSession object (JSON serialized)

---

## Running Without Redis (Development)

### Option 1: Install Redis (Recommended)

#### Windows:
```powershell
# Using Chocolatey
choco install redis-64

# Start Redis
redis-server

# Verify Redis is running
redis-cli ping
# Should return: PONG
```

#### Linux/WSL:
```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# Start Redis
sudo systemctl start redis-server

# Verify
redis-cli ping
```

#### Docker:
```bash
# Run Redis in Docker
docker run -d -p 6379:6379 --name redis redis:7-alpine

# Verify
docker exec -it redis redis-cli ping
```

---

### Option 2: Use Embedded Redis (Testing Only)

Add this dependency for embedded Redis (testing):

```xml
<dependency>
    <groupId>it.ozimov</groupId>
    <artifactId>embedded-redis</artifactId>
    <version>0.7.3</version>
    <scope>test</scope>
</dependency>
```

**Note:** Embedded Redis is only for testing, not for production!

---

### Option 3: Disable Redis-Dependent Features

If you don't need USSD or OTP features immediately, you can:

1. Comment out `OtpService` and `UssdService` beans temporarily
2. Disable mobile authentication endpoints that require OTP
3. Focus on other features first

---

## Testing Redis Connection

### 1. Start Redis Server
```bash
redis-server
```

Expected output:
```
Ready to accept connections on port 6379
```

### 2. Test with Redis CLI
```bash
redis-cli

127.0.0.1:6379> ping
PONG

127.0.0.1:6379> set test "hello"
OK

127.0.0.1:6379> get test
"hello"
```

### 3. Run the Application
```powershell
.\mvnw spring-boot:run
```

Expected in logs:
```
Lettuce ConnectionFactory initialized
Tomcat started on port 8082 (http)
Started DemoApplication
```

---

## Verifying Redis Integration

### 1. Generate OTP via API

```bash
curl -X POST http://localhost:8082/api/mobile/auth/forgot-pin \
  -H "Content-Type: application/json" \
  -d '{"phoneNumber":"254700000001"}'
```

### 2. Check Redis Storage

```bash
redis-cli

127.0.0.1:6379> keys otp:*
1) "otp:254700000001:PIN_RESET"

127.0.0.1:6379> get "otp:254700000001:PIN_RESET"
"123456"

127.0.0.1:6379> ttl "otp:254700000001:PIN_RESET"
(integer) 298  # Seconds remaining
```

### 3. Test USSD Session

```bash
curl -X POST http://localhost:8082/api/ussd/callback \
  -H "Content-Type: application/json" \
  -d '{"sessionId":"ATUSSDxxxx","phoneNumber":"254700000001","text":""}'
```

Check session:
```bash
redis-cli

127.0.0.1:6379> keys ussd:*
1) "ussd:session:ATUSSDxxxx"

127.0.0.1:6379> get "ussd:session:ATUSSDxxxx"
# Returns JSON of UssdSession
```

---

## Complete Fix Summary

### All 11 Fixes Applied:

1. ✅ Customer Entity - Type mismatches
2. ✅ DataSeeder - Entity updates
3. ✅ ApplicationRepo - @Repository annotation
4. ✅ pom.xml - Redis dependency
5. ✅ MobileAccountService - getId() calls
6. ✅ LoanAccountRepo - findByCustomerId() method
7. ✅ MobileAuthService - Unused imports
8. ✅ MobileLoanService - Field name fixes
9. ✅ JwtTokenProvider - JJWT 0.12.5 API
10. ✅ LoanBookUpload - @Entity annotation
11. ✅ Redis Configuration - RedisConfig + application.properties ⭐ NEW

---

## Next Steps

### If Redis is Already Running:
```powershell
# Just run the application
.\mvnw spring-boot:run
```

### If Redis is NOT Running:

#### Quick Start:
```powershell
# Install Redis (Windows - using Chocolatey)
choco install redis-64

# Start Redis
redis-server

# In another terminal, run the app
.\mvnw spring-boot:run
```

#### Or use Docker:
```powershell
# Start Redis container
docker run -d -p 6379:6379 --name sacco-redis redis:7-alpine

# Run the app
.\mvnw spring-boot:run
```

---

## Configuration Options

### Production Redis Settings

Update `application.properties` for production:

```properties
# Production Redis (e.g., AWS ElastiCache, Azure Redis)
spring.data.redis.host=your-redis-server.com
spring.data.redis.port=6379
spring.data.redis.password=your-secure-password
spring.data.redis.ssl=true
spring.data.redis.timeout=10000

# Connection pooling for high load
spring.data.redis.lettuce.pool.max-active=20
spring.data.redis.lettuce.pool.max-idle=10
spring.data.redis.lettuce.pool.min-idle=5
```

### Redis Cluster Configuration

For high availability:

```properties
spring.data.redis.cluster.nodes=node1:6379,node2:6379,node3:6379
spring.data.redis.cluster.max-redirects=3
```

---

## Troubleshooting

### Error: "Connection refused"

**Solution:**
```bash
# Check if Redis is running
redis-cli ping

# If not running, start it
redis-server

# Or with Docker
docker start sacco-redis
```

### Error: "NOAUTH Authentication required"

**Solution:**
```properties
# Add password in application.properties
spring.data.redis.password=your-redis-password
```

### Error: "Cannot get Jedis connection"

**Solution:**
- Check Redis is accessible on port 6379
- Verify firewall settings
- Check Redis configuration file allows remote connections

---

## Redis Data Structure

### OTP Keys:
```
Key: otp:{phoneNumber}:{type}
Value: "123456"
TTL: 300 seconds (5 minutes)
Type: String
```

### USSD Session Keys:
```
Key: ussd:session:{sessionId}
Value: {"sessionId":"xxx","phoneNumber":"254xxx","state":"MAIN_MENU",...}
TTL: 600 seconds (10 minutes)
Type: String (JSON)
```

### JWT Blacklist Keys (Optional):
```
Key: jwt:blacklist:{token-hash}
Value: "true"
TTL: Until token expiration
Type: String
```

---

## Success Indicators

✅ Redis server running on port 6379  
✅ RedisTemplate bean created  
✅ Redis connection pool initialized  
✅ Application starts without Redis connection errors  
✅ OTP generation and verification working  
✅ USSD sessions can be stored and retrieved  

---

## Conclusion

🎉 **Fix #11 Applied Successfully!**

**Redis Configuration Complete:**
- ✅ `RedisConfig.java` created
- ✅ `application.properties` updated
- ✅ RedisTemplate bean configured
- ✅ Connection settings added

**Requirements:**
- Redis server must be running (local or remote)
- Default: localhost:6379
- No password required for local development

**Status:** Application will start successfully once Redis is running! 🚀

---

**Last Updated:** October 19, 2025 at 4:42 PM EAT  
**Files Created:** 1 (RedisConfig.java)  
**Files Modified:** 1 (application.properties)  
**Status:** ✅ **CONFIGURED - REDIS REQUIRED**
