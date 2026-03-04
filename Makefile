# Makefile for Longboi Launcher
# Provides convenient commands for common development tasks

.PHONY: help visual-audit test test-unit test-instrumented coverage lint format assemble clean

# Default target
help:
	@echo "Longboi Launcher - Automation Commands:"
	@echo ""
	@echo "Visuals & Audit:"
	@echo "  visual-audit      - Generate screenshots of current app state (requires device)"
	@echo ""
	@echo "Testing & Quality:"
	@echo "  test              - Run all tests (unit + instrumented)"
	@echo "  test-unit         - Run unit tests"
	@echo "  test-instrumented  - Run instrumentation tests (requires device)"
	@echo "  coverage          - Generate test coverage report"
	@echo "  lint              - Run Android lint checks"
	@echo "  format            - Format code with Spotless"
	@echo ""
	@echo "Build & Maintenance:"
	@echo "  assemble          - Build debug APK"
	@echo "  clean             - Clean build artifacts"
	@echo ""
	@echo "Example:"
	@echo "  make visual-audit # See the current state of the app"

# Visual Audit: Capture the current state of the app
visual-audit:
	@echo "📸 Generating visual state audit..."
	@./generate_screenshots.sh

# Run all tests
test: test-unit test-instrumented
	@echo "✅ All tests completed"

# Run unit tests
test-unit:
	@echo "🧪 Running unit tests..."
	./gradlew testDebugUnitTest

# Run instrumentation tests
test-instrumented:
	@echo "🧪 Running instrumentation tests..."
	./gradlew connectedAndroidTest

# Generate coverage report
coverage:
	@echo "📊 Generating test coverage..."
	./gradlew koverXmlReport
	@echo "✅ Coverage report: build/reports/kover/report.xml"

# Format code
format:
	@echo "🎨 Formatting code..."
	./gradlew spotlessApply
	@echo "✅ Code formatted"

# Build debug version
assemble:
	@echo "📦 Building debug APK..."
	./gradlew assembleDebug
	@echo "✅ APK: app/build/outputs/apk/debug/app-debug.apk"

# Run lint checks
lint:
	@echo "🔍 Running lint checks..."
	./gradlew lintDebug
	@echo "✅ Lint completed"

# Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	./gradlew clean
	@echo "✅ Clean completed"
