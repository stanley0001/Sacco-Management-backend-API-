# ✅ C2B Register URL - @JsonProperty Fix

## Problem
M-PESA Daraja API requires exact field names with specific capitalization:
- `ShortCode` (not `shortCode`)
- `ResponseType` (not `responseType`)
- `ConfirmationURL` (not `confirmationUrl`)
- `ValidationURL` (not `validationUrl`)

Without `@JsonProperty` annotations, Spring's Jackson would convert to camelCase, causing the API call to fail.

## Solution Implemented

### 1. Created DTO with @JsonProperty annotations
**File:** `C2BRegisterUrlRequest.java`

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class C2BRegisterUrlRequest {

    @JsonProperty("ShortCode")
    private String shortCode;

    @JsonProperty("ResponseType")
    private String responseType;

    @JsonProperty("ConfirmationURL")
    private String confirmationURL;

    @JsonProperty("ValidationURL")
    private String validationURL;
}
```

### 2. Updated MpesaService to use DTO
**File:** `MpesaService.java` (line 849)

**Before:**
```java
Map<String, Object> requestBody = new HashMap<>();
requestBody.put("ShortCode", shortcode);
requestBody.put("ResponseType", "Completed");
requestBody.put("ConfirmationURL", confirmationUrl);
requestBody.put("ValidationURL", validationUrl);

HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
```

**After:**
```java
C2BRegisterUrlRequest requestBody = C2BRegisterUrlRequest.builder()
    .shortCode(shortcode)
    .responseType("Completed")
    .confirmationURL(confirmationUrl)
    .validationURL(validationUrl)
    .build();

HttpEntity<C2BRegisterUrlRequest> requestEntity = new HttpEntity<>(requestBody, headers);
```

## How It Works

When Spring serializes the DTO to JSON, `@JsonProperty` tells Jackson to use the exact field names:

```json
{
  "ShortCode": "402634",
  "ResponseType": "Completed",
  "ConfirmationURL": "https://mydomain.com/api/auto-pay/callback/confirm",
  "ValidationURL": "https://mydomain.com/api/auto-pay/callback/validate"
}
```

Without `@JsonProperty`, it would have been (WRONG):
```json
{
  "shortCode": "402634",
  "responseType": "Completed",
  "confirmationURL": "https://mydomain.com/api/auto-pay/callback/confirm",
  "validationURL": "https://mydomain.com/api/auto-pay/callback/validate"
}
```

## Testing

### Verify JSON Output
Add this log to see the exact JSON being sent:
```java
log.info("Request Body JSON: {}", objectMapper.writeValueAsString(requestBody));
```

Should output:
```json
{
  "ShortCode":"402634",
  "ResponseType":"Completed",
  "ConfirmationURL":"https://yourdomain.com/api/auto-pay/callback/confirm",
  "ValidationURL":"https://yourdomain.com/api/auto-pay/callback/validate"
}
```

### Expected Response from M-PESA
**Success:**
```json
{
  "OriginatorCoversationID": "7619-37765134-1",
  "ResponseCode": "0",
  "ResponseDescription": "success"
}
```

**Error (if field names are wrong):**
```json
{
  "ResponseCode": "1",
  "ResponseDescription": "Invalid request"
}
```

## Benefits of Using DTO

1. ✅ **Type Safety** - Compile-time validation
2. ✅ **Documentation** - Clear what fields are required
3. ✅ **Reusability** - Can use in tests and other services
4. ✅ **Validation** - Can add @NotNull, @Size annotations
5. ✅ **Exact Field Names** - @JsonProperty ensures M-PESA compatibility

## Files Modified

1. **Created:** `C2BRegisterUrlRequest.java` - DTO with @JsonProperty
2. **Modified:** `MpesaService.java` - Uses DTO instead of HashMap

## Status: ✅ READY TO TEST

The C2B URL registration now sends the exact field names required by Safaricom Daraja API!
