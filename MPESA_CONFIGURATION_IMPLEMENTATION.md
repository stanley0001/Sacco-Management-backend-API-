# M-PESA Configuration & Testing Implementation

## Overview

This document describes the complete M-PESA configuration management system with UI integration and testing capabilities implemented in the BPS (Banking & Payment Systems) module.

## Features Implemented

### ✅ Backend Features

1. **Configuration Management**
   - Create, Read, Update, Delete M-PESA configurations
   - Support multiple configurations (SANDBOX and PRODUCTION)
   - Set default configuration
   - Toggle active/inactive status
   - Secure storage of sensitive credentials

2. **Test Connection Functionality**
   - **Authentication Test**: Verify consumer key and secret
   - **STK Push Test**: Send real test payment prompt to phone
   - **Query Test**: Test API endpoints
   - Detailed test results with timing and error information

3. **Statistics Tracking**
   - Total transactions per configuration
   - Success/failure rates
   - Last test timestamp and results

### ✅ Frontend Features

1. **M-PESA Configuration UI**
   - View all configurations
   - Create new configurations
   - Edit existing configurations
   - Delete configurations
   - Set as default
   - Toggle status

2. **Test Connection UI**
   - Test authentication
   - Test STK Push with custom phone number and amount
   - View test results in real-time
   - Detailed success/failure information

## Architecture

### Backend Structure

```
payments/
├── entities/
│   └── MpesaConfig.java              # Configuration entity
├── dto/
│   ├── MpesaConfigDTO.java           # Configuration DTO with masking
│   ├── MpesaTestConnectionRequest.java  # Test request
│   └── MpesaTestConnectionResponse.java # Test response
├── repositories/
│   └── MpesaConfigRepository.java    # Data access
├── services/
│   └── MpesaConfigService.java       # Business logic & testing
└── controllers/
    └── MpesaConfigController.java    # REST endpoints
```

### Frontend Structure

```
services/
└── mpesa-config.service.ts           # Angular service for API calls

bps/
├── bps.component.ts                  # Component logic with M-PESA integration
├── bps.component.html                # UI templates
└── bps.component.css                 # Styling
```

## API Endpoints

### Configuration Management

| Method | Endpoint | Description | Permission |
|--------|----------|-------------|------------|
| GET | `/api/mpesa/config` | Get all configurations | `canViewBps` |
| GET | `/api/mpesa/config/{id}` | Get configuration by ID | `canViewBps` |
| POST | `/api/mpesa/config` | Create configuration | `canManageBps` |
| PUT | `/api/mpesa/config/{id}` | Update configuration | `canManageBps` |
| DELETE | `/api/mpesa/config/{id}` | Delete configuration | `canManageBps` |
| PATCH | `/api/mpesa/config/{id}/toggle-status` | Toggle active status | `canManageBps` |
| PATCH | `/api/mpesa/config/{id}/set-default` | Set as default | `canManageBps` |
| POST | `/api/mpesa/config/test-connection` | Test connection | `canViewBps` |

### Test Connection Request

```json
{
  "configId": 1,
  "testType": "AUTH" | "STK_PUSH" | "QUERY",
  "testPhoneNumber": "0708374149",
  "testAmount": 1
}
```

### Test Connection Response

```json
{
  "success": true,
  "testType": "STK_PUSH",
  "message": "STK Push initiated successfully!",
  "testTime": "2025-10-21T12:30:00",
  "duration": 1245,
  "authenticationSuccess": true,
  "apiReachable": true,
  "configurationValid": true,
  "checkoutRequestId": "ws_CO_211020251230123456",
  "merchantRequestId": "1234-5678-9012",
  "details": {
    "CheckoutRequestID": "ws_CO_211020251230123456",
    "MerchantRequestID": "1234-5678-9012",
    "ResponseCode": "0",
    "ResponseDescription": "Success"
  }
}
```

## Database Schema

### mpesa_config Table

```sql
CREATE TABLE mpesa_config (
    id BIGSERIAL PRIMARY KEY,
    config_name VARCHAR(255) NOT NULL UNIQUE,
    consumer_key VARCHAR(255) NOT NULL,
    consumer_secret VARCHAR(255) NOT NULL,
    shortcode VARCHAR(255) NOT NULL,
    passkey TEXT NOT NULL,
    initiator_name VARCHAR(255) NOT NULL,
    security_credential TEXT NOT NULL,
    api_url VARCHAR(255) NOT NULL,
    callback_url VARCHAR(255) NOT NULL,
    timeout_url VARCHAR(255) NOT NULL,
    result_url VARCHAR(255) NOT NULL,
    environment VARCHAR(50) NOT NULL, -- 'SANDBOX' or 'PRODUCTION'
    active BOOLEAN NOT NULL DEFAULT true,
    default_config BOOLEAN NOT NULL DEFAULT false,
    description TEXT,
    
    -- Test Results
    last_test_success BOOLEAN,
    last_test_date TIMESTAMP,
    last_test_message TEXT,
    
    -- Statistics
    total_transactions BIGINT DEFAULT 0,
    successful_transactions BIGINT DEFAULT 0,
    failed_transactions BIGINT DEFAULT 0,
    
    -- Audit
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);
```

## Usage Guide

### 1. Create M-PESA Configuration

**Via UI:**
1. Navigate to BPS Management → Configuration
2. Click "Add Provider" or "Configure Provider"
3. Fill in M-PESA credentials:
   - Config Name: e.g., "PRIMARY_SANDBOX"
   - Consumer Key: From Safaricom Developer Portal
   - Consumer Secret: From Safaricom Developer Portal
   - Shortcode: Your business shortcode
   - Passkey: Lipa Na M-PESA passkey
   - Environment: SANDBOX or PRODUCTION
   - URLs: Callback, Timeout, Result URLs

**Via API:**
```bash
curl -X POST http://localhost:8082/api/mpesa/config \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "configName": "PRIMARY_SANDBOX",
    "consumerKey": "your_consumer_key",
    "consumerSecret": "your_consumer_secret",
    "shortcode": "174379",
    "passkey": "your_passkey",
    "initiatorName": "testapi",
    "securityCredential": "your_security_credential",
    "apiUrl": "https://sandbox.safaricom.co.ke",
    "callbackUrl": "https://your-domain.com/api/mpesa/callback",
    "timeoutUrl": "https://your-domain.com/api/mpesa/timeout",
    "resultUrl": "https://your-domain.com/api/mpesa/result",
    "environment": "SANDBOX",
    "active": true,
    "defaultConfig": true,
    "description": "Primary Sandbox Configuration"
  }'
```

### 2. Test Connection

**Authentication Test:**
```typescript
// Frontend
this.mpesaService.testConnection({
  configId: 1,
  testType: 'AUTH'
}).subscribe(result => {
  console.log('Test result:', result);
});
```

**STK Push Test:**
```typescript
// Frontend
this.mpesaService.testConnection({
  configId: 1,
  testType: 'STK_PUSH',
  testPhoneNumber: '0708374149',
  testAmount: 1
}).subscribe(result => {
  if (result.success) {
    alert('Check phone for payment prompt!');
  }
});
```

### 3. Use in Production

The configured M-PESA settings are automatically used by the existing M-PESA service:

```java
// In MpesaService.java
public STKPushResponse initiateSTKPush(STKPushRequest request) {
    // Automatically uses default configuration from database
    MpesaConfig config = configService.getDefaultConfiguration();
    
    // Use config.getConsumerKey(), config.getShortcode(), etc.
    // ... rest of STK Push logic
}
```

## Security Features

### 1. Credential Masking
Sensitive fields are automatically masked when sent to frontend:
- Consumer Secret: `abc***xyz`
- Passkey: `pass***word`
- Security Credential: `sec***dential`

### 2. Full Credentials for Edit
When editing, full unmasked credentials are loaded to allow updates.

### 3. Conditional Updates
Only update secrets if they're not masked (don't contain `***`).

### 4. Permission-Based Access
- `canViewBps`: View configurations and test
- `canManageBps`: Create, update, delete configurations
- `ADMIN_ACCESS`: Full access

## Test Connection Types

### 1. AUTH Test
**Purpose:** Verify consumer key and secret are valid
**Process:**
1. Encode credentials to Base64
2. Call M-PESA OAuth endpoint
3. Attempt to get access token
4. Return success/failure with token details

### 2. STK_PUSH Test
**Purpose:** Send real payment prompt to test the complete flow
**Process:**
1. Authenticate (AUTH test)
2. Generate timestamp and password
3. Call STK Push API with test amount
4. Send prompt to provided phone number
5. Return checkout request ID

### 3. QUERY Test
**Purpose:** Verify query endpoint connectivity
**Currently:** Performs AUTH test (can be extended)

## Configuration Best Practices

### 1. Multiple Configurations
- **Sandbox**: For development and testing
- **Production**: For live transactions
- **Backup**: Fallback configuration

### 2. Configuration Naming
Use descriptive names:
- `PRIMARY_SANDBOX`
- `PRIMARY_PRODUCTION`
- `BACKUP_PRODUCTION`

### 3. Default Configuration
- Mark the most used configuration as default
- Only one configuration can be default
- Used automatically by payment services

### 4. Testing Before Going Live
1. Create SANDBOX configuration
2. Test authentication
3. Test STK Push with small amounts
4. Verify callback handling
5. Then create PRODUCTION configuration

## Integration with Existing M-PESA Service

The configuration can be integrated with the existing `MpesaService`:

```java
@Service
public class MpesaService {
    
    private final MpesaConfigService configService;
    
    public STKPushResponse initiateSTKPush(STKPushRequest request) {
        // Get default configuration
        MpesaConfig config = configService.getDefaultConfiguration();
        
        // Use configuration values
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            
        String password = Base64.getEncoder().encodeToString(
            (config.getShortcode() + config.getPasskey() + timestamp).getBytes()
        );
        
        // Build request using config values
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("BusinessShortCode", config.getShortcode());
        requestBody.put("Password", password);
        // ... rest of the implementation
    }
}
```

## Frontend Usage

### Loading Configurations
```typescript
ngOnInit(): void {
  this.loadMpesaConfigurations();
}

loadMpesaConfigurations(): void {
  this.mpesaService.getAllConfigurations().subscribe(
    configs => {
      this.mpesaConfigs = configs;
      console.log('Loaded configurations:', configs);
    },
    error => {
      console.error('Error loading configurations:', error);
    }
  );
}
```

### Testing STK Push from UI
```typescript
testSTKPush(configId: number, phoneNumber: string, amount: number): void {
  const request = {
    configId: configId,
    testType: 'STK_PUSH',
    testPhoneNumber: phoneNumber,
    testAmount: amount
  };
  
  this.mpesaService.testConnection(request).subscribe(
    response => {
      if (response.success) {
        alert(`STK Push sent! Check phone ${phoneNumber}`);
      } else {
        alert(`Failed: ${response.message}`);
      }
    }
  );
}
```

## Troubleshooting

### Issue: Authentication Failed
**Causes:**
- Invalid consumer key/secret
- Credentials from wrong environment (sandbox vs production)

**Solution:**
1. Verify credentials in Safaricom Developer Portal
2. Ensure using correct environment URL
3. Test authentication separately

### Issue: STK Push Not Received
**Causes:**
- Invalid phone number format
- Phone not registered for M-PESA
- Sandbox limitations

**Solution:**
1. Format phone as 254XXXXXXXXX
2. Use registered test numbers in sandbox
3. Check M-PESA callback logs

### Issue: Callback Not Received
**Causes:**
- Invalid callback URL
- URL not publicly accessible
- Firewall blocking

**Solution:**
1. Use ngrok for local testing
2. Ensure HTTPS in production
3. Whitelist Safaricom IPs

## Next Steps

1. **Run Database Migration**: Ensure `mpesa_config` table is created
2. **Create Initial Configuration**: Add your M-PESA credentials
3. **Test Connection**: Use AUTH test to verify setup
4. **Test STK Push**: Send test payment to your phone
5. **Monitor Transactions**: Check statistics and success rates
6. **Go Live**: Create production configuration when ready

## Security Checklist

- [ ] Store credentials securely in database
- [ ] Use HTTPS for callback URLs
- [ ] Implement rate limiting on test endpoints
- [ ] Log all configuration changes
- [ ] Restrict delete operation to non-default configs
- [ ] Mask sensitive data in API responses
- [ ] Validate phone numbers before STK Push
- [ ] Monitor failed authentication attempts

## Support

For M-PESA API documentation: https://developer.safaricom.co.ke
For issues or questions: Contact your system administrator
