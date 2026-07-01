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
- `<org.junit.version>`: set to `6.1.0`
- `<testframework.version>`: set to `8.0.0`
- `<postgresql>` version: set to `42.7.11`
- Maven compiler `<release>`: set to `21`

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

## Step 5 — Delete platform-superseded ps/ files

A file may only be deleted when **both** conditions are true:
1. The file itself contains no project-specific logic (still matches the archetype template pattern — class-level annotations, basic delegation, no business logic)
2. No other file in the project imports or references this class

For each file below, if it exists, apply this procedure:

**Check condition 1** — read the file and assess whether it was modified beyond the archetype template.

**Check condition 2** — grep for any usage of the class in the rest of the project:
```
grep -rn "ClassName" src/main/java/
```
(replace `ClassName` with the simple class name of the file being checked)

**Decision:**
- Both conditions met → delete the file
- File was modified (condition 1 fails) → do NOT delete; adapt the file's content to work with IOM 6 instead; flag for manual review
- Other code uses it (condition 2 fails) → do NOT delete; migrate the callers to use the platform equivalent first, then delete; flag for manual review if the migration is non-trivial
- Both conditions fail → flag for manual review

Files and their platform equivalents:

| File | Simple class name | Platform replacement |
|---|---|---|
| `src/main/java/com/intershop/oms/ps/rest/DefaultOptionsExceptionHandler.java` | `DefaultOptionsExceptionHandler` | `com.intershop.oms.rest.exceptions.DefaultOptionsMethodExceptionMapper` |
| `src/main/java/com/intershop/oms/ps/rest/ExceptionHandler.java` | `ExceptionHandler` | `com.intershop.oms.rest.exceptions.ExceptionHandler` |
| `src/main/java/com/intershop/oms/ps/rest/JacksonObjectMapperProvider.java` | `JacksonObjectMapperProvider` | `com.intershop.oms.rest.provider.JacksonContextResolver` |
| `src/main/java/com/intershop/oms/ps/rest/filter/BasicAuthSecurityContext.java` | `BasicAuthSecurityContext` | `com.intershop.oms.rest.authentication.BasicSecurityContext` |
| `src/main/java/com/intershop/oms/ps/rest/filter/CORSFilter.java` | `CORSFilter` | `com.intershop.oms.rest.provider.CORSFilter` |
| `src/main/java/com/intershop/oms/ps/rest/filter/IOMAuthFilter.java` | `IOMAuthFilter` | `com.intershop.oms.rest.provider.AuthenticationFilter` |
| `src/main/java/com/intershop/oms/ps/rest/logging/sl4j/SLF4JContainerLoggingHandler.java` | `SLF4JContainerLoggingHandler` | `com.intershop.oms.rest.logging.LoggingHandler` |
| `src/main/java/com/intershop/oms/ps/rest/logging/sl4j/SLF4JWriterInterceptor.java` | `SLF4JWriterInterceptor` | `com.intershop.oms.rest.logging.LoggingWriterInterceptor` |

Note: `AuthenticationFilter` from the platform is not `@Provider`-annotated — callers that previously relied on auto-scan must explicitly register it.

Commit: `fix: remove ps/ files now provided by IOM 6 platform`

---

## Step 6 — Update remaining ps/ files

### `ps/rest/logging/DynamicLoggingFeature.java`

Read the file. Replace:
- All references to `SLF4JContainerLoggingHandler` → `LoggingHandler` (from `com.intershop.oms.rest.logging`)
- All references to `SLF4JWriterInterceptor` → `LoggingWriterInterceptor` (from `com.intershop.oms.rest.logging`)
- Update imports accordingly; remove old `ps/rest/logging/sl4j/` imports

### `ps/util/ClientBuilder.java`

Read the file. Replace:
- `SLF4JWriterInterceptor` → `LoggingWriterInterceptor` (from `com.intershop.oms.rest.logging`)
- Keep `SLF4JClientLoggingHandler` unchanged (no platform equivalent)
- Update imports accordingly

### `ps/rest/logging/LoggingIOStreamHandler.java` (if still present)

Read the file. Remove any methods that use `org.apache.http.*` types as parameters or return types. Remove the corresponding `import org.apache.http.*` statements. Keep all other methods unchanged.

### `ps/rest/logging/MaskedHeaders.java` (if still present)

Read the file. Remove any methods that use `org.apache.http.Header` as a parameter or return type. Remove the `import org.apache.http.Header` statement. Keep all other methods unchanged.

### `ps/rest/RestServiceApplication.java`

Read the file. Remove any references to the deleted classes (step 5). If `IOMAuthFilter` was registered here, add explicit registration of `com.intershop.oms.rest.provider.AuthenticationFilter`.

### All other Java source files — Apache HttpClient scan

Project-specific Java files can be anywhere under `src/main/java/` with any package name. Run:

```
grep -rn "import org.apache.http" src/main/java/
```

For each file that still imports `org.apache.http.*`: read the file, identify the affected methods, and remove or rewrite them. If a method cannot be replaced, flag it for manual review.

Commit: `fix: update ps/ files to use IOM 6 platform APIs`

---

## Step 7 — `azure-pipelines.yml`

Read the file. Apply:
- `JDK_MAJOR_VERSION`: change to `21`
- `IOM_HELM_VERSION`: change to `3.1.1`
- Maven task version: if any `Maven@4` is present, revert to `Maven@3` (IOM uses Maven 3.9.x)

Preserve all project-specific content.

Commit: `fix: update CI pipeline for IOM 6 (JDK 21, Helm 3.1.1)`

---

## Step 8 — Helm values files

Find all `helm-values*.yaml` files:
```
find . -name "helm-values*.yaml"
```

For each file:
1. Read it
2. Add `kubectlImageRepository: "registry.k8s.io/kubectl:v1.32.1"` at the top level if not already present
3. Find `dbaccount.image.tag` and update to `2.1.0` if currently below that version

Commit: `fix: update helm values for IOM 6 (kubectl image, dbaccount tag)`

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

## Step 10 — Summary

After a successful build, report:
- Which files were changed
- Which ps/ files were deleted
- Whether any ps/ file was flagged for manual review (project-specific logic detected)
- Whether any Apache HttpClient usages remain that could not be automatically resolved
- Build result
