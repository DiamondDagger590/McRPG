Run `./gradlew verifiedShadowJar` from the project root and report the outcome.

Steps:
1. Execute the build with `./gradlew verifiedShadowJar`.
2. Report:
   - Total tests run / passed / failed / skipped.
   - Whether the shaded jar was produced — show its exact filename under `build/libs/`.
   - Any compilation errors or test failures with the relevant output snippet (not the full log).
3. If everything passed, confirm success in one sentence.
4. If anything failed, show only the failing test names and error messages.
