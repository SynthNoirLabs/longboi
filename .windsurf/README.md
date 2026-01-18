# Longboi Launcher - Windsurf Configuration

This directory contains the Windsurf AI assistant configuration for Longboi Launcher development. It implements a disciplined engineering approach inspired by Superpowers, with Android-specific optimizations.

## Structure

```
text
.windsurf/
├── skills/              # AI skill definitions
│   ├── brainstorming/           # Requirement refinement
│   ├── systematic-debugging/    # 4-phase debugging
│   ├── executing-plans/         # Implementation with checkpoints
│   ├── testing-anti-patterns/   # Testing quality gates
│   ├── android-debugging/       # Android-specific debugging
│   ├── compose-performance/     # Jetpack Compose optimization
│   ├── udf-enforcer/           # UDF pattern enforcement
│   ├── gradle-optimizer/       # Build optimization
│   └── mobile-security/        # Security review
├── hooks/               # Automation hooks
│   ├── pre_write_code/          # Before file changes
│   └── post_write_code/         # After file changes
├── memories/            # Context persistence
│   └── project-ctx.md           # Project overview
├── scripts/             # Utility scripts
│   └── skill_discovery.py       # Find and list skills
├── workflows/           # Workflow definitions
├── rules/              # Development rules
└── README.md           # This file
```

## Core Skills (Superpowers-inspired)

### 1. Brainstorming

Use before any creative work to refine requirements through structured questioning.

**When to use:**

- Before implementing new features
- When requirements are unclear
- For architectural decisions

### 2. Systematic Debugging

4-phase debugging process: investigate → analyze → hypothesize → implement.

**When to use:**

- For all bug fixes
- When crashes or ANRs occur
- For performance issues

### 3. Executing Plans

Execute documented features with batch processing and human review checkpoints.

**When to use:**

- After design is complete
- When implementing features
- For coordinated multi-task work

### 4. Testing Anti-patterns

Gate function to prevent common testing mistakes.

**When to use:**

- BEFORE writing any tests
- During code reviews
- When reviewing existing tests

## Android-Specific Skills

### 5. Android Debugging

Mobile-specific debugging with logcat analysis and device tools.

### 6. Compose Performance

Optimize Jetpack Compose UI performance with best practices.

### 7. UDF Enforcer

Ensure proper Unidirectional Data Flow pattern in all code.

### 8. Gradle Optimizer

Speed up builds and optimize Gradle configuration.

### 9. Mobile Security

Review code for security vulnerabilities and ensure best practices.

## Automation Hooks

### Security Hook (pre_write_code)

- Blocks commits with hardcoded secrets
- Prevents HTTP usage (enforce HTTPS)
- Checks for debug logs in production
- Validates encryption algorithms

### Format Hook (post_write_code)

- Auto-formats Kotlin files with ktlint
- Formats Gradle files
- Formats XML and JSON files

## Usage

### Quick Start

```bash
# Install hooks and generate configs
make setup

# List all available skills
make skills

# Run validation
make validate
```

### Using Skills

Skills are automatically triggered based on context. You can also reference them explicitly:

- "Use the brainstorming skill to refine this feature"
- "Apply systematic debugging to this crash"
- "Execute this plan using the executing-plans skill"

### Memory System

The memory system maintains context across sessions:

- `project-ctx.md`: Project overview and architecture
- Additional memories can be added for specific contexts

## Development Workflow

1. **Design Phase**: Use `brainstorming` skill
2. **Implementation**: Use `executing-plans` with `udf-enforcer`
3. **Testing**: Apply `testing-anti-patterns` before writing tests
4. **Debugging**: Use `systematic-debugging` for bugs
5. **Performance**: Apply `compose-performance` for UI
6. **Security**: Run `mobile-security` before release

## Configuration

### Hooks Configuration

See `.windsurf/hooks.json` for hook settings.

### Skill Discovery

Run the skill discovery script:

```bash
python3 .windsurf/scripts/skill_discovery.py
```

### Makefile Commands

- `make generate` - Generate skill index
- `make validate` - Validate all configurations
- `make test` - Run test suite
- `make format` - Format code
- `make deploy` - Build for deployment

## Best Practices

### For Developers

1. Always start new features with brainstorming
2. Follow UDF pattern strictly
3. Write tests after applying testing-anti-patterns
4. Use systematic debugging for all issues
5. Run security review before releases

### For Code Reviewers

1. Check UDF compliance
2. Verify no testing anti-patterns
3. Ensure performance considerations
4. Validate security practices
5. Check proper skill usage

## Integration with IDE

The configuration integrates with Windsurf IDE features:

- Skills appear in autocomplete
- Hooks run automatically on file changes
- Memories provide context in chat
- Rules guide code suggestions

## Troubleshooting

### Hooks Not Running

```bash
# Check permissions
make hooks

# Verify hook configuration
cat .windsurf/hooks.json
```

### Skills Not Found

```bash
# Regenerate skill index
make generate

# Check skill structure
python3 .windsurf/scripts/skill_discovery.py --json
```

### Build Issues

```bash
# Validate all configurations
make validate

# Clean and rebuild
make clean
make assemble
```

## Contributing

When adding new skills:

1. Follow the skill structure template
2. Include proper frontmatter
3. Add to skill discovery
4. Update documentation
5. Test with validation

## License

This configuration is part of Longboi Launcher project.
