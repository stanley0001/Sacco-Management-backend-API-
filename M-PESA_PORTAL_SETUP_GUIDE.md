# 🔗 M-PESA Portal Callback URL Setup Guide

## 📋 **Step 1: Access Safaricom Developer Portal**

1. **Login** to [Safaricom Developer Portal](https://developer.safaricom.co.ke/)
2. **Navigate** to your M-PESA app
3. **Go to** "My Apps" → Select your app → "Configuration"

## 🌐 **Step 2: Configure Callback URLs**

### **Production URLs** (Replace `yourdomain.com` with your actual domain):
```
STK Push Callback: https://yourdomain.com/api/mpesa/callback/stk-push
B2C Callback: https://yourdomain.com/api/mpesa/callback/b2c
Transaction Status: https://yourdomain.com/api/mpesa/callback/transaction-status
Validation URL: https://yourdomain.com/api/mpesa/callback/generic
Confirmation URL: https://yourdomain.com/api/mpesa/callback/generic
```

### **Development URLs** (For local testing with ngrok):
```bash
# Start ngrok
ngrok http 8080

# Use the HTTPS URL provided by ngrok
STK Push Callback: https://abc123.ngrok.io/api/mpesa/callback/stk-push
B2C Callback: https://abc123.ngrok.io/api/mpesa/callback/b2c
Transaction Status: https://abc123.ngrok.io/api/mpesa/callback/transaction-status
Validation URL: https://abc123.ngrok.io/api/mpesa/callback/generic
Confirmation URL: https://abc123.ngrok.io/api/mpesa/callback/generic
```

## 🔐 **Step 3: Security Configuration**

Add to your `SecurityConfig.java`:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authz -> authz
            // Exempt M-PESA callbacks from authentication
            .requestMatchers("/api/mpesa/callback/**").permitAll()
            .requestMatchers("/api/payments/callback/**").permitAll()
            
            // Health check endpoints
            .requestMatchers("/api/mpesa/callback/health").permitAll()
            
            // Your other security configurations...
            .anyRequest().authenticated()
        )
        .csrf(csrf -> csrf
            // Disable CSRF for M-PESA callbacks
            .ignoringRequestMatchers("/api/mpesa/callback/**")
        );
        
        return http.build();
    }
}
```

## 🧪 **Step 4: Testing Callback URLs**

### **Test Health Endpoint:**
```bash
# Test if callback URL is accessible
curl -X GET https://yourdomain.com/api/mpesa/callback/health

# Expected response:
{
  "status": "healthy",
  "timestamp": "2024-11-04T02:27:00",
  "service": "M-PESA Callback Service"
}
```

### **Test STK Push Callback:**
```bash
# Simulate M-PESA callback (for testing)
curl -X POST https://yourdomain.com/api/mpesa/callback/stk-push \
  -H "Content-Type: application/json" \
  -d '{
    "Body": {
      "stkCallback": {
        "MerchantRequestID": "test123",
        "CheckoutRequestID": "ws_CO_test123",
        "ResultCode": 0,
        "ResultDesc": "The service request is processed successfully."
      }
    }
  }'

# Expected response:
{
  "ResultCode": 0,
  "ResultDesc": "Success"
}
```

## 📊 **Step 5: Monitor Callback Logs**

Add to your `application.yml`:
```yaml
logging:
  level:
    com.example.demo.finance.payments.controllers.MpesaCallbackController: DEBUG
    com.example.demo.finance.payments.services.MpesaService: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n"
```

**Monitor logs:**
```bash
# Follow callback logs
tail -f logs/application.log | grep -i "callback\|mpesa"

# Example successful log:
2024-11-04 02:27:15.123 INFO  MpesaCallbackController - 🔔 M-PESA STK Push Callback received: {...}
2024-11-04 02:27:15.124 INFO  MpesaService - Processing STK callback for checkout: ws_CO_123...
2024-11-04 02:27:15.125 INFO  MpesaCallbackController - ✅ STK Push callback processed successfully
```

## 🎯 **Step 6: Verification Checklist**

### **Portal Configuration:**
- [ ] ✅ Callback URLs configured in Safaricom portal
- [ ] ✅ URLs are HTTPS (required for production)
- [ ] ✅ URLs are publicly accessible
- [ ] ✅ Health endpoint returns 200 OK

### **Backend Configuration:**
- [ ] ✅ Security exemptions added for callback endpoints
- [ ] ✅ CSRF disabled for callback endpoints
- [ ] ✅ Callback controller handles all M-PESA callback types
- [ ] ✅ Logging configured for monitoring

### **Testing:**
- [ ] ✅ Health endpoint accessible
- [ ] ✅ Callback endpoints return proper M-PESA response format
- [ ] ✅ STK Push callbacks processed correctly
- [ ] ✅ Transaction status updates in database
- [ ] ✅ SMS notifications sent on completion

## 🚨 **Common Issues & Solutions**

### **Issue 1: Callback URL Not Accessible**
```bash
# Test URL accessibility
curl -I https://yourdomain.com/api/mpesa/callback/health

# Should return: HTTP/1.1 200 OK
```
**Solution:** Ensure firewall allows HTTPS traffic, domain DNS is correct

### **Issue 2: CSRF Token Errors**
```bash
# Error: CSRF token missing
```
**Solution:** Add CSRF exemption in SecurityConfig:
```java
.csrf(csrf -> csrf.ignoringRequestMatchers("/api/mpesa/callback/**"))
```

### **Issue 3: 401 Unauthorized**
```bash
# Error: Authentication required
```
**Solution:** Add security exemption:
```java
.requestMatchers("/api/mpesa/callback/**").permitAll()
```

### **Issue 4: Callback Not Processing**
```bash
# Check logs for errors
tail -f logs/application.log | grep -i error
```
**Solution:** Check MpesaService.processCallback() method implementation

## 🎉 **Success Indicators**

When properly configured, you should see:

1. **M-PESA Portal**: ✅ Green status indicators for all callback URLs
2. **Application Logs**: ✅ Successful callback processing messages
3. **Database**: ✅ Transaction status updates from PENDING to SUCCESS/FAILED
4. **SMS**: ✅ Success/failure notifications sent to customers
5. **Frontend**: ✅ Real-time transaction status updates in client profile

## 📞 **Support Contacts**

- **Safaricom Developer Support**: developer@safaricom.co.ke
- **M-PESA Integration Docs**: https://developer.safaricom.co.ke/docs
- **Technical Issues**: Check logs and review callback implementation

---

**🎯 Ready for Production!** Once all checklist items are complete, your M-PESA integration will handle callbacks reliably and provide real-time transaction updates to your users.
