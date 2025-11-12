package com.example.demo.system.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generic CSV Data Loader Service
 * Reads CSV files from resources/seed-data folder
 */
@Service
@Slf4j
public class CsvDataLoader {

    private static final String SEED_DATA_PATH = "seed-data/";

    /**
     * Load CSV file and return list of maps (column -> value)
     * @param filename Name of CSV file (e.g., "permissions.csv")
     * @return List of row data as maps
     */
    public List<Map<String, String>> loadCsvData(String filename) {
        List<Map<String, String>> data = new ArrayList<>();
        String fullPath = SEED_DATA_PATH + filename;
        
        try {
            ClassPathResource resource = new ClassPathResource(fullPath);
            
            if (!resource.exists()) {
                log.warn("CSV file not found: {}", fullPath);
                return data;
            }

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
                
                // Read header line
                String headerLine = reader.readLine();
                if (headerLine == null) {
                    log.warn("Empty CSV file: {}", fullPath);
                    return data;
                }

                String[] headers = parseCsvLine(headerLine);
                log.debug("CSV Headers for {}: {}", filename, String.join(", ", headers));

                // Read data lines
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    
                    // Skip empty lines and comments
                    if (line.isEmpty() || line.startsWith("#")) {
                        continue;
                    }

                    String[] values = parseCsvLine(line);
                    
                    // Create map of column -> value
                    Map<String, String> row = new HashMap<>();
                    for (int i = 0; i < headers.length && i < values.length; i++) {
                        row.put(headers[i].trim(), values[i].trim());
                    }
                    
                    data.add(row);
                }

                log.info("Loaded {} records from {}", data.size(), fullPath);
                
            }
        } catch (Exception e) {
            log.error("Error loading CSV file: {}. Error: {}", fullPath, e.getMessage(), e);
        }

        return data;
    }

    /**
     * Parse a CSV line handling quoted values and commas
     * @param line CSV line to parse
     * @return Array of values
     */
    private String[] parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(current.toString());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        result.add(current.toString());
        return result.toArray(new String[0]);
    }

    /**
     * Get string value from row map
     */
    public String getString(Map<String, String> row, String columnName, String defaultValue) {
        String value = row.get(columnName);
        return (value == null || value.isEmpty()) ? defaultValue : value;
    }

    /**
     * Get integer value from row map
     */
    public Integer getInteger(Map<String, String> row, String columnName, Integer defaultValue) {
        try {
            String value = row.get(columnName);
            return (value == null || value.isEmpty()) ? defaultValue : Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid integer value for column {}: {}", columnName, row.get(columnName));
            return defaultValue;
        }
    }

    /**
     * Get double value from row map
     */
    public Double getDouble(Map<String, String> row, String columnName, Double defaultValue) {
        try {
            String value = row.get(columnName);
            return (value == null || value.isEmpty()) ? defaultValue : Double.parseDouble(value);
        } catch (NumberFormatException e) {
            log.warn("Invalid double value for column {}: {}", columnName, row.get(columnName));
            return defaultValue;
        }
    }

    /**
     * Get boolean value from row map
     */
    public Boolean getBoolean(Map<String, String> row, String columnName, Boolean defaultValue) {
        String value = row.get(columnName);
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        return "true".equalsIgnoreCase(value) || 
               "yes".equalsIgnoreCase(value) || 
               "1".equals(value);
    }
}
