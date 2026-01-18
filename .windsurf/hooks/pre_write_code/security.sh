#!/bin/bash

# Security Hook for Android Development
# Blocks commits with security issues

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

# Check if file exists
if [ ! -f "$file_path" ]; then
    exit 0
fi

# Security checks
security_issues=()

# 1. Check for hardcoded secrets
if grep -E "(API_KEY|SECRET|PASSWORD|TOKEN).*=.*[\"'][^\"']+[\"']" "$file_path" > /dev/null 2>&1; then
    security_issues+=("❌ Hardcoded secret detected - use BuildConfig")
fi

# 2. Check for HTTP usage (excluding localhost)
if grep -E "http://[^localhost]" "$file_path" > /dev/null 2>&1; then
    security_issues+=("⚠️ HTTP detected - use HTTPS")
fi

# 3. Check for debug logs in production files
if [[ "$file_path" != *"test"* ]] && [[ "$file_path" != *"androidTest"* ]]; then
    if grep -E "Log\.[dew]" "$file_path" > /dev/null 2>&1; then
        security_issues+=("⚠️ Debug logs found - remove in production")
    fi
fi

# 4. Check for weak encryption
if grep -E "Cipher\.getInstance.*\"(DES|MD5|SHA1)\"" "$file_path" > /dev/null 2>&1; then
    security_issues+=("❌ Weak encryption algorithm detected")
fi

# 5. Check for SQL injection risks
if grep -E "execSQL.*\+" "$file_path" > /dev/null 2>&1; then
    security_issues+=("❌ Potential SQL injection - use parameterized queries")
fi

# 6. Check for WebView JavaScript enabled
if grep -E "settings\.javaScriptEnabled = true" "$file_path" > /dev/null 2>&1; then
    security_issues+=("⚠️ JavaScript enabled in WebView - ensure it's necessary")
fi

# Report issues
if [ ${#security_issues[@]} -gt 0 ]; then
    echo "Security issues detected in $file_path:"
    printf '%s\n' "${security_issues[@]}"

    # Block for critical issues
    if grep -q "❌" <<< "${security_issues[@]}"; then
        echo ""
        echo "Operation blocked due to critical security issues."
        exit 2
    fi
fi

# Output JSON for Windsurf
echo '{"status": "ok", "file": "'$file_path'", "security_check": "passed"}'
exit 0
