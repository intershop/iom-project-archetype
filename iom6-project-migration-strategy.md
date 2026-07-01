# IOM 6 Migration Strategy — Customer IOM Projects

## Purpose and Scope

This document captures the reasoning and decisions behind migrating an existing IOM project from IOM 5 to IOM 6. It is the background companion to `iom6-project-migration-agent.md`, which contains the actionable step-by-step instructions for a Claude agent to execute the migration.

The agent produces a migration protocol (`iom6-migration-protocol.md`) in the project root that records every file touched, every decision made, and every item requiring manual follow-up. The protocol is the primary deliverable of the migration process alongside the code changes themselves.

A "customer IOM project" is a project generated from `iom-project-archetype` and subsequently extended by a project team. It is distinct from the archetype itself — the archetype is the template; this document concerns migrating the generated, living project.

The corresponding archetype migration strategy is in `iom6-migration-strategy.md`. The changes needed for a customer project are largely the same, but with different rules around enum constants and the expectation that project-specific code exists alongside the archetype-provided foundation.

---

## Key Differences: Project Migration vs. Archetype Migration

### 1. Enum constants have real, positive IDs

In the archetype template, all enum constants use negative placeholder IDs (e.g. `-9999`) because the archetype must never define real values. In a customer project, enum constants have real positive IDs that were registered in the database — these must **never be changed**. Only the Java annotations, imports, and constructor argument types need to be updated where IOM 6 changed the API.

### 2. Project-specific Java source files may exist anywhere

A project may have added its own Java source files anywhere under `src/main/java/` — not necessarily under the `ps/` package. Project-specific files can have any package name and any directory structure. All Java source files must be scanned for Apache HttpClient 4.x usage (dropped in WildFly 40) and for references to the archetype-provided classes that are now deleted.

### 3. The archetype-provided ps/ files may already have been modified, or used by other project code

Unlike the clean archetype template, the project's copy of the archetype-provided ps/ files may have been locally modified. More importantly, other project code (anywhere in `src/main/java/`) may import and use these classes directly.

A file can only be deleted when **both** of the following are true:
1. The file itself contains no project-specific logic — meaning its content is identical to what the archetype generated when the project was created
2. No other file in the project imports or references this class

**Determining the original archetype content:** The archetype version used to generate the project is recorded in the project's git history — typically the initial commit contains an unmodified archetype output, or the `pom.xml` records the archetype version under `<plugin><artifactId>maven-archetype-plugin</artifactId>`. The authoritative source for any archetype-provided file is the `iom-project-archetype` repository at the corresponding release tag (e.g. tag `2.6.0`). Compare the project file against that tag to determine whether it was modified.

**Definition of "unmodified":** A file is considered unmodified if it is **semantically identical** to the archetype original. The following differences do not count as modifications:
- Whitespace changes (indentation, blank lines, trailing spaces)
- Import reordering
- Method reordering within a class
- Comment additions or changes
- Brace style or other formatting differences

The following **do** count as modifications:
- Added, removed, or renamed methods or fields
- Changed method signatures, return types, or parameter types
- Added, removed, or changed annotations
- Changed logic within a method body
- Added or removed imports that reflect new dependencies

If either condition fails, deletion is not possible. Instead, the callers must be migrated to use the platform equivalent, and the file itself must either be adapted or left in place if its content was also customized.

### 4. Generated projects contain no Helm values files

`helm-values-test.yaml` lives in the `iom-project-archetype` repository root and is used exclusively by the archetype's own CI pipeline to test the archetype itself — it deploys a generated project into AKS to verify the archetype template works. It is not part of the generated project and does not appear in customer projects.

A generated project's `azure-pipelines.yml` delegates CI entirely to the `ci-job-template.yml` in the `iom-partner-devops` repository. Helm deployment, including any Helm values, is managed centrally by that template — the project only provides a `projectEnvName` parameter and WildFly CLI configuration files under `src/etc/env/<name>/`. There are no `helm-values-*.yaml` files to migrate in a customer project.

If a project does contain Helm values files, they are a project-specific addition outside the standard archetype structure. The migration agent must note their existence in the protocol and flag them for manual review — they are out of scope for this automated migration.

### 5. azure-pipelines.yml has project-specific customizations

The pipeline may contain project-specific stages, variable groups, and service connections. Only the IOM-version-driven settings need updating (JDK version, Helm chart version). Project-specific content must be preserved.

---

## Required Changes

### 1. `pom.xml` — versions

| Item | IOM 5 value | IOM 6 value |
|---|---|---|
| `platformVersion` property | `5.x.x` | `6.0.0` |
| `wildfly.version` | `30.0.1.Final` | `40.0.0.Final` |
| `testframework.version` | `7.x.x` | `8.0.0` |
| compiler `<release>` | `17` | `21` |
| `postgresql` | `42.7.x` | `42.7.11` |

**Maven plugin versions** — update the following in `<pluginManagement>` (add the entry if absent):

| Plugin | Target version |
|---|---|
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

These versions are taken from archetype release 3.x (which targets IOM 6.0.0).

**Project-specific plugins not in the table above:**

The project may contain plugins in `<pluginManagement>` or `<build><plugins>` that are not in the archetype and therefore not listed in the table. These plugins are project-specific and are out of scope for the automated migration. For each such plugin found:

- Do not change it
- Add an entry to the migration protocol under "Follow-up tasks" with:
  - The plugin `groupId:artifactId`
  - The current version used in the project
  - A note that the version should be reviewed and updated as a follow-up to the IOM 6 migration

This gives the project team a complete list of plugins that may benefit from an upgrade, without blocking the migration itself.

**Plugin configuration rules:**

- **Preserve** all project-specific plugin `<configuration>` blocks that already exist — they represent intentional project customizations and must not be overwritten.
- **Add** the following `maven-clean-plugin` configuration if it is not already present in the project's `<build><plugins>` section. This configuration is critical for devenv-4-iom: it prevents `mvn clean` from deleting the `target/` directory itself, only deleting its contents. Without it, bind-mounts used by the dev environment break.

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

If the project already has a `maven-clean-plugin` configuration, read it carefully before touching it — it may have project-specific exclusions that must be preserved.

**`testframework.version`** (`iom-test-framework`) must be updated to `8.0.0` because the framework tracks IOM infrastructure changes — version 8.0.0 added Java 21 support and removed the Order State service, making it a required update for IOM 6 compatibility.

**`org.junit.version`** must **not** be updated as part of this migration. JUnit is a general-purpose test library independent of the IOM platform version. A project with many tests written against JUnit 5 APIs will continue to work on IOM 6 with JUnit 5. Upgrading JUnit 5 → 6 is a separate task that may require migrating test implementations; it should be scheduled as a follow-up after the IOM 6 migration is complete, using the JUnit migration documentation as a guide.

### 2. `pom.xml` — dependencies

**Remove** (dropped in IOM 6 / WildFly 40):
- `resteasy-core-spi` (provided)
- `resteasy-client` (provided)
- `slf4j-api` (provided)
- `slf4j-simple` (test)
- `jackson-datatype-jsr310`

**Change scope:**
- `commons-lang3`: compile → `provided`

**Add if not already present:**
- `com.intershop.oms:rest` at `${platform.version}` with `<scope>provided</scope>` — provides the platform logging API (`LoggingHandler`, `LoggingWriterInterceptor`, etc.)

**Preserve** all project-specific dependencies. Do not remove any dependency that is not in the list above.

### 3. `dependency-helper/pom.xml`

**Remove** `com.intershop.oms:order-state-app` — the Order State REST API v1 was removed in IOM 6. This module no longer exists at version `6.0.0` in the Maven feed.

If the project has no `dependency-helper/` module, skip this step.

### 4. Expanded enum files

**Pattern rule — all `Expanded*DefDO` files must follow one single pattern:**
- `@ExpandedEnum(...)` annotation must be present
- `@Entity`, `@Table`, and `@Configuration` annotations must **not** be present on any of these files

#### Archetype-provided files (21 files)

These files exist in every IOM project and were generated from the archetype. Two of them require semantic API changes in IOM 6:

**`ExpandedExecutionBeanKeyDefDO.java`**
- Replace annotation: `@PersistedEnumerationTable(ExecutionBeanKeyDefDO.class)` → `@ExpandedEnum(ExecutionBeanKeyDefDO.class)`
- Replace import: `bakery.persistence.annotation.PersistedEnumerationTable` → `bakery.persistence.annotation.ExpandedEnum`

**`ExpandedDocumentMapperDefDO.java`**
- No annotation changes required. The `@ExpandedEnum` annotation is already present and sufficient. Do **not** add `@Entity`, `@Table`, or `@Configuration`.

**`ExpandedPaymentDefDO.java`**
- Add import: `import bakery.payment.v1.EnumPayment;`
- For **every enum constant** in this file: the last constructor argument changes from `String` to `EnumPayment`. Replace the last string literal with the appropriate `EnumPayment` constant. For project-specific constants, choose the constant that matches the actual payment type. For archetype placeholder constants (negative ID, clearly example values), use `EnumPayment.NO_PAYMENT`.
- The name and description strings of placeholder constants (negative IDs) must be obviously generic — e.g. `"whateverName"`, `"whateverDescription"` — not real payment names like `"AfterPay"`. A name like `"AfterPay"` combined with `EnumPayment.NO_PAYMENT` is contradictory and misleads readers.

The remaining 18 archetype-provided enum files require no changes.

#### Project-specific enum files

Files present in the project but not in the archetype template are project-specific additions. They can be in any package, not necessarily under `enums/expand/`. These do not need the annotation changes above. However, scan all Java source files for:
- Any import or use of `order-state-app` classes (very unlikely but possible)
- Any import of `org.apache.http.*` (dropped in WildFly 40)

### 5. Java source files — platform consolidation and API compatibility

This section applies to **all** Java source files in the project — regardless of package name or directory. Project-specific code can be anywhere under `src/main/java/`.

There are two independent concerns: archetype-provided files that are superseded by the platform (and may need to be deleted or updated), and any project file that uses APIs dropped in IOM 6 / WildFly 40.

#### 5a. Archetype-provided files superseded by the platform

The following archetype-provided files are replaced by `com.intershop.oms.rest.*` in IOM 6. They are typically located under the `ps/` package but their exact path depends on the package name chosen when the project was generated.

A file may only be deleted when both of these conditions are met:
1. The file itself contains no project-specific logic (still matches the original archetype template)
2. No other file in the project imports or references this class

Check both conditions before deleting. If callers exist — in **any** file anywhere in the project — migrate them to the platform equivalent first. If the file itself was modified, its content must be adapted rather than deleted.

| Archetype-provided file (relative to package root) | Platform replacement |
|---|---|
| `rest/DefaultOptionsExceptionHandler.java` | `com.intershop.oms.rest.exceptions.DefaultOptionsMethodExceptionMapper` |
| `rest/ExceptionHandler.java` | `com.intershop.oms.rest.exceptions.ExceptionHandler` |
| `rest/JacksonObjectMapperProvider.java` | `com.intershop.oms.rest.provider.JacksonContextResolver` |
| `rest/filter/BasicAuthSecurityContext.java` | `com.intershop.oms.rest.authentication.BasicSecurityContext` |
| `rest/filter/CORSFilter.java` | `com.intershop.oms.rest.provider.CORSFilter` |
| `rest/filter/IOMAuthFilter.java` | `com.intershop.oms.rest.provider.AuthenticationFilter` |
| `rest/logging/sl4j/SLF4JContainerLoggingHandler.java` | `com.intershop.oms.rest.logging.LoggingHandler` |
| `rest/logging/sl4j/SLF4JWriterInterceptor.java` | `com.intershop.oms.rest.logging.LoggingWriterInterceptor` |

Note: `AuthenticationFilter` from the platform is not `@Provider`-annotated and must be explicitly registered in `RestServiceApplication`.

The following archetype-provided files must be kept but updated — again, they may be in any package:

**`rest/logging/DynamicLoggingFeature.java`**
- Replace `SLF4JContainerLoggingHandler` → `com.intershop.oms.rest.logging.LoggingHandler`
- Replace `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor`
- Update imports accordingly

**`util/ClientBuilder.java`**
- Replace `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor`
- Keep `SLF4JClientLoggingHandler` if present (no platform equivalent for client-side SLF4J logging)
- Update imports accordingly

**`rest/logging/LoggingIOStreamHandler.java`** (if still present and not platform-replaced)
- Remove any methods using `org.apache.http.*` types (dropped in WildFly 40)
- Remove imports for `org.apache.http.*`

**`rest/logging/MaskedHeaders.java`** (if still present and not platform-replaced)
- Remove any methods using `org.apache.http.Header` (dropped in WildFly 40)
- Remove imports for `org.apache.http.Header`

**`rest/RestServiceApplication.java`**
- Remove references to any of the deleted classes above
- If `IOMAuthFilter` was registered here, replace with explicit registration of `com.intershop.oms.rest.provider.AuthenticationFilter`

#### 5b. Apache HttpClient 4.x — all Java source files

WildFly 40 dropped Apache HttpClient 4.x (`org.apache.http.*`). This affects not only the archetype-provided files above but potentially any project-specific Java file. Scan the entire source tree:

```
grep -rn "import org.apache.http" src/main/java/
```

For every file found — regardless of where it lives or who wrote it — remove or replace the `org.apache.http.*` usages. If a method's only purpose was to use Apache HttpClient types and has no alternative, remove the method. If a replacement requires understanding project-specific business logic, flag it for manual review.

### 6. `azure-pipelines.yml`

The generated project's `azure-pipelines.yml` delegates CI entirely to `ci-job-template.yml` in the `iom-partner-devops` repository. It contains no version-pinned JDK, Helm, or Maven task references — those are managed centrally by the template. There is therefore **nothing to change** in a standard generated project's pipeline for the IOM 6 migration.

If the project has added custom stages or jobs beyond the standard delegation (which is allowed by the template), scan them for:
- Hardcoded JDK version references (`17` → `21` if present)
- Maven task version (`Maven@3` is correct; do not upgrade to `Maven@4`)

Preserve all project-specific content.

---

## What NOT to Change

- Enum constant names, IDs, and JNDI strings — these are project-specific registered values
- Project-specific dependencies in `pom.xml`
- `src/sql-config/` — SQL migration scripts are project-specific and not affected by IOM 6
- `src/etc/`, `src/xsl-templates/`, `src/mail-templates/` — project-specific configuration
- Helm values files — standard generated projects do not contain them (see section 4 above)

---

## Verification

After applying all changes, the project must build successfully:

```
mvn clean install
```

A successful build confirms:
- Compilation succeeds against IOM 6 platform APIs
- No missing imports from removed dependencies
- No Apache HttpClient references remain in Java sources

For a full integration verification, the CI pipeline must pass — this exercises Helm deployment, DB migration, and the platform integration.
