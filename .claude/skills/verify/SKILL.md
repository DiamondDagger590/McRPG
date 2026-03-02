---
name: verify
description: Run the full verified build (clean, test, shadowJar) and report results. Use before marking any task complete.
---

Run the full build and test suite for McRPG:

```bash
cd /home/user/McRPG && ./gradlew verifiedShadowJar
```

Report back:
- Whether all tests passed
- Any compilation errors with file names and line numbers
- Any test failures with the failing test class and method names, and the assertion message
- The output jar path if the build succeeded (format: `build/libs/McRPG-<version>-<hash>.jar`)

If the build fails, do not mark the originating task as complete — diagnose the failure and fix it.
