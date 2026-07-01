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

```
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
- `<org.junit.version>`: **do not change** — JUnit is independent of the IOM platform version. Upgrading JUnit 5 → 6 requires test code changes and is a separate follow-up task, not part of the IOM 6 migration. Record in the protocol that this was intentionally left unchanged.

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
    <scope>provided</scope>
</dependency>
```

**Plugin versions** — update `<pluginManagement>` entries to match the versions in the IOM 6 archetype release (see `iom6-project-migration-strategy.md` for the target versions). Do not change project-specific plugin configurations, only the `<version>` tags.

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

### 4a — `ExpandedExecutionBeanKeyDefDO.java`

Find this file. Read it. Apply:
- Replace import `bakery.persistence.annotation.PersistedEnumerationTable` with `bakery.persistence.annotation.ExpandedEnum`
- Replace class annotation `@PersistedEnumerationTable(ExecutionBeanKeyDefDO.class)` with `@ExpandedEnum(ExecutionBeanKeyDefDO.class)`

Do not change anything else — not the enum constants, not the IDs, not the names.

### 4b — `ExpandedDocumentMapperDefDO.java`

Find this file. Read it. Add these three annotations immediately before the `@ExpandedEnum` annotation on the class:

```java
@Entity
@Table(name = "`DocumentMapperDefDO`")
@Configuration
```

Add these imports (in the existing import block, do not reorder existing imports):
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import bakery.persistence.dataobject.Configuration;
```

Do not change anything else.

### 4c — `ExpandedPaymentDefDO.java`

Find this file. Read it carefully.

The constructor for `ExpandedPaymentDefDO` enum constants changed: the last argument is now `EnumPayment` instead of `String`.

Add import:
```java
import bakery.payment.v1.EnumPayment;
```

For **each enum constant** in the file: the last string argument (e.g. `"AfterPay"`, `"None"`, `"CreditCard"`) must be replaced with the appropriate `EnumPayment` enum constant. If you cannot determine the correct constant from the string value, use `EnumPayment.NO_PAYMENT` as a safe default. Do not change the IDs, names, or other arguments.

Do not change the other 18 `Expanded*DefDO.java` files — they require no IOM 6 changes.

Commit: `fix: update expanded enum files for IOM 6 API changes`

---

## Step 5 — Delete archetype-provided files superseded by the platform

The archetype provided certain files that are now superseded by `com.intershop.oms.rest.*` in IOM 6. These files are typically located under the package the project was generated with (often `com.intershop.oms.ps`), but their exact path depends on the package name chosen at generation time.

First, locate the files by their class names — use `find` rather than assuming a fixed path:
```
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
```
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
|---|---|
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

```
grep -rn "import org.apache.http" src/main/java/
```

For **every** file found — regardless of package or who wrote it — read the file, identify the affected methods, and remove or rewrite them. If a method cannot be replaced without understanding project-specific business logic, flag it for manual review.

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
```
find . -name "helm-values*.yaml"
```

A standard generated project contains **no Helm values files** — Helm deployment is managed centrally by `iom-partner-devops`. If this command finds nothing, record that in the protocol and skip this step.

If Helm values files are found, they are a project-specific addition outside the standard archetype structure. Record each file in the protocol under "Decisions and Observations" and flag them for manual review — they are out of scope for this automated migration.

---

## Step 9 — Build verification

Run:
```
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

## Decisions and Observations

Any notable finding that does not fit the above, e.g.:
- A file that was expected to exist but was absent
- An Apache HttpClient usage that could not be automatically resolved
- An enum constant where `EnumPayment` mapping was uncertain and `NO_PAYMENT` was used as a fallback
- Any deviation from the standard migration steps and the reason for it
```

---

Commit the protocol: `docs: add IOM 6 migration protocol`
