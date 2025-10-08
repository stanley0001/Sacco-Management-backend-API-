# ⚡ CRITICAL: pom.xml Fix for LocalDate Parameter Error

## 🚨 Error Being Fixed

```
java.lang.IllegalArgumentException: Name for argument of type [java.time.LocalDate] not specified, 
and parameter name information not available via reflection. 
Ensure that the compiler uses the '-parameters' flag.
```

---

## 🔧 SOLUTION

### **Step 1: Update pom.xml**

**File**: `pom.xml` (in project root)

**Find the `<build>` section**. It should look something like this:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.8.1</version>
            <configuration>
                <source>17</source>
                <target>17</target>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**Replace with** (or update the maven-compiler-plugin configuration):

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <version>3.11.0</version>
            <configuration>
                <source>17</source>
                <target>17</target>
                <parameters>true</parameters>
                <compilerArgs>
                    <arg>-parameters</arg>
                </compilerArgs>
            </configuration>
        </plugin>
        
        <!-- Spring Boot Maven Plugin -->
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
        </plugin>
    </plugins>
</build>
```

### **Key Changes**:
1. ✅ Updated version to 3.11.0
2. ✅ Added `<parameters>true</parameters>`
3. ✅ Added `<compilerArgs>` with `-parameters` flag

---

### **Step 2: Rebuild Project**

Open terminal in project root and run:

```bash
# Clean previous builds
mvn clean

# Compile with new settings
mvn compile

# Install dependencies and package
mvn install -DskipTests

# Or do it all at once
mvn clean install -DskipTests
```

---

### **Step 3: Restart Application**

```bash
# Start Spring Boot
mvn spring-boot:run

# Or if you're running from IDE, just restart the application
```

---

## ✅ VERIFICATION

After restart, test the reports endpoint:

```bash
curl -X GET "http://localhost:8080/api/reports/portfolio" \
  -H "Authorization: Bearer YOUR_TOKEN"
```

If it works without the LocalDate error, you're good! ✅

---

## 🎯 ALTERNATIVE FIX (If above doesn't work)

If the pom.xml fix doesn't work, you need to add `@RequestParam` annotations to all controller methods.

**Example**:

### **BEFORE (Broken)**:
```java
@GetMapping("/reports")
public ResponseEntity<?> getReports(LocalDate startDate, LocalDate endDate) {
    // Missing parameter names
}
```

### **AFTER (Fixed)**:
```java
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;

@GetMapping("/reports")
public ResponseEntity<?> getReports(
    @RequestParam("startDate") 
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
    LocalDate startDate,
    
    @RequestParam("endDate") 
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) 
    LocalDate endDate
) {
    // Now Spring knows the parameter names
}
```

**Apply this pattern to ALL methods** that have LocalDate, LocalDateTime, or any custom object parameters.

---

## 📝 FILES TO UPDATE (If using Alternative Fix)

Find and update these controller files:

1. **ReportsController.java** - All report methods
2. **SasraReportsController.java** - SASRA report methods  
3. **LoanController.java** - Date-based queries
4. **PaymentController.java** - Payment history
5. **Any other controllers with date parameters**

---

## 🚀 RECOMMENDED APPROACH

**Primary**: Update pom.xml (5 minutes)
**Fallback**: Add @RequestParam annotations (30 minutes per controller)

Use the pom.xml fix first - it's faster and cleaner!

---

**Last Updated**: 2025-10-08  
**Status**: Ready to Apply
