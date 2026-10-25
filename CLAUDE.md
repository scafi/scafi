# CLAUDE.md

Hard rules for working in this repository. They are not suggestions.

## Test-Driven Development

- Write a failing test first, run it, and watch it fail for the expected reason.
- Write the minimum code that makes it pass, then refactor with the tests green.
- No production change without a test that covers it. Bug fixes start with a test that reproduces the bug.
- Tests use ScalaTest and live next to the module they exercise (see `tests/` and each module's `src/test`).
- Run the relevant tests before you call a task done: `sbt "<project>/testOnly <Spec>"`, then `sbt <project>/test`.

## Functional Scala

- Immutable data only: `val`, case classes, immutable collections. No `var` or mutable collections unless performance requires it and a comment says why.
- Pure functions by default. Keep side effects (I/O, randomness, time, actors) at the edges.
- Model absence and failure with types (`Option`, `Either`, `Try`), never `null`. Throw exceptions only at system boundaries.
- Use expressions over statements: `map`/`flatMap`/`fold`/for-comprehensions and pattern matching instead of loops and early `return`.
- Use ADTs (`sealed trait` + case classes/objects) and exhaustive matches.
- Prefer small, composable functions and type classes over inheritance hierarchies.
- Follow the existing style and `scalastyle-config.xml`.

## Commits

- Conventional Commits, enforced by commitlint: `type(scope): subject`.
- One short subject line. Add a body only when it is really needed, and keep it to one or two lines.
- One logical change per commit.
- No attribution of any kind: no `Co-Authored-By`, no "Generated with" lines, no tool or AI mentions in commits or PRs.
