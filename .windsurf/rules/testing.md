---
trigger: glob
globs: ["**/test/**/*.kt", "**/androidTest/**/*.kt"]
---

# Testing Rules

- Test ViewModels by emitting events and asserting state transitions
- Use Turbine for testing Flows
- Use fake implementations over mocks where possible
- Screenshot tests go in `screenshotTest` source set
- Name tests descriptively: `given_when_then` or `should_X_when_Y`
