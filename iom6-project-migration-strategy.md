# IOM 6 Migration Strategy — Customer IOM Projects

## Purpose and Scope

This document captures the reasoning and decisions behind migrating an existing IOM project from IOM 5 to IOM 6. It is the background companion to `iom6-project-migration-agent.md`, which contains the actionable step-by-step instructions for a Claude agent to execute the migration.

A "customer IOM project" is a project generated from `iom-project-archetype` and subsequently extended by a project team. It is distinct from the archetype itself — the archetype is the template; this document concerns migrating the generated, living project.

The corresponding archetype migration strategy is in `iom6-migration-strategy.md`. The changes needed for a customer project are largely the same, but with different rules around enum constants and the expectation that project-specific code exists alongside the archetype-provided foundation.

---

## Key Differences: Project Migration vs. Archetype Migration

### 1. Enum constants have real, positive IDs

In the archetype template, all enum constants use negative placeholder IDs (e.g. `-9999`) because the archetype must never define real values. In a customer project, enum constants have real positive IDs that were registered in the database — these must **never be changed**. Only the Java annotations, imports, and constructor argument types need to be updated where IOM 6 changed the API.

### 2. Project-specific ps/ files may exist

A project may have added its own files under `src/main/java/com/intershop/oms/ps/` alongside the archetype-provided ones. These must be reviewed for Apache HttpClient 4.x usage (dropped in WildFly 40) and for references to the archetype-provided classes that are now deleted.

### 3. The archetype-provided ps/ files may already have been modified

Unlike the clean archetype template, the project's copy of the archetype-provided ps/ files may have been locally modified. Deletion of platform-superseded files must only happen after confirming the project has not added project-specific logic to them.

### 4. helm values files are project-specific

The project will have multiple `helm-values-*.yaml` files (dev, ci, production, etc.) with real hostnames, database credentials, and resource sizes. Only the infrastructure-level settings need updating (`kubectlImageRepository`, `dbaccount.image.tag`), not the project-specific values.

### 5. azure-pipelines.yml has project-specific customizations

The pipeline may contain project-specific stages, variable groups, and service connections. Only the IOM-version-driven settings need updating (JDK version, Helm chart version). Project-specific content must be preserved.

---

## Required Changes

### 1. `pom.xml` — versions

| Item | IOM 5 value | IOM 6 value |
|---|---|---|
| `platformVersion` property | `5.x.x` | `6.0.0` |
| `wildfly.version` | `30.0.1.Final` | `40.0.0.Final` |
| `org.junit.version` | `5.9.2` | `6.1.0` |
| `testframework.version` | `7.1.0` | `8.0.0` |
| compiler `<release>` | `17` | `21` |
| `postgresql` | `42.7.x` | `42.7.11` |
| Plugin versions | various | align with archetype 3.x.x / ci-project hotfix/6.0 |

The exact plugin versions to target are the same as those applied to the archetype in `iom6-migration-strategy.md` section 1.

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

#### Archetype-provided files (21 files)

These files exist in every IOM project and were generated from the archetype. Three of them require semantic API changes in IOM 6:

**`ExpandedExecutionBeanKeyDefDO.java`**
- Replace annotation: `@PersistedEnumerationTable(ExecutionBeanKeyDefDO.class)` → `@ExpandedEnum(ExecutionBeanKeyDefDO.class)`
- Replace import: `bakery.persistence.annotation.PersistedEnumerationTable` → `bakery.persistence.annotation.ExpandedEnum`

**`ExpandedDocumentMapperDefDO.java`**
- Add three class-level annotations before `@ExpandedEnum`:
  ```java
  @Entity
  @Table(name = "`DocumentMapperDefDO`")
  @Configuration
  ```
- Add imports: `jakarta.persistence.Entity`, `jakarta.persistence.Table`, `bakery.persistence.dataobject.Configuration`

**`ExpandedPaymentDefDO.java`**
- Add import: `import bakery.payment.v1.EnumPayment;`
- For **every enum constant** in this file: the last constructor argument changes from `String` to `EnumPayment`. Replace the last string literal (e.g. `"AfterPay"`, `"None"`, etc.) with the appropriate `EnumPayment` enum constant. For constants where the payment type is "no payment" or unknown, use `EnumPayment.NO_PAYMENT`.

The remaining 18 archetype-provided enum files require no changes.

#### Project-specific enum files

Files present in the project but not in the archetype template are project-specific. These do not need the annotation changes above. However, check them for:
- Any import or use of `order-state-app` classes (very unlikely but possible)
- Any import of `org.apache.http.*` (dropped in WildFly 40)

### 5. `ps/` Java source files — platform consolidation

#### Files to DELETE (replaced by platform `com.intershop.oms.rest.*`)

Before deleting, check whether the project has added project-specific logic to each file. If yes, migrate that logic to the platform equivalent rather than deleting outright.

| File | Platform replacement |
|---|---|
| `ps/rest/DefaultOptionsExceptionHandler.java` | `com.intershop.oms.rest.exceptions.DefaultOptionsMethodExceptionMapper` |
| `ps/rest/ExceptionHandler.java` | `com.intershop.oms.rest.exceptions.ExceptionHandler` |
| `ps/rest/JacksonObjectMapperProvider.java` | `com.intershop.oms.rest.provider.JacksonContextResolver` |
| `ps/rest/filter/BasicAuthSecurityContext.java` | `com.intershop.oms.rest.authentication.BasicSecurityContext` |
| `ps/rest/filter/CORSFilter.java` | `com.intershop.oms.rest.provider.CORSFilter` |
| `ps/rest/filter/IOMAuthFilter.java` | `com.intershop.oms.rest.provider.AuthenticationFilter` |
| `ps/rest/logging/sl4j/SLF4JContainerLoggingHandler.java` | `com.intershop.oms.rest.logging.LoggingHandler` |
| `ps/rest/logging/sl4j/SLF4JWriterInterceptor.java` | `com.intershop.oms.rest.logging.LoggingWriterInterceptor` |

Note: `AuthenticationFilter` from the platform is not `@Provider`-annotated and must be explicitly registered in `RestServiceApplication`.

#### Files to KEEP and UPDATE

**`ps/rest/logging/DynamicLoggingFeature.java`**
- Replace `SLF4JContainerLoggingHandler` → `com.intershop.oms.rest.logging.LoggingHandler`
- Replace `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor`
- Update imports accordingly

**`ps/util/ClientBuilder.java`**
- Replace `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor`
- Keep `SLF4JClientLoggingHandler` if present (no platform equivalent for client-side SLF4J logging)
- Update imports accordingly

**`ps/rest/logging/LoggingIOStreamHandler.java`** (if still present and not platform-replaced)
- Remove any methods using `org.apache.http.*` types (dropped in WildFly 40)
- Remove imports for `org.apache.http.*`

**`ps/rest/logging/MaskedHeaders.java`** (if still present and not platform-replaced)
- Remove any methods using `org.apache.http.Header` (dropped in WildFly 40)
- Remove imports for `org.apache.http.Header`

**`ps/rest/RestServiceApplication.java`**
- Remove references to any of the deleted classes above
- If `IOMAuthFilter` was registered here, replace with explicit registration of `com.intershop.oms.rest.provider.AuthenticationFilter`

#### Any other ps/ file with `org.apache.http.*` imports

WildFly 40 dropped Apache HttpClient 4.x. Scan all remaining ps/ Java files for `import org.apache.http.` and remove or replace those usages. If a method's only purpose was to use Apache HttpClient types and has no alternative, remove the method.

### 6. `azure-pipelines.yml`

**Change:**
- `JDK_MAJOR_VERSION`: `17` → `21`
- `IOM_HELM_VERSION`: update to `3.1.1` (or the version appropriate for IOM 6.0.0)
- Maven task: keep `Maven@3` — IOM uses Maven 3.9.x (do not upgrade to `Maven@4`)

**Preserve all project-specific content** — variable groups, service connections, custom stages, project-specific Maven goals.

### 7. Helm values files

For **each** `helm-values-*.yaml` in the project:

- Add `kubectlImageRepository: "registry.k8s.io/kubectl:v1.32.1"` if not already present. The IOM Helm chart defaults to `docker.io/bitnami/kubectl:1.32.1` which requires Docker Hub authentication; `registry.k8s.io` is the official Kubernetes registry, publicly accessible.
- Update `dbaccount.image.tag` from `2.0.0` → `2.1.0` if not already at `2.1.0` or higher.

---

## What NOT to Change

- Enum constant names, IDs, and JNDI strings — these are project-specific registered values
- Project-specific dependencies in `pom.xml`
- Project-specific stages and jobs in `azure-pipelines.yml`
- Project-specific database names, hostnames, credentials in helm values
- `src/sql-config/` — SQL migration scripts are project-specific and not affected by IOM 6
- `src/etc/`, `src/xsl-templates/`, `src/mail-templates/` — project-specific configuration

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
