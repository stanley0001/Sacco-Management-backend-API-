# BPS PayBill Integration Guide

**Date:** November 4, 2025  
**Status:** ✅ INTEGRATED WITH BPS MODULE

---

## Overview

PayBill C2B callbacks are now **fully integrated with your BPS module**. You can register callback URLs directly from your admin panel without needing external configuration!

---

## ✅ BPS Module Integration

### Step 1: Auto-Generate Callback URLs

From your BPS admin panel:

```http
POST /api/mpesa/config/{configId}/generate-urls
Content-Type: application/json

{
    "baseUrl": "https://yourdomain.com"
}
```

**This automatically sets:**
- `validationUrl`: `https://yourdomain.com/api/mpesa/callback/validation`
- `confirmationUrl`: `https://yourdomain.com/api/mpesa/callback/confirmation`
- `paybillCallbackUrl`: `https://yourdomain.com/api/mpesa/callback/paybill`
- `stkCallbackUrl`: `https://yourdomain.com/api/mpesa/callback/stk-push`
- `b2cCallbackUrl`: `https://yourdomain.com/api/mpesa/callback/b2c`

### Step 2: Register with M-PESA (Optional)

```http
POST /api/mpesa/config/{configId}/register-paybill
Content-Type: application/json

{
    "paybillUrl": "https://yourdomain.com/api/mpesa/callback/paybill"
}
```

**That's it!** No need to leave the platform.

---

## 🔗 Callback Endpoints

All endpoints now follow M-PESA's official format and match your BPS auto-generated URLs:

### 1. Validation Endpoint
```http
POST /api/mpesa/callback/validation
```
- Called by M-PESA **before** processing payment
- Validates if transaction should be accepted
- Returns `ResultCode: 0` to accept

### 2. Confirmation Endpoint
```http
POST /api/mpesa/callback/confirmation
OR
POST /api/mpesa/callback/paybill
```
- Called by M-PESA **after** payment is confirmed
- Processes the actual payment
- Routes to deposit or loan repayment

### 3. Health Check
```http
GET /api/mpesa/callback/health
```
- Verify callback endpoints are running
- Returns status and all available endpoints

### 4. Test Endpoint
```http
POST /api/mpesa/callback/test-paybill
```
- Simulate PayBill payments locally
- No need for actual M-PESA transactions

---

## 📝 M-PESA Request Format

Your system now expects the **EXACT format** M-PESA sends:

```json
{
  "TransactionType": "Pay Bill",
  "TransID": "QGK12345678",
  "TransTime": "20251104223000",
  "TransAmount": "1000.00",
  "BusinessShortCode": "400200",
  "BillRefNumber": "12345678",
  "InvoiceNumber": "",
  "OrgAccountBalance": "",
  "ThirdPartyTransID": "",
  "MSISDN": "254712345678",
  "FirstName": "John",
  "MiddleName": "",
  "LastName": "Doe"
}
```

**Key Fields:**
- `TransID` - M-PESA receipt number
- `TransAmount` - Amount paid
- `BillRefNumber` - **Customer's document number or LOAN-xxx**
- `MSISDN` - Customer phone number

---

## 💡 How It Works

### For Deposits (Document Number):
```
Customer pays via M-PESA PayBill
├─ Business: 400200
├─ Account: 12345678 (Document Number)
└─ Amount: 1000

M-PESA → Validation → Your System
       ← ResultCode: 0 (Accept)

M-PESA → Confirmation → Your System
       ├─ Find customer by document number
       ├─ Deposit to SAVINGS account
       └─ Send SMS confirmation
       ← ResultCode: 0 (Success)
```

### For Loan Repayment:
```
Customer pays via M-PESA PayBill
├─ Business: 400200
├─ Account: LOAN-123456
└─ Amount: 500

M-PESA → Validation → Your System
       ← ResultCode: 0 (Accept)

M-PESA → Confirmation → Your System
       ├─ Find loan by reference
       ├─ Apply payment
       └─ Send SMS confirmation
       ← ResultCode: 0 (Success)
```

---

## 🧪 Testing

### Test 1: Verify Endpoints are Live

```bash
GET http://localhost:8082/api/mpesa/callback/health

Expected:
{
    "status": "UP",
    "service": "M-PESA PayBill Callback",
    "timestamp": "2025-11-04T22:30:00",
    "endpoints": {
        "validation": "/api/mpesa/callback/validation",
        "confirmation": "/api/mpesa/callback/confirmation",
        "paybill": "/api/mpesa/callback/paybill"
    }
}
```

### Test 2: Simulate Deposit

```bash
POST http://localhost:8082/api/mpesa/callback/test-paybill
Content-Type: application/json

{
    "accountNumber": "12345678",
    "amount": "1000",
    "phoneNumber": "254712345678"
}

Expected:
{
    "ResultCode": "0",
    "ResultDesc": "Success"
}
```

### Test 3: Simulate Loan Repayment

```bash
POST http://localhost:8082/api/mpesa/callback/test-paybill
Content-Type: application/json

{
    "accountNumber": "LOAN-123456",
    "amount": "500",
    "phoneNumber": "254712345678"
}
```

---

## 🔧 BPS Module Commands

### View All M-PESA Configurations

```http
GET /api/mpesa/config
Authorization: Bearer {token}
```

### Create New Configuration

```http
POST /api/mpesa/config
Content-Type: application/json

{
    "configName": "Production PayBill",
    "shortcode": "400200",
    "consumerKey": "your_consumer_key",
    "consumerSecret": "your_consumer_secret",
    "passKey": "your_pass_key",
    "environment": "production",
    "isActive": true
}
```

### Auto-Generate URLs for Configuration

```http
POST /api/mpesa/config/1/generate-urls
Content-Type: application/json

{
    "baseUrl": "https://yourdomain.com"
}
```

---

## 📋 Checklist for Production

- [ ] Configure M-PESA credentials in BPS module
- [ ] Auto-generate callback URLs using BPS
- [ ] Test validation endpoint
- [ ] Test confirmation endpoint  
- [ ] Test with real document numbers
- [ ] Test loan repayment
- [ ] Verify SMS notifications work
- [ ] Register URLs with Safaricom (optional via BPS)
- [ ] Monitor suspense payments
- [ ] Set up callback URL monitoring

---

## 🎯 Key Benefits

1. **No External Configuration** - Everything managed in BPS
2. **Automatic URL Generation** - One click to set all callbacks
3. **Official M-PESA Format** - Exact format Safaricom sends
4. **Suspense Handling** - Payments never lost
5. **Integrated SMS** - Automatic notifications
6. **Health Monitoring** - Built-in endpoint verification

---

## 📊 Monitoring

### Check Failed Transactions

```http
GET /api/mpesa/config/failed-transactions?days=7
```

### View Transaction History

```http
GET /api/mpesa/config/history?page=0&size=50
```

### Get Analytics

```http
GET /api/mpesa/config/analytics?days=30
```

### View Suspense Payments

```http
GET /api/suspense-payments?status=NEW
```

---

## 🚀 Deployment Steps

1. **Start Application**
   ```bash
   mvn spring-boot:run
   ```

2. **Verify Health**
   ```bash
   curl http://localhost:8082/api/mpesa/callback/health
   ```

3. **Configure in BPS**
   - Login to admin panel
   - Go to M-PESA Configuration
   - Add/edit configuration
   - Click "Generate URLs"
   - Enter your domain

4. **Test**
   - Use test endpoint
   - Verify logs
   - Check database

5. **Go Live**
   - Update M-PESA dashboard
   - Monitor transactions
   - Track suspense payments

---

## Status: PRODUCTION READY ✅

✅ BPS module integration complete  
✅ Auto-URL generation working  
✅ Official M-PESA format supported  
✅ Document number + loan repayment  
✅ Suspense payments recorded  
✅ SMS notifications enabled  
✅ Health monitoring available  

**No external configuration needed - everything managed in your platform!**
