#!/bin/bash

# Auto-format Hook for Android Development
# Formats code after file modifications

# Read JSON input from stdin
input=$(cat)
file_path=$(echo "$input" | jq -r '.tool_info.file_path' 2>/dev/null)

# If jq fails, try to extract file_path differently
if [ "$file_path" = "null" ] || [ -z "$file_path" ]; then
    file_path=$(echo "$input" | grep -o '"file_path":"[^"]*"' | cut -d'"' -f4)
fi

# Skip if no file path
if [ -z "$file_path" ]; then
    exit 0
fi

# Get project root directory
project_root=$(git rev-parse --show-toplevel 2>/dev/null)
if [ -z "$project_root" ]; then
    project_root="$PWD"
fi

# Change to project root for Gradle commands
cd "$project_root"

# Format based on file type
case "$file_path" in
    *.kt)
        # Format Kotlin files with ktlint
        if [ -f "gradlew" ]; then
            ./gradlew ktlintFormat "$file_path" 2>/dev/null || echo "Kotlin formatting failed"
        fi
        ;;
    *.gradle|*.gradle.kts)
        # Format Gradle files
        if command -v gradlefmt >/dev/null 2>&1; then
            gradlefmt -w "$file_path" 2>/dev/null || echo "Gradle formatting failed"
        fi
        ;;
    *.xml)
        # Format XML files (if xmllint is available)
        if command -v xmllint >/dev/null 2>&1; then
            xmllint --format "$file_path" > "${file_path}.tmp" 2>/dev/null && mv "${file_path}.tmp" "$file_path"
        fi
        ;;
    *.json)
        # Format JSON files
        if command -v jq >/dev/null 2>&1; then
            jq '.' "$file_path" > "${file_path}.tmp" 2>/dev/null && mv "${file_path}.tmp" "$file_path"
        fi
        ;;
esac

# Output JSON for Windsurf (suppress output for clean operation)
echo '{"status": "formatted", "file": "'$file_path'"}'
exit 0
