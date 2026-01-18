---
name: brainstorming
description: Use this skill before any creative work or feature development to refine requirements through structured questioning
---

# Brainstorming

## When to Use This Skill

- Before implementing new features
- When requirements are unclear or incomplete
- For architectural decisions
- When multiple approaches are possible

## Process

### 1. Analyze Current Project State

- Read existing code in relevant modules
- Check recent commits for context
- Review existing patterns and conventions
- Understand the current architecture

### 2. Ask Clarifying Questions

#### For Features

- **What's the primary motivation for [feature]?**
  - User experience improvement
  - Business requirement
  - Technical debt reduction
  - Competitive necessity

- **Who is this for?**
  - End users (visible feature)
  - Developers (internal improvement)
  - Operations (maintainability)

- **Should [feature] be:**
  1. Automatic (system-driven)
  2. Manual (user-triggered)
  3. Both with user override

#### For Technical Decisions

- **What are the constraints?**
  - Performance requirements
  - Memory limitations
  - Battery impact
  - Network conditions

- **What are the edge cases?**
  - Error states
  - Empty/null scenarios
  - Network failures
  - Low memory situations

### 3. Present Architecture Options

Always present 2-3 approaches with pros/cons:

```kotlin
// Option 1: StateFlow with sealed events
// Pros: Type-safe, testable, follows UDF
// Cons: More boilerplate

// Option 2: Callback-based
// Pros: Simple, less code
// Cons: Harder to test, potential memory leaks
```

### 4. Get Approval Before Implementation

After presenting the design:

- "Does this architecture look right so far?"
- "Any concerns or modifications?"
- "Ready to proceed with implementation?"

## Example Questions for Android

### UI Features

- "Should this animation be:
  1. Smooth (60fps) but battery intensive
  2. Simple but battery efficient
  3. Adaptive based on battery level?"

### Data Features

- "Should data be:
  1. Cached locally (offline support, more storage)
  2. Always fresh (real-time, more network)
  3. Hybrid (smart sync, complex)"

### Performance

- "What's the acceptable:
  - Load time? (< 500ms for instant, < 2s for normal)
  - Memory usage? (< 50MB for feature)
  - Battery impact?"

## Output Format

After brainstorming, create a design document:

```
docs/plans/YYYY-MM-DD-[feature-name]-design.md
```

Include:

- Problem statement
- Chosen approach with rationale
- Architecture diagram (text-based)
- Implementation phases
- Success criteria

## Integration

This skill should trigger:

- `executing-plans` skill for implementation
- `udf-enforcer` skill for Android features
- `compose-performance` skill for UI features
