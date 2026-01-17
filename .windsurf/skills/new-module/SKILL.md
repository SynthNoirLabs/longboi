---
name: new-module
description: Scaffold a new feature module for Longboi Launcher with proper structure
---

# New Module Skill

Use this when creating a new feature module for the launcher.

## Steps

1. Ask for the module name (e.g., "search", "widgets")
2. Create directory `feature/{name}/`
3. Create `build.gradle` from template (see `build.gradle.template`)
4. Add to `settings.gradle`
5. Create basic package structure
6. Run `./gradlew help` to verify

## Resources

- `build.gradle.template` - Standard feature module build file
- `structure.md` - Expected directory structure
