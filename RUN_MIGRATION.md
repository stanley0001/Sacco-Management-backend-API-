# Jakarta Migration Instructions

## Quick Migration (Automated)

### For Windows (PowerShell):

```powershell
# Navigate to backend directory
cd s:\code\PERSONAL\java\Sacco-Management-backend-API-

# Run migration script
.\migrate-to-jakarta.ps1
```

### For Unix/Linux/Mac (Bash):

```bash
# Navigate to backend directory
cd /path/to/Sacco-Management-backend-API-

# Make script executable
chmod +x migrate-to-jakarta.sh

# Run migration
./migrate-to-jakarta.sh
```

## Manual Migration (If scripts don't work)

### PowerShell One-Liners:

```powershell
# Navigate to backend/src directory first
cd s:\code\PERSONAL\java\Sacco-Management-backend-API-\src

# Migrate javax.persistence
Get-ChildItem -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName -Raw) -replace 'import javax\.persistence\.', 'import jakarta.persistence.' |
    Set-Content $_.FullName -NoNewline
}

# Migrate javax.validation
Get-ChildItem -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName -Raw) -replace 'import javax\.validation\.', 'import jakarta.validation.' |
    Set-Content $_.FullName -NoNewline
}

# Migrate javax.servlet
Get-ChildItem -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName -Raw) -replace 'import javax\.servlet\.', 'import jakarta.servlet.' |
    Set-Content $_.FullName -NoNewline
}

# Migrate javax.annotation
Get-ChildItem -Filter *.java -Recurse | ForEach-Object {
    (Get-Content $_.FullName -Raw) -replace 'import javax\.annotation\.', 'import jakarta.annotation.' |
    Set-Content $_.FullName -NoNewline
}
```

## What Gets Migrated?

The migration replaces the following imports:

- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.servlet.*` → `jakarta.servlet.*`
- `javax.annotation.*` → `jakarta.annotation.*`
- `javax.transaction.*` → `jakarta.transaction.*`
- `javax.xml.bind.*` → `jakarta.xml.bind.*`

## Files That Will Be Modified

Approximately **35 Java files** will be updated:

### Entities:
- All entity classes in `banking`, `loanManagement`, `customerManagement`, `communication`, `userManagements`
- Examples: `BankAccounts.java`, `Customer.java`, `LoanAccount.java`, `Users.java`, etc.

### Services:
- `JWTauthFilter.java`
- `authService.java`
- `CustomAuthenticationFailureHandler.java`

## After Migration

1. **Verify changes**:
   ```powershell
   git diff
   ```

2. **Clean and rebuild**:
   ```powershell
   .\mvnw clean install
   ```

3. **Run the application**:
   ```powershell
   .\mvnw spring-boot:run
   ```

4. **Check for errors**:
   - All `javax.persistence` errors should be gone
   - IDE should recognize `jakarta.persistence` annotations
   - Application should start successfully

## Troubleshooting

### If you see "cannot find symbol" errors:
- Run Maven update: `.\mvnw dependency:resolve`
- Refresh your IDE project
- Clean and rebuild

### If migration script doesn't run:
- Ensure you're in the correct directory
- Check PowerShell execution policy: `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`
- Use manual PowerShell one-liners above

## IDE Configuration

After migration, configure your IDE:

### IntelliJ IDEA:
1. File → Project Structure → Project SDK → Select Java 17
2. File → Settings → Build, Execution, Deployment → Compiler → Java Compiler → Set to 17
3. Invalidate Caches and Restart

### Eclipse:
1. Right-click project → Properties → Java Build Path → Libraries → Set JRE to 17
2. Project → Properties → Java Compiler → Set to 17
3. Clean and rebuild project

### VS Code:
1. Update `settings.json`:
   ```json
   {
     "java.configuration.runtimes": [
       {
         "name": "JavaSE-17",
         "path": "path/to/jdk-17"
       }
     ]
   }
   ```
2. Reload window

## Verification Checklist

- [ ] All `.java` files show `jakarta.*` imports (not `javax.*`)
- [ ] Maven build completes successfully
- [ ] No compilation errors in IDE
- [ ] Application starts without errors
- [ ] Swagger UI accessible at `http://localhost:8082/swagger-ui.html`
- [ ] All API endpoints working

## Need Help?

If you encounter issues:
1. Check the MIGRATION_GUIDE.md for detailed information
2. Verify Spring Boot version is 3.2.5 in pom.xml
3. Ensure Java 17 is installed and configured
4. Review error logs for specific issues
