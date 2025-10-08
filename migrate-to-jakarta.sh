#!/bin/bash
# Migration script from javax to jakarta for Spring Boot 3.x
# Run this from the project root directory

echo "Starting migration from javax.* to jakarta.*..."

# Count total files
total_files=$(find src -name "*.java" | wc -l)
modified_files=0

echo "Found $total_files Java files to process..."

# Process each Java file
find src -name "*.java" -type f | while read file; do
    # Create backup
    # cp "$file" "$file.bak"
    
    # Perform replacements
    if sed -i.bak \
        -e 's/import javax\.persistence\./import jakarta.persistence./g' \
        -e 's/import javax\.validation\./import jakarta.validation./g' \
        -e 's/import javax\.servlet\./import jakarta.servlet./g' \
        -e 's/import javax\.annotation\./import jakarta.annotation./g' \
        -e 's/import javax\.transaction\./import jakarta.transaction./g' \
        -e 's/import javax\.xml\.bind\./import jakarta.xml.bind./g' \
        "$file"; then
        
        # Check if file was modified
        if ! cmp -s "$file" "$file.bak"; then
            echo "✓ Modified: $(basename $file)"
            ((modified_files++))
        fi
        
        # Remove backup
        rm "$file.bak"
    fi
done

echo ""
echo "========================================"
echo "Migration Complete!"
echo "Total files processed: $total_files"
echo "Files modified: $modified_files"
echo "========================================"
echo ""
echo "Next steps:"
echo "1. Review the changes with: git diff"
echo "2. Build the project: ./mvnw clean install"
echo "3. Run the application: ./mvnw spring-boot:run"
