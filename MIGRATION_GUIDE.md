# Migration Guide - Spring Boot 3 and javax to jakarta

## Overview
This application has been upgraded to Spring Boot 3.2.5, which requires migrating from `javax.*` to `jakarta.*` packages.

## Required Changes

### 1. Update All Entity Classes

Replace all occurrences of:
```java
import javax.persistence.*;
import javax.validation.constraints.*;
```

With:
```java
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
```

### 2. Update Servlet Imports

Replace:
```java
import javax.servlet.*;
```

With:
```java
import jakarta.servlet.*;
```

### 3. Files Requiring Updates

Run the following find-and-replace across the entire `src/` directory:

```bash
# On Unix/Linux/Mac
find src -type f -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} +
find src -type f -name "*.java" -exec sed -i 's/javax\.validation/jakarta.validation/g' {} +
find src -type f -name "*.java" -exec sed -i 's/javax\.servlet/jakarta.servlet/g' {} +

# On Windows PowerShell
Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName) -replace 'javax\.persistence', 'jakarta.persistence' |
    Set-Content $_.FullName
}
Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName) -replace 'javax\.validation', 'jakarta.validation' |
    Set-Content $_.FullName
}
Get-ChildItem -Path src -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName) -replace 'javax\.servlet', 'jakarta.servlet' |
    Set-Content $_.FullName
}
```

## Verified New Features

### ✅ Savings Management Module
- Complete savings account management
- Multiple savings products
- Deposit and withdrawal transactions
- Interest calculation support
- Transaction history

### ✅ Enhanced Reports Module
- Loan portfolio reports
- SASRA SG3 - Loan Classification
- SASRA SG4 - Liquidity Analysis
- SASRA SG5 - Capital Adequacy
- Prudential Returns
- Excel export functionality

### ✅ Modernized Dependencies
- Spring Boot 3.2.5
- OpenAPI 3.0 (Swagger UI)
- JWT 0.12.5
- Apache POI 5.2.5 for Excel generation
- iText PDF 5.5.13 for PDF generation

### ✅ Deployment Ready
- Docker multi-stage build
- Docker Compose configuration
- Environment-based configuration
- Health checks
- PostgreSQL 15

## Testing After Migration

1. **Run Maven build**:
```bash
./mvnw clean install
```

2. **Start with Docker**:
```bash
docker-compose up --build
```

3. **Verify API Documentation**:
- Navigate to: http://localhost:8082/swagger-ui.html
- Test endpoints through Swagger UI

4. **Test Key Endpoints**:
- POST /authenticate - Login
- GET /api/savings/products - List savings products
- GET /api/reports/sasra - Generate SASRA report

## Breaking Changes

1. **JWT Library**: Updated to 0.12.5 - method signatures have changed
2. **Swagger**: Moved from Springfox 2.x to Springdoc OpenAPI 3.x
3. **Persistence**: All JPA annotations now use `jakarta.persistence`
4. **Validation**: All validation annotations now use `jakarta.validation`

## Rollback Plan

If issues arise, revert to the previous version:
```bash
git checkout <previous-commit-hash>
```

Or use Docker to run the old version while fixing issues.
