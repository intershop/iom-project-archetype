# IOM 6 Project Migration — Agent Instructions

You are migrating an existing IOM customer project from IOM 5 to IOM 6.

Read `iom6-project-migration-strategy.md` before starting — it contains the rationale and decisions behind every step below. This document contains only the executable steps.

The project to migrate is in the directory provided by the user. All paths below are relative to that project root.

---

## Rules

1. **Never change enum constant IDs, names, or JNDI strings.** These are project-registered values that exist in the database. Only change imports, annotations, and constructor argument types where the IOM 6 API requires it.
2. **Preserve all project-specific content.** Dependencies, pipeline stages, helm values, SQL scripts — only change what is listed below.
3. **Apply semantic changes only.** Ignore whitespace, import ordering, and brace style differences. A change is semantic if it affects imports, annotations, field types, method signatures, or referenced class names.
4. **Before deleting an archetype-provided file, read it.** If it contains project-specific logic beyond the archetype template, do not delete it — migrate the project-specific logic to the platform equivalent instead.
5. **Commit each section separately** with a clear message so the migration is reviewable step by step.
6. **Maintain a running migration protocol throughout.** For every file touched, every file skipped, and every decision made, record an entry in the protocol (see Step 10 for the format). Do not wait until the end — record each entry as you go.

---

## Step 1 — Read the project

Before making any changes, run:

```bash
find . -name "pom.xml" | head -20
find . -name "*.java" -path "*/src/main/java/*" | sort
find . -name "helm-values*.yaml" | sort
find . -name "azure-pipelines.yml"
find . -name "Expanded*DefDO.java" | sort
```

Understand the structure. Note any files that deviate from the standard archetype layout — those are project additions and must be preserved.

---

## Step 2 — `pom.xml` (root)

Read `pom.xml`. Apply these changes:

**Version properties** — find and update:

- `<platformVersion>` or `<platform.version>`: set to `6.0.0`
- `<wildfly.version>`: set to `40.0.0.Final`
- `<testframework.version>`: set to `8.0.0` — `iom-test-framework` tracks IOM infrastructure changes; 8.0.0 is required for IOM 6 / Java 21 compatibility
- `<postgresql>` version: set to `42.7.11`
- Maven compiler `<release>`: set to `21`
- `<org.junit.version>`: **do not change** — JUnit is independent of the IOM platform version. Upgrading JUnit 5 → 6.1.0 requires test code changes and is a separate follow-up task, not part of the IOM 6 migration. If present,  record in the protocol that this was intentionally left unchanged and add it in the  "Untouched items worth updating" paragraph.
- `<org.mockito.version>`: **do not change** — Upgrading mockito from  4.6.1 to 4.11.0 may require test code changes and is a separate follow-up task, not part of the IOM 6 migration. If mockito is present,  record in the protocol that this was intentionally left unchanged and add it in the  "Untouched items worth updating" paragraph.
- `<net.bytebuddy.version>`: **do not change** — Upgrading bytebuddy from 1.12 to 1.18.8 may require test code changes and is a separate follow-up task, not part of the IOM 6 migration. If bytebuddy is present,  record in the protocol that this was intentionally left unchanged and add it in the  "Untouched items worth updating" paragraph.

**Transitive effects of `iom-test-framework` 8.0.0 — additional checks:**

*SLF4J logging backend:* Search `pom.xml` for `logback` or `log4j`. If found:

- Logback: ensure its version is `1.4.x` or newer. If older, update it.
- Log4j2: ensure the SLF4J bridge artifact is `log4j-slf4j2-impl` (with `2` suffix). If the project uses `log4j-slf4j-impl` (without the `2`), change it. Record the finding in the protocol.

*`jackson-annotations` pin:* Check `<dependencyManagement>` for an explicit `com.fasterxml.jackson.core:jackson-annotations` version. If it is less than `2.21`, update it to `2.21`. If it is `2.21` or absent, no action needed. Record the finding in the protocol.

*Flyway direct API usage in tests:* Run:

```bash
grep -rn "import org.flywaydb" src/test/
```

If any results are found, record them in the protocol under "Follow-up tasks" — direct Flyway API usage may require changes due to Flyway 9.5.0 → 12.8.1 (PostgreSQL support moved to a separate module in Flyway 10). Do not attempt an automated fix; flag for manual review.

**Dependencies to remove** (remove the entire `<dependency>` block for each):

- `resteasy-core-spi`
- `resteasy-client`
- `slf4j-api` with `<scope>provided</scope>`
- `slf4j-simple` with `<scope>test</scope>`
- `jackson-datatype-jsr310`

**Dependencies to change:**

- `commons-lang3`: change `<scope>` to `provided` (add the scope element if absent)

**Dependencies to add** (if not already present):

```xml
<dependency>
    <groupId>com.intershop.oms</groupId>
    <artifactId>rest</artifactId>
    <version>${platform.version}</version>
    <scope>compile</scope>
</dependency>
```

**Plugin versions** — read the full `<pluginManagement>`, `<build><plugins>`, and `<reporting>` sections. For every plugin found, decide:

- If it is in the table below: update its `<version>` to the target version. Preserve any existing `<configuration>` block unchanged.
- If it is **not** in the table below: it is a project-specific plugin. Do not change it. Add an entry to the migration protocol under a "Follow-up tasks" section with the plugin `groupId:artifactId`, its current version, and a note that the version should be reviewed as a follow-up to the IOM 6 migration.

Target versions for archetype-provided plugins:

| Plugin | Target version |
| --- | --- |
| `maven-dependency-plugin` | `3.11.0` |
| `maven-clean-plugin` | `3.5.0` |
| `maven-surefire-plugin` | `3.5.6` |
| `maven-failsafe-plugin` | `3.5.6` |
| `maven-war-plugin` | `3.5.1` |
| `maven-compiler-plugin` | `3.15.0` |
| `maven-assembly-plugin` | `3.8.0` |
| `velocity-maven-plugin` | `1.1.3` |
| `maven-enforcer-plugin` | `3.6.3` |
| `maven-site-plugin` | `4.0.0-M16` |
| `exec-maven-plugin` | `3.6.3` |
| `jacoco-maven-plugin` | `0.8.14` |
| `maven-antrun-plugin` | `3.2.0` |
| `maven-release-plugin` | `3.3.1` |
| `maven-deploy-plugin` | `3.1.4` |
| `maven-install-plugin` | `3.1.4` |
| `maven-resources-plugin` | `3.5.0` |
| `versions-maven-plugin` | `2.21.0` |

**`maven-clean-plugin` configuration** — check whether the project's `<build><plugins>` section already contains a `maven-clean-plugin` entry with `<excludeDefaultDirectories>true</excludeDefaultDirectories>`. If it does not, add the following. This configuration is **critical for devenv-4-iom**: it prevents `mvn clean` from deleting the `target/` directory itself (only its contents), so that bind-mounts used by the dev environment remain intact.

```xml
<plugin>
    <artifactId>maven-clean-plugin</artifactId>
    <configuration>
        <excludeDefaultDirectories>true</excludeDefaultDirectories>
        <filesets>
            <fileset>
                <directory>${project.build.directory}</directory>
                <includes>
                    <include>**/*</include>
                </includes>
                <followSymlinks>false</followSymlinks>
            </fileset>
        </filesets>
    </configuration>
</plugin>
```

If a `maven-clean-plugin` configuration already exists, read it carefully — it may contain project-specific exclusions. Preserve those and only add `<excludeDefaultDirectories>true</excludeDefaultDirectories>` if it is missing. Record the decision in the protocol.

Commit: `fix: update pom.xml for IOM 6 (Java 21, WildFly 40, platform 6.0.0)`

---

## Step 3 — `dependency-helper/pom.xml`

If this file exists, read it and remove the `<dependency>` block for `order-state-app`:

```xml
<dependency>
    <groupId>com.intershop.oms</groupId>
    <artifactId>order-state-app</artifactId>
    ...
</dependency>
```

This module was removed in IOM 6.

Commit: `fix: remove order-state-app from dependency-helper (removed in IOM 6)`

---

## Step 4 — Expanded enum files

**Pattern rule:** Every `Expanded*DefDO` file must have `@ExpandedEnum(...)` and must **not** have `@Entity`, `@Table`, or `@Configuration`. Before making any changes, scan all `Expanded*DefDO.java` files and remove those three annotations (and their imports) wherever found.

### 4a — `ExpandedExecutionBeanKeyDefDO.java`

Find this file. If exists: Read it. Apply:

- Replace import `bakery.persistence.annotation.PersistedEnumerationTable` with `bakery.persistence.annotation.ExpandedEnum`
- Replace class annotation `@PersistedEnumerationTable(ExecutionBeanKeyDefDO.class)` with `@ExpandedEnum(ExecutionBeanKeyDefDO.class)`

Do not change anything else — not the enum constants, not the IDs, not the names.

### 4b — `ExpandedDocumentMapperDefDO.java`

Find this file. If exists: Read it. No annotation changes are required — `@ExpandedEnum` is already present and is the only class-level annotation needed. If `@Entity`, `@Table`, or `@Configuration` are present, remove them and their corresponding imports.

### 4c — `ExpandedPaymentDefDO.java`

Find this file. If exists: Read it carefully. No annotation changes are required — `@ExpandedEnum` is already present and is the only class-level annotation needed. If `@Entity`, `@Table`, or `@Configuration` are present, remove them and their corresponding imports.

For **each enum constant** in the file, apply this rule:

1. **Name and description of placeholder constants** (negative IDs only): if the name, description or payment is a real-sounding payment name (e.g. `"AfterPay"`), replace it with an obviously generic placeholder such as `"whateverName"` / `"whateverDescription"` / `"whateverPayment"`. A name like `"AfterPay"` combined with `EnumPayment.NO_PAYMENT` is contradictory and misleads readers. For project-specific constants (positive IDs), preserve the attributes unchanged.

Do not change the other 18 `Expanded*DefDO.java` files — they require no IOM 6 changes.

Commit: `fix: update expanded enum files for IOM 6 API changes`

---

## Step 5 — Delete archetype-provided files superseded by the platform

The archetype provided certain files that are now superseded by `com.intershop.oms.rest.*` in IOM 6. These files are typically located under the package the project was generated with (often `com.intershop.oms.ps`), but their exact path depends on the package name chosen at generation time.

First, locate the files by their class names — use `find` rather than assuming a fixed path:

```bash
find src/main/java -name "DefaultOptionsExceptionHandler.java" \
  -o -name "ExceptionHandler.java" \
  -o -name "JacksonObjectMapperProvider.java" \
  -o -name "BasicAuthSecurityContext.java" \
  -o -name "CORSFilter.java" \
  -o -name "IOMAuthFilter.java" \
  -o -name "SLF4JContainerLoggingHandler.java" \
  -o -name "SLF4JWriterInterceptor.java"
```

For each file found, apply this procedure:

**Note:** Even for files that are kept (modified or with callers), check whether they import any of the removed dependencies (`resteasy-core-spi`, `resteasy-client`, `slf4j-api`, `jackson-datatype-jsr310`). If they do, remove those imports and any calls that depend on them, since the underlying jars are no longer on the classpath. Record in the protocol.

**Check condition 1 — is the file unmodified from the archetype original?**

1. Find the archetype version: check the project's `pom.xml` for a `<plugin><artifactId>maven-archetype-plugin</artifactId>` entry, or look at the earliest git commits for an archetype version reference. The version is typically noted as a comment or property (e.g. `archetypeVersion=2.6.0`).
2. Read the corresponding file from the `iom-project-archetype` repository at that release tag (e.g. `git show 2.6.0:src/main/resources/archetype-resources/src/main/java/.../FileName.java` in the archetype repo, stripping the `${package}` placeholders mentally).
3. Compare the two files semantically. The following differences do **not** count as modifications — treat the file as unmodified if only these are present:
   - Whitespace (indentation, blank lines, trailing spaces)
   - Import reordering
   - Method reordering within the class
   - Comment additions or changes
   - Brace style or other formatting differences
4. The following **do** count as modifications — condition 1 fails if any of these are present:
   - Added, removed, or renamed methods or fields
   - Changed method signatures, return types, or parameter types
   - Added, removed, or changed annotations
   - Changed logic within a method body
   - Added or removed imports that reflect new dependencies

**Check condition 2 — is the class referenced anywhere in the project?**

Search the entire source tree — project-specific callers can be in any package, not only alongside the file being checked:

```bash
grep -rn "ClassName" src/main/java/
```

(replace `ClassName` with the simple class name of the file being checked)

**Decision:**

- Both conditions met → delete the file
- File was modified (condition 1 fails) → do NOT delete; adapt the file's content to work with IOM 6 instead; flag for manual review
- Other code uses it (condition 2 fails) → do NOT delete; migrate the callers to use the platform equivalent first, then delete; flag for manual review if the migration is non-trivial
- Both conditions fail → flag for manual review

Platform equivalents:

| Simple class name | Platform replacement |
| --- | --- |
| `DefaultOptionsExceptionHandler` | `com.intershop.oms.rest.exceptions.DefaultOptionsMethodExceptionMapper` |
| `ExceptionHandler` | `com.intershop.oms.rest.exceptions.ExceptionHandler` |
| `JacksonObjectMapperProvider` | `com.intershop.oms.rest.provider.JacksonContextResolver` |
| `BasicAuthSecurityContext` | `com.intershop.oms.rest.authentication.BasicSecurityContext` |
| `CORSFilter` | `com.intershop.oms.rest.provider.CORSFilter` |
| `IOMAuthFilter` | `com.intershop.oms.rest.provider.AuthenticationFilter` |
| `SLF4JContainerLoggingHandler` | `com.intershop.oms.rest.logging.LoggingHandler` |
| `SLF4JWriterInterceptor` | `com.intershop.oms.rest.logging.LoggingWriterInterceptor` |

Note: `AuthenticationFilter` from the platform is not `@Provider`-annotated — callers that previously relied on auto-scan must explicitly register it.

Commit: `fix: remove archetype-provided files now superseded by IOM 6 platform`

---

## Step 6 — Update archetype-provided files that must be kept

Locate each file by class name using `find src/main/java -name "FileName.java"`. The files may be in any package.

### `DynamicLoggingFeature.java`

Read the file. Replace:

- All references to `SLF4JContainerLoggingHandler` → `LoggingHandler` (from `com.intershop.oms.rest.logging`)
- All references to `SLF4JWriterInterceptor` → `LoggingWriterInterceptor` (from `com.intershop.oms.rest.logging`)
- Update imports accordingly; remove the old `SLF4JContainerLoggingHandler` and `SLF4JWriterInterceptor` imports
- Update imports accordingly; remove the old `SLF4JContainerLoggingHandler` and `SLF4JWriterInterceptor` imports

Check for furthetr usages of  `SLF4JContainerLoggingHandler` and  `SLF4JWriterInterceptor` in other java classes:
 - when found, update them using the same replacements as in `DynamicLoggingFeature.java`

### `ClientBuilder.java`

Read the file. Replace:

- `SLF4JWriterInterceptor` → `LoggingWriterInterceptor` (from `com.intershop.oms.rest.logging`)
- Keep `SLF4JClientLoggingHandler` unchanged (no platform equivalent)
- Update imports accordingly

### `LoggingIOStreamHandler.java` (if still present)

Read the file. Remove any methods that use `org.apache.http.*` types as parameters or return types. Remove the corresponding `import org.apache.http.*` statements. Keep all other methods unchanged.

### `MaskedHeaders.java` (if still present)

Read the file. Remove any methods that use `org.apache.http.Header` as a parameter or return type. Remove the `import org.apache.http.Header` statement. Keep all other methods unchanged.

### `RestServiceApplication.java`

Read the file. Remove any references to the deleted classes (step 5). If `IOMAuthFilter` was registered here, add explicit registration of `com.intershop.oms.rest.provider.AuthenticationFilter`.

### All Java source files — Apache HttpClient scan

WildFly 40 dropped Apache HttpClient 4.x. This may affect not only the archetype-provided files above but any project-specific file in the entire source tree. Run:

```bash
grep -rn "import org.apache.http" src/main/java/
```

For **every** file found — regardless of package or who wrote it — read the file, identify the affected methods, and remove or rewrite them. If a method cannot be replaced without understanding project-specific business logic, flag it for manual review. As for all steps defined in this iom6-project-migration-agent.md file, ensure that all  modifications are recorded in the migration protocol.

### All Java source files — legacy `commons-lang` (2.x) scan

The legacy `commons-lang` 2.x artifact (`commons-lang:commons-lang`, package `org.apache.commons.lang`) was removed from the parent IOM pom in IOM 6. Project source files may still rely on it transitively — e.g. `import org.apache.commons.lang.NotImplementedException`. Since the jar is no longer on the classpath, these imports will no longer resolve and must be migrated to `commons-lang3` (package `org.apache.commons.lang3`). Run:

```bash
grep -rn "import org.apache.commons.lang\." src/main/java/ src/test/java/
```

Note the trailing `.` after `lang` — it matches the 2.x package `org.apache.commons.lang.*` while excluding `org.apache.commons.lang3.*`, which is already correct and must not be touched.

For **every** file found — regardless of package or who wrote it:

1. Replace the import prefix `org.apache.commons.lang.` with `org.apache.commons.lang3.` (e.g. `org.apache.commons.lang.NotImplementedException` → `org.apache.commons.lang3.NotImplementedException`).
2. Verify the referenced class actually exists in `commons-lang3` — most classes moved 1:1, but a few were renamed, relocated, or removed between 2.x and 3.x. If a class has no direct `commons-lang3` equivalent, do not guess: flag the file for manual review in the protocol instead of applying the rename.
3. Leave the rest of the file unchanged.

**If (and only if) at least one match was found**, ensure the project pom declares `commons-lang3` explicitly with `provided` scope (the platform supplies it at runtime, so it must not be bundled). Add the following to the project pom's `<dependencies>` if a `commons-lang3` dependency is not already present; if it is present, apply the scope change already described in Step 2 ("Dependencies to change"):

```xml
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <scope>provided</scope>
</dependency>
```

If no `org.apache.commons.lang.*` imports were found, do not add the dependency. Record the outcome (matches found and migrated, or none found) in the migration protocol.

Commit: `fix: update Java sources to use IOM 6 platform APIs`

---

## Step 7 — `azure-pipelines.yml`

Read the file. A standard generated project delegates CI entirely to `ci-job-template.yml` in the `iom-partner-devops` repository and contains no version-pinned JDK, Helm chart, or Maven task references. In that case, **no changes are needed** — record this in the protocol.

If the project has extended the pipeline with custom stages or jobs, scan those additions for:

- Hardcoded JDK version `17` → change to `21`
- `Maven@4` → revert to `Maven@3` (IOM uses Maven 3.9.x)

If changes were made, commit: `fix: update CI pipeline custom stages for IOM 6`

---

## Step 8 — Helm values files

Run:

```bash
find . -name "helm-values*.yaml"
```

A standard generated project contains **no Helm values files** — Helm deployment is managed centrally by `iom-partner-devops`. If this command finds nothing, record that in the protocol and skip this step.

If Helm values files are found, they are a project-specific addition outside the standard archetype structure. Record each file in the protocol under "Decisions and Observations" and flag them for manual review — they are out of scope for this automated migration.

---

## Step 9 — Build verification

Run:

```bash
mvn clean install
```

If the build fails:

- Compilation errors about missing types → a dependency was not removed or a platform class reference is wrong
- Compilation errors about `org.apache.http` → Apache HttpClient references remain; find and remove them
- Any other error → read the error carefully and apply the minimum fix needed

Do not make speculative changes. Fix only what the compiler reports.

Commit any fixes with: `fix: resolve build errors from IOM 6 migration`

---

## Step 10 — Migration Protocol

Write a migration protocol document named `iom6-migration-protocol.md` in the project root. This is the authoritative record of what was done and why. It must be detailed enough that a reviewer can understand every decision without reading the code themselves.

Structure the protocol as follows:

---

```markdown
# IOM 6 Migration Protocol

**Date:** <date>
**Project:** <project name / artifactId>
**Archetype version used to generate this project:** <version>
**Migrated by:** Claude (iom6-project-migration-agent.md)

## Build Result

<BUILD SUCCESS / BUILD FAILURE — include error summary if failure>

## Changes Applied

For each file that was changed, one entry:

### <file path>
**Action:** <changed / deleted / skipped>
**Reason:** <why this action was taken>
**Details:** <what specifically was changed, or why it was skipped, or what the deletion check found>

## Files Flagged for Manual Review

For each file that could not be automatically handled:

### <file path>
**Issue:** <what was found — modified beyond archetype template / has callers that could not be migrated>
**Details:** <the specific modifications found, or which files are calling it>
**Required action:** <what a human needs to do>

## Follow-up Tasks

### Project-specific out-of-scope items

Items that are out of scope for this automated migration but should be addressed afterwards.

### <groupId:artifactId>
**Current version:** <version found in the project>
**Reason:** Project-specific plugin not covered by the IOM 6 archetype — version was not reviewed as part of this migration.

### Untouched items worth updating

Include untouched items that are probably worth updating as a follow-up.

### <groupId:artifactId>
**Current version:** <version found in the project>
**Target version:** <as used in blueprint project>

## Decisions and Observations

Any notable finding that does not fit the above, e.g.:
- A file that was expected to exist but was absent
- An Apache HttpClient usage that could not be automatically resolved
- Any deviation from the standard migration steps and the reason for it
```

---

Commit the protocol: `docs: add IOM 6 migration protocol`
