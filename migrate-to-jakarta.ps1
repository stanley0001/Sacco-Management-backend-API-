# Migration script from javax to jakarta for Spring Boot 3.x
# Run this from the project root directory

Write-Host "Starting migration from javax.* to jakarta.*..." -ForegroundColor Green

# Get all Java files
$javaFiles = Get-ChildItem -Path "src" -Filter "*.java" -Recurse

$totalFiles = $javaFiles.Count
$modifiedFiles = 0

Write-Host "Found $totalFiles Java files to process..." -ForegroundColor Yellow

foreach ($file in $javaFiles) {
    $content = Get-Content $file.FullName -Raw
    $originalContent = $content
    
    # Replace javax.persistence with jakarta.persistence
    $content = $content -replace 'import javax\.persistence\.', 'import jakarta.persistence.'
    
    # Replace javax.validation with jakarta.validation
    $content = $content -replace 'import javax\.validation\.', 'import jakarta.validation.'
    
    # Replace javax.servlet with jakarta.servlet
    $content = $content -replace 'import javax\.servlet\.', 'import jakarta.servlet.'
    
    # Replace javax.annotation with jakarta.annotation
    $content = $content -replace 'import javax\.annotation\.', 'import jakarta.annotation.'
    
    # Replace javax.transaction with jakarta.transaction
    $content = $content -replace 'import javax\.transaction\.', 'import jakarta.transaction.'
    
    # Replace javax.xml.bind with jakarta.xml.bind
    $content = $content -replace 'import javax\.xml\.bind\.', 'import jakarta.xml.bind.'
    
    # Only write if content changed
    if ($content -ne $originalContent) {
        Set-Content -Path $file.FullName -Value $content -NoNewline
        $modifiedFiles++
        Write-Host "✓ Modified: $($file.Name)" -ForegroundColor Cyan
    }
}

Write-Host "`n========================================" -ForegroundColor Green
Write-Host "Migration Complete!" -ForegroundColor Green
Write-Host "Total files processed: $totalFiles" -ForegroundColor Yellow
Write-Host "Files modified: $modifiedFiles" -ForegroundColor Green
Write-Host "========================================`n" -ForegroundColor Green

Write-Host "Next steps:" -ForegroundColor Yellow
Write-Host "1. Review the changes with: git diff" -ForegroundColor White
Write-Host "2. Build the project: .\mvnw clean install" -ForegroundColor White
Write-Host "3. Run the application: .\mvnw spring-boot:run" -ForegroundColor White
