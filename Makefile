# Makefile for Longboi Launcher - Windsurf Automation
# Provides convenient commands for common development tasks

.PHONY: help generate validate test format deploy clean skills hooks test-unit test-instrumented test-screenshot test-benchmark coverage lint

# Default target
help:
	@echo "Longboi Launcher - Available Commands:"
	@echo ""
	@echo "Core Commands:"
	@echo "  generate     - Generate skill index and validate configs"
	@echo "  validate     - Validate all skills and workflows"
	@echo "  test         - Run all tests"
	@echo "  test-unit    - Run unit tests only"
	@echo "  test-instrumented - Run instrumentation tests"
	@echo "  test-screenshot - Run screenshot tests"
	@echo "  test-benchmark - Run performance benchmarks"
	@echo "  coverage     - Generate test coverage report"
	@echo "  format       - Format code and documentation"
	@echo "  deploy       - Build and deploy release version"
	@echo ""
	@echo "Android Commands:"
	@echo "  assemble     - Build debug APK"
	@echo "  lint         - Run Android lint checks"
	@echo "  clean        - Clean build artifacts"
	@echo ""
	@echo "Windsurf Commands:"
	@echo "  skills       - List all available skills"
	@echo "  hooks        - Install hooks with proper permissions"
	@echo ""
	@echo "Examples:"
	@echo "  make skills  # Show all available skills"
	@echo "  make test    # Run full test suite"

# Generate configurations and indexes
generate:
	@echo "🔧 Generating configurations..."
	@python3 .windsurf/scripts/skill_discovery.py
	@echo "✅ Skills index generated at .windsurf/SKILLS_INDEX.md"

# Validate all configurations
validate: validate-skills validate-hooks
	@echo "✅ All validations passed"

# Validate skills structure
validate-skills:
	@echo "🔍 Validating skills..."
	@python3 .windsurf/scripts/skill_discovery.py --json > /dev/null
	@echo "✅ Skills validation passed"

# Validate hooks
validate-hooks:
	@echo "🔍 Validating hooks..."
	@test -x .windsurf/hooks/pre_write_code/security.sh || (echo "❌ Security hook not executable" && exit 1)
	@test -x .windsurf/hooks/post_write_code/format.sh || (echo "❌ Format hook not executable" && exit 1)
	@echo "✅ Hooks validation passed"

# Run all tests
test: test-unit test-instrumented
	@echo "✅ All tests completed"

# Run unit tests only
test-unit:
	@echo "🧪 Running unit tests..."
	./gradlew testDebugUnitTest

# Run instrumentation tests
test-instrumented:
	@echo "🧪 Running instrumentation tests..."
	./gradlew connectedAndroidTest

# Run screenshot tests
test-screenshot:
	@echo "📸 Running screenshot tests..."
	./gradlew screenshotTest

# Run performance benchmarks
test-benchmark:
	@echo "📊 Running performance benchmarks..."
	./gradlew :benchmark:connectedAndroidTest

# Generate coverage report
coverage:
	@echo "📊 Generating test coverage..."
	./gradlew koverXmlReport
	@echo "✅ Coverage report generated at build/reports/kover/report.xml"

# Format code and documentation
format:
	@echo "🎨 Formatting code..."
	./gradlew ktlintFormat
	@echo "✅ Code formatted"

# Build debug version
assemble:
	@echo "📦 Building debug APK..."
	./gradlew assembleDebug
	@echo "✅ Debug APK built: app/build/outputs/apk/debug/app-debug.apk"

# Run lint checks
lint:
	@echo "🔍 Running lint checks..."
	./gradlew lintDebug
	@echo "✅ Lint completed"

# Deploy to Play Store (placeholder)
deploy: validate test assemble
	@echo "🚀 Preparing deployment..."
	@echo "TODO: Add Play Store deployment steps"
	@echo "✅ Deployment ready"

# Clean build artifacts
clean:
	@echo "🧹 Cleaning build artifacts..."
	./gradlew clean
	rm -rf .windsurf/dist/
	@echo "✅ Clean completed"

# List all skills
skills:
	@echo "📚 Available Skills:"
	@echo ""
	@python3 .windsurf/scripts/skill_discovery.py

# Install hooks with proper permissions
hooks:
	@echo "🔗 Installing hooks..."
	@chmod +x .windsurf/hooks/pre_write_code/security.sh
	@chmod +x .windsurf/hooks/post_write_code/format.sh
	@echo "✅ Hooks installed"

# Quick development setup (run after cloning)
setup: hooks generate
	@echo "⚡ Development setup complete!"
	@echo ""
	@echo "Next steps:"
	@echo "1. Run 'make skills' to see available skills"
	@echo "2. Run 'make test' to verify setup"
	@echo "3. Start developing!"

# CI/CD pipeline
ci: validate test-unit lint coverage
	@echo "✅ CI pipeline completed successfully"
