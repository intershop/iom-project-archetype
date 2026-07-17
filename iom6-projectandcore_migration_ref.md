# IOM Migration Guide: 5.1.x → 6.0.x

This document aggregates all migration knowledge for the IOM 5.1.x → 6.0.x upgrade path. It is structured in two clearly separated parts:

- **Part 1 — IOM Platform (`iom/`)**: changes made to the IOM product source, Docker images, and CI pipeline
- **Part 2 — Customer Project (`ci-project/` and generated projects)**: changes required in any project built on top of IOM

The separation matters because Part 1 is done once by the IOM team; Part 2 is done by every project team that consumes the platform.

---

## Summary of Major Changes in IOM 6

| Area | IOM 5.1 | IOM 6.0 |
|------|---------|---------|
| Java | 17 | 21 |
| WildFly | 30.0.1.Final | 40.0.0.Final |
| Hibernate ORM | 6.2 / 6.4 | 7.x (via WF40 BOM) |
| Spring Framework | 6.0.1 | 7.0.8 |
| Apache Maven (wrapper) | 3.8.6 | 3.9.16 |
| JUnit | 5.8.2 (iom), 5.9.2 (ci-project) | 6.1.0 |
| PostgreSQL JDBC | 42.7.2 | 42.7.11 |
| iom-test-framework | 7.1.0 | 8.0.0 |
| REST platform | archetype `ps/` layer | `com.intershop.oms.rest.*` (platform module) |
| Order State API v1 | present (`order-state-app`) | **removed** |
| Drools / KIE / jBPM deps | present in POM | **removed** |
| Commons Lang | `commons-lang` 2.6 | `commons-lang3` (WildFly-provided) |

The upgrade path from WF30 to WF40 spans ten intermediate WildFly versions (31–40), each documented individually in `doc/wildfly-*.md`. Key code changes accumulated through those intermediate steps are summarized here; the full symptom + root-cause detail is in the per-version documents.

---

# Part 1 — IOM Platform (`iom/`)

## 1.1 Version Changes

Three files must always be updated together — they are not linked by Maven inheritance:

| File | Property | 5.1 value | 6.0 value |
|------|----------|-----------|-----------|
| `iom/pom.xml` | `<wildfly.version>` | `30.0.1.Final` | `40.0.0.Final` |
| `ci/azure-pipelines.yml` | `WILDFLY_VERSION` | `30.0.1.Final` | `40.0.0.Final` |
| `ci-project/pom.xml` | `<wildfly.version>` | `30.0.1.Final` | `40.0.0.Final` |

Verify no fourth location was added: `grep -rn "wildfly.version\|WILDFLY_VERSION"`.

Additional version changes in `iom/pom.xml`:

| Property | 5.1 | 6.0 |
|----------|-----|-----|
| `revision` | `5.1.x-SNAPSHOT` | `6.0.0` |
| Java compiler `<release>` | `17` | `21` |
| Maven minimum (enforcer) | `3.8.6` | `3.9.16` |
| `spring.version` | `6.0.1` | `7.0.8` |
| `junit.version` | `5.8.2` | `6.1.0` |
| `mockito.core.version` | `4.6.1` | `4.11.0` |
| `net.bytebuddy.version` | `1.12.10` | `1.18.8` |
| `io.swagger.core.v3.version` | `2.2.19` | `2.2.50` |
| `quartz.version` | `2.3.2` | `2.5.2` |
| `org.mapstruct.version` | `1.5.2.Final` | `1.6.3` |
| `org.projectlombok.version` | `1.18.22` | `1.18.46` |
| `xmlgraphics.version` | `2.8` | `2.11` |
| `postgresql.version` | `42.7.2` | `42.7.11` |
| `jersey.version` | (absent) | `3.1.11` (new) |
| `sitemesh.version` | (absent) | `2.4.4` (new) |

## 1.2 Dependency Changes in `iom/pom.xml`

**Removed:**
- `jbpm.version` property and all `org.kie:*`, `org.drools:*`, `org.jbpm:*` entries — these were dead POM entries; no module used them
- `commons-lang:commons-lang` 2.6 — replaced by `commons-lang3` (WildFly-provided)
- `com.squareup.okhttp:okhttp`, `okhttp:logging-interceptor`, `com.squareup.okio:okio` — were a dead dependency of the old `okhttp` OpenAPI generator; `bakery.omt` now uses the `jersey3` generator
- JBoss public repository (`repository.jboss.org`)

**Added:**
- `infinispan-core` 16.0.11 to the category-3 block (WF35: Infinispan moved out of BOM; artifact renamed from `infinispan-core-jakarta` → `infinispan-core`)
- `jersey.version` property; Jersey artifacts consolidated to a single version (3.1.11)
- `avalon-framework-api/impl` 4.3.1

**Changed:**
- `jaxb-impl`: `2.3.3-b02` → `4.0.9`
- `Saxon-HE`: `9.7.0-1` → `12.9`
- `jbossws-api`: `1.1.2.Final` → `3.0.0.Final`
- `velocity-engine-core`: `2.3` → `2.4.1`
- `guava`: `32.1.2-jre` → `33.6.0-jre`
- `slf4j-simple`: `2.0.9` → `2.0.17`
- `sitemesh`: `2.4.2` → `2.4.4`
- `jsch`: `0.2.15` → `2.28.2`
- `commons-vfs2`: `2.9.0` → `2.10.0`

## 1.3 Module Removed: `order-state-app`

The Order State REST API v1 (`order-state-app`) is fully removed in IOM 6. This includes:
- The Maven module `iom/order-state-app/` and its `<module>` entry in `iom/pom.xml`
- 62 source files including all REST resources, model classes, and persistence objects
- The OpenAPI YAML `iom/kubernetes.deployment/doc/REST/intershop-order-management-order-state-1.0.yaml`
- 18 `OrderState*PO.java` persistence objects in `iom/bakery.persistence/common-order/`

## 1.4 REST Platform Module (`iom/rest/`)

The `iom/rest/` module (`com.intershop.oms.rest.*`) is the authoritative platform version of REST utilities that were previously maintained as archetype `ps/` code in each customer project. The following classes now live in the platform:

| Platform class | Package |
|---------------|---------|
| `AuthenticationFilter` | `com.intershop.oms.rest.provider` |
| `CORSFilter` | `com.intershop.oms.rest.provider` |
| `JacksonContextResolver` | `com.intershop.oms.rest.provider` |
| `ExceptionHandler` | `com.intershop.oms.rest.exceptions` |
| `DefaultOptionsMethodExceptionMapper` | `com.intershop.oms.rest.exceptions` |
| `LoggingHandler` | `com.intershop.oms.rest.logging` |
| `LoggingWriterInterceptor` | `com.intershop.oms.rest.logging` |
| `BasicSecurityContext` | `com.intershop.oms.rest.authentication` |

Note: `AuthenticationFilter` is **not** `@Provider`-annotated — it must be explicitly registered in the JAX-RS `Application` class.

## 1.5 Code Changes Accumulated Across WF30 → WF40

The ten WildFly upgrades each produced a dedicated migration document in `doc/wildfly-*-to-*-migration.md`. The most significant platform-level code changes are summarized here.

### WF30 → WF31: Hibernate 6 dialect detection requires live DB connection

Hibernate 5 could detect the SQL dialect from the JDBC driver class name even if no connection was available at startup. Hibernate 6 requires a live JDBC connection. `OMS_DB_HOSTLIST` must be set correctly wherever WildFly starts (it is separate from `OMS_DB_HOST`, which is used only by shell scripts).

### WF30 → WF31: `@JoinTable` join columns must not have `updatable=false` without `insertable=false`

Hibernate 6 validates bidirectional association consistency at deployment time. Two classes were fixed: `DocumentDO` and `InvoicingDO`. Remove `updatable=false` from `joinColumns` and `inverseJoinColumns` in `@JoinTable` annotations — only `nullable=false` is needed.

### WF35: `infinispan-core-jakarta` → `infinispan-core`

Infinispan 15.x renamed the artifact; three module POMs updated. The artifact dropped from the WildFly BOM (category-2 → category-3) and must be pinned manually.

### WF30 → WF31: byte-buddy scope fix for annotation processor

WildFly 31's BOM upgraded `hibernate-jpamodelgen` from 6.2.x to 6.4.4.Final, which added a hard compile-time dependency on `net.bytebuddy:byte-buddy`. The `dependencyManagement` entry for byte-buddy carried `<scope>test</scope>`, restricting it to the test classpath and breaking annotation processing during the `compile` phase. Fix: remove the `<scope>test</scope>` from the `dependencyManagement` entry (child modules that need test scope declare it explicitly on their own `<dependency>` declaration).

### WF39 → WF40: Hibernate JPA metamodel generator renamed

`org.hibernate.orm:hibernate-jpamodelgen` → `org.hibernate.orm:hibernate-processor`. Update the `artifactId` in `maven-compiler-plugin` `annotationProcessorPaths` in `iom/pom.xml`.

### WF40: Hibernate 7 returns `LocalDateTime` for timestamp columns in native queries

Hibernate ORM 7 changed the default mapping for `timestamp`/`timestamptz` columns in native SQL queries from `java.sql.Timestamp` to `java.time.LocalDateTime`. All `(Timestamp) o[N]` and `(Date) o[N]` casts in native query result mapping throw `ClassCastException` at runtime.

Fix: use helper methods that handle both types:

```java
public static Date toDate(Object value) {
    if (value instanceof LocalDateTime ldt)
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    return (Date) value;
}

public static Timestamp toTimestamp(Object value) {
    if (value instanceof LocalDateTime ldt)
        return Timestamp.valueOf(ldt);
    return (Timestamp) value;
}
```

These helpers are consolidated in `bakery.persistence.bean.util.PersistenceUtils`. Affected beans: `OrderPersistenceBean`, `ArticlePersistenceBean`, `AtpPersistenceBean`, `DbJobPersistenceBean`, `DispatchPersistenceBean`, `ReturnPersistenceBean`, `OrderReportPersistenceBean`, `CancellationPersistenceBean`, `PaymentNotificationPersistenceBean`, `CustomerPersistenceBean`.

### WF40: WildFly 40 dropped Apache HttpClient 4.x

`org.apache.http.*` is no longer bundled by WildFly 40. Remove all `import org.apache.http.*` usages from IOM sources. This is also the driver for removing the archetype-provided `ps/` logging classes that had an HttpClient dependency (now superseded by the platform REST module — see Part 2).

### WF40: JSP compiler rejects empty JSP files

The JDT-based JSP compiler in `jastow-2.3.0.Final` throws `IllegalStateException` when a JSP file generates no Java statements. Replace empty JSP content with `<%@ include file="/common/taglibs.jsp" %>`.

### WF40: CDI stricter injection

`@EJB` injection by implementation class (e.g. `ReturnAnnouncementTransmissionPersistenceBean`) must be replaced by injection by interface (`ReturnAnnouncementTransmissionPersistenceService`).

### WF40: OIDC logout interception

Elytron OIDC in WF40 intercepted IOM's logout URL without an explicit `logout-path` configuration, redirecting users to the OIDC provider's logout endpoint. Set `logout-path` in the Elytron OIDC configuration to give the local IOM logout handler precedence.

### WF40: SOAP endpoint address override in tests

WildFly 40 changed how JAX-WS resolves the effective endpoint address from a WSDL — it uses the server's container-detected hostname instead of the configured address. Tests calling `getXxxService().getXxxServicePort()` directly must override `BindingProvider.ENDPOINT_ADDRESS_PROPERTY` immediately after obtaining the port. Production code in `bakery.logic-core` already does this via `AbstractWebserviceSender.reconfigureBindingProvider()`; only test code calling the port directly is affected.

### WF40: Dead-code NPE in `InvoicingTransmissionLogicBean`

A dead `else` block in `sendTransmission()` called `getMessageTransmitter()` a second time without a null check. Under WF40 JNDI lookup semantics, this returned `null` for some transmitter configurations. The dead block was removed entirely.

## 1.6 Spring Framework 6 → 7

Spring 7 introduced stricter URL path matching. Trailing slashes and redundant inner slashes no longer match controller route patterns (e.g. `/order/10026/200/` no longer matches `/order/{a}/{b}`). A `TrailingSlashFilter` was added to `bakery.omt` to normalize legacy URL shapes before they reach the controller layer.

## 1.7 Jakarta EE Deployment Descriptor Namespaces

21 deployment descriptors migrated from legacy `http://xmlns.jcp.org/xml/ns/javaee` schemas to `https://jakarta.ee/xml/ns/...` schemas:
- `web.xml` (8 files): version 3.1 → 6.0
- `beans.xml` (9 files): version 1.1/2.0 → 4.0
- `persistence.xml` (4 files): version 2.1 → 3.1

Only the root element namespace/schema/version changed — body elements already used `jakarta.*`. JAX-WS files (handlers, WSDLs) were intentionally left untouched.

## 1.8 Commons Lang Migration

`commons-lang:commons-lang` 2.6 removed. All usages migrated to `org.apache.commons.lang3.*` (equivalent APIs with `3` in the package name). `commons-lang3` is now WildFly-provided (covered by the WildFly BOM, category-2) and should not be bundled.

## 1.9 Maven and Tooling

| Item | 5.1 | 6.0 |
|------|-----|-----|
| Maven wrapper | 3.8.6 | 3.9.16 |
| `maven-compiler-plugin` | 3.11.0 | 3.15.0 |
| `maven-surefire-plugin` | 3.2.2 | 3.5.6 |
| `maven-failsafe-plugin` | 3.2.2 | 3.5.6 |
| `jacoco-maven-plugin` | 0.8.11 | 0.8.14 |
| `maven-enforcer-plugin` | 3.4.1 | 3.6.3 |
| `flatten-maven-plugin` | 1.6.0 | 1.7.3 |
| Hibernate annotation processor | `hibernate-jpamodelgen` | `hibernate-processor` (renamed in Hibernate 7) |

Maven 3.9.16 removes two previous blockers: `jaxb30-maven-plugin` 0.16.0 (previously required Maven ≥ 3.9.0) and `jaxws-maven-plugin` 4.0.4 (required newer Maven Resolver API) can now be upgraded.

## 1.10 Docker Image and CI Changes

**`ci/azure-pipelines.yml` variable changes:**

| Variable | 5.1 | 6.0 |
|----------|-----|-----|
| `WILDFLY_VERSION` | `30.0.1.Final` | `40.0.0.Final` |
| `JDK_MAJOR_VERSION` | `17` | `21` |
| `IOM_HELM_VERSION` | `3.0.0` | `3.1.1` |
| Kubernetes auth | `Kubernetes@1` service connection | `az login` + `kubelogin` |
| Pull secret creation | `KubernetesManifest@0` | Copy `iomdevops-pull-secret` from default namespace |
| Ubuntu base | 22.04 | 24.04 |
| JRE distribution | Temurin JRE 17 | Temurin JRE 21 |

**Ubuntu 24.04 compatibility issues (relevant for local image builds):**

- Pre-existing GID/UID 1000 `ubuntu` user: use `groupmod`/`usermod` to rename to `jboss`
- `sources.list` replaced by DEB822 format: `sed` commands now target `/etc/apt/sources.list.d/ubuntu.sources`
- `jq 1.7` removed `--argfile`: replace with `--slurpfile`; references change from `$VAR` to `$VAR[0]`

**Docker Hub proxy:** CI no longer uses a local image cache (`ci: use docker-hub proxy instead of local image cache`, commit `9558237deb`).

**`iom-project-archetype` CI pipeline** (relevant only when migrating the archetype itself, not customer projects):

| Setting | IOM 5 | IOM 6 |
|---------|-------|-------|
| `pool` | static `ubuntu-20.4-DS2_v2-adopt-adoptium-jdk` | variable from `azure-devops-build-machines` group |
| `JDK_MAJOR_VERSION` | `17` | `21` |
| `IOM_HELM_VERSION` | `3.0.0` | `3.1.1` |
| Kubernetes auth | `Kubernetes@1` service connection | `az login` + `kubelogin` |
| Pull secret creation | `KubernetesManifest@0` | copy `iomdevops-pull-secret` from default namespace |
| `dbaccount.image.tag` (helm-values-test.yaml) | `2.0.0` | `2.1.0` |
| `kubectlImageRepository` (helm-values-test.yaml) | (default) | `registry.k8s.io/kubectl:v1.32.1` |

Note on `kubectlImageRepository`: the IOM Helm chart defaults to `docker.io/bitnami/kubectl` which is unavailable in the CI AKS environment. Use `registry.k8s.io/kubectl:v1.32.1` instead — publicly accessible without authentication.

## 1.11 Deprecations Introduced in IOM 6

| Component | Deprecated element | Replacement |
|-----------|-------------------|-------------|
| `bakery.util-io` | `bakery.util.io.AdvancedFileMapper` | None — unused |

## 1.12 Deferred Items (known open at IOM 6.0.0 release)

| Item | Reason deferred |
|------|-----------------|
| `eclipse-transformer-maven-plugin` 1.0.0 | Azure Artifacts proxy caches negative resolution for `biz.aQute.bndlib:7.0.0`; same blocker since WF32 |
| `mockito` 5.x | `mockito-inline` merged into `mockito-core`; requires code review and `byte-buddy` sync |
| `openapi-generator-maven-plugin` 7.x | Major version; generated output must be verified |
| JUnit 5.x → 6.x (iom platform) | Separate task; test code migration required |

---

# Part 2 — Customer Project (`ci-project/` and generated projects)

This part applies to any project generated from `iom-project-archetype` and extended by a project team. The `ci-project/` directory is the reference implementation and serves as the authoritative example.

Automated step-by-step migration instructions for a Claude agent are in `.claude/iom6-project-migration-agent.md`. The detailed decision rationale is in `.claude/iom6-project-migration-strategy.md`. This section documents the complete set of required changes.

**Critical rule on enum constants:** In any customer project, enum constant IDs, names, and JNDI strings must **never be changed** — they are registered in the database. Only Java annotations, imports, and constructor argument types may be updated where the IOM 6 API requires it. (Contrast with the archetype template, where placeholder constants must use negative IDs and generic names.)

## 2.1 `pom.xml` — Version Properties

| Property | IOM 5 value | IOM 6 value |
|----------|-------------|-------------|
| `platform.version` (or `platformVersion`) | `5.x.x` | `6.0.0` |
| `wildfly.version` | `30.0.1.Final` | `40.0.0.Final` |
| `testframework.version` | `7.x.x` | `8.0.0` |
| Java compiler `<release>` | `17` | `21` |
| `postgresql` version | `42.7.x` | `42.7.11` |
| `org.junit.version` | do **not** change | — |
| `org.mockito.version` | do **not** change | — |
| `net.bytebuddy.version` | do **not** change | — |

JUnit, Mockito, and Byte-Buddy are general-purpose test libraries independent of the IOM platform version. Upgrading them (especially JUnit 5 → 6) may require test code changes and should be a separate follow-up task.

## 2.2 `pom.xml` — Dependencies

**Remove** (dropped in IOM 6 / WildFly 40):
- `resteasy-core-spi` (scope: provided)
- `resteasy-client` (scope: provided)
- `slf4j-api` (scope: provided)
- `slf4j-simple` (scope: test)
- `jackson-datatype-jsr310`

**Change scope:**
- `commons-lang3`: `compile` → `provided` (WildFly now provides it)

**Add** if not already present:
```xml
<dependency>
    <groupId>com.intershop.oms</groupId>
    <artifactId>rest</artifactId>
    <version>${platform.version}</version>
    <scope>compile</scope>
</dependency>
```

Note: use `<scope>compile</scope>` — the `rest` module must be bundled by the project WAR since it is not provided by WildFly.

**Preserve** all project-specific dependencies. Do not remove anything not in the list above.

## 2.3 `pom.xml` — Plugin Versions

| Plugin | Target version |
|--------|----------------|
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

Preserve all existing `<configuration>` blocks unchanged. For any plugin in the project that is not in this table, do not change its version — note it as a follow-up candidate.

## 2.4 `pom.xml` — `maven-clean-plugin` Configuration (devenv bind-mount)

Add this configuration if not already present. It prevents `mvn clean` from deleting the `target/` directory itself — only its contents — which is required for `devenv-4-iom` bind-mounts to survive a clean build:

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

If a `maven-clean-plugin` configuration already exists, preserve any project-specific exclusions and only add `<excludeDefaultDirectories>true</excludeDefaultDirectories>` if missing.

## 2.5 `pom.xml` — Maven Artifact Feed

The Azure DevOps feed was renamed from `order-iom-releases` to `iom-maven-artifacts`. If your project's `pom.xml` references `order-iom-releases`, update the feed URL to `iom-maven-artifacts`:

Old: `.../_packaging/order-iom-releases/maven/v1`
New: `.../_packaging/iom-maven-artifacts/maven/v1`

## 2.6 `pom.xml` — Transitive Effects of `iom-test-framework` 8.0.0

`iom-test-framework` 8.0.0 upgrades several transitive dependencies. Check for each:

**SLF4J 2.x logging backend:** Search `pom.xml` for `logback` or `log4j`.
- Logback: must be version `1.4.x` or newer
- Log4j2: must use `log4j-slf4j2-impl` (with `2` suffix); change from `log4j-slf4j-impl` if present

**`jackson-annotations` pin:** If `<dependencyManagement>` explicitly pins `jackson-annotations` to a version older than `2.21`, update it to `2.21`. With WildFly 40 BOM this override is no longer needed (the BOM already ships 2.21); if you added the override for WildFly 30 compatibility (as documented in `ci-project/MIGRATION-IOM-PROJECT_5.2.0.md`), remove it entirely. If absent, no action needed.

**Flyway direct API usage in tests:** Run `grep -rn "import org.flywaydb" src/test/`. If found: Flyway upgraded from 9.5.0 → 12.8.1; since Flyway 10, database-specific support (including PostgreSQL) was moved to separate modules — see the [Flyway 10+ release notes](https://documentation.red-gate.com/flyway/release-notes-and-older-versions). Flag for manual review; do not attempt an automated fix.

**`iom-maven-artifacts` feed access:** `iom-test-framework` 8.0.0 is published to the per-project `iom-maven-artifacts` Azure DevOps feed, not Maven Central. The feed is already configured in the project's `pom.xml`. Developers building locally must have credentials in `~/.m2/settings.xml` (a Personal Access Token from the Azure DevOps environment).

## 2.7 `dependency-helper/pom.xml`

Remove `com.intershop.oms:order-state-app` — this module no longer exists in IOM 6.

## 2.8 `Expanded*DefDO` Enum Files

**Pattern rule (applies to all `Expanded*DefDO` files):**
- `@ExpandedEnum(...)` annotation must be present
- `@Entity`, `@Table`, and `@Configuration` annotations must **not** be present

Scan all `Expanded*DefDO.java` files and remove those three annotations and their imports wherever found.

Two archetype-provided files require specific API changes:

**`ExpandedExecutionBeanKeyDefDO.java`**
- Replace import: `bakery.persistence.annotation.PersistedEnumerationTable` → `bakery.persistence.annotation.ExpandedEnum`
- Replace class annotation: `@PersistedEnumerationTable(ExecutionBeanKeyDefDO.class)` → `@ExpandedEnum(ExecutionBeanKeyDefDO.class)`

**`ExpandedPaymentDefDO.java`**
- Add import: `import bakery.payment.v1.EnumPayment;`
- `EnumPayment` is a Java interface whose fields are `String` constants — `EnumPayment.NO_PAYMENT` is a `String` value, not a new type. The `payment` field type, the constructor's last parameter type, and the `getPayment()` return type all stay `String`. Only the last argument of each enum constant call changes.
- For **every enum constant** in this file: replace the last string literal argument with the appropriate `EnumPayment` constant.
  - For project-specific constants (positive IDs): choose the `EnumPayment` constant that matches the actual payment type configured in the database.
  - For placeholder constants (negative IDs): use `EnumPayment.NO_PAYMENT`.
- Do not change enum constant names, IDs, or JNDI strings.

**`ExpandedDocumentMapperDefDO.java`**: `@ExpandedEnum` is already present and is the only class-level annotation needed. Do **not** add `@Entity`, `@Table`, or `@Configuration`. No other annotation changes required.

The remaining 18 archetype-provided enum files require no changes. Project-specific enum files (not in the archetype) require no annotation changes but should be scanned for `org.apache.http.*` imports and for any import or use of `order-state-app` classes.

## 2.9 Java Source Files — Archetype-Provided Files Superseded by Platform

The following archetype-provided files are superseded by `com.intershop.oms.rest.*` in IOM 6. They are typically in the `ps/` package but may be anywhere, depending on the package name chosen at project generation.

A file may only be deleted when **both** conditions are true:
1. The file itself contains no project-specific logic (semantically identical to the archetype original — whitespace, import order, and comment differences do not count)
2. No other file in the project imports or references this class

| Class to delete | Platform replacement |
|----------------|---------------------|
| `DefaultOptionsExceptionHandler` | `com.intershop.oms.rest.exceptions.DefaultOptionsMethodExceptionMapper` |
| `ExceptionHandler` | `com.intershop.oms.rest.exceptions.ExceptionHandler` |
| `JacksonObjectMapperProvider` | `com.intershop.oms.rest.provider.JacksonContextResolver` |
| `BasicAuthSecurityContext` | `com.intershop.oms.rest.authentication.BasicSecurityContext` |
| `CORSFilter` | `com.intershop.oms.rest.provider.CORSFilter` |
| `IOMAuthFilter` | `com.intershop.oms.rest.provider.AuthenticationFilter` |
| `SLF4JContainerLoggingHandler` | `com.intershop.oms.rest.logging.LoggingHandler` |
| `SLF4JWriterInterceptor` | `com.intershop.oms.rest.logging.LoggingWriterInterceptor` |

If a file was modified or has callers, do not delete it. Migrate callers to the platform equivalent first; if the file's own content was modified, adapt it in place.

**How to determine "unmodified":** A file is unmodified if it is semantically identical to the archetype original. The following do **not** count as modifications: whitespace, import reordering, method reordering, comment changes, brace style. The following **do** count: added/removed/renamed methods or fields, changed method signatures, changed annotations, changed logic, added/removed imports that reflect new dependencies.

Note: Even for files that are kept (modified or with callers), check whether they import any removed dependency (`resteasy-core-spi`, `resteasy-client`, `slf4j-api`, `jackson-datatype-jsr310`). If so, remove those imports since the underlying jars are no longer on the classpath.

Note: `AuthenticationFilter` from the platform is not `@Provider`-annotated — callers that previously relied on auto-scan must explicitly register it in `RestServiceApplication`.

## 2.10 Java Source Files — Files to Keep and Update

The following archetype-provided files have **no platform equivalent** and must be kept in every project:

| Class | Purpose |
|-------|---------|
| `rest/filter/BasicAuthenticationFilter.java` | Client-side Basic Auth injection filter |
| `rest/filter/BearerAuthenticationFilter.java` | Client-side Bearer token injection filter |
| `rest/filter/ClientCorrelationIdFilter.java` | Client-side correlation ID propagation |
| `rest/filter/ContainerCorrelationIdFilter.java` | Container-side correlation ID propagation |
| `rest/RestServiceApplication.java` | Project's own JAX-RS `Application` class — every project needs its own |
| `rest/logging/sl4j/SLF4JClientLoggingHandler.java` | Client-side SLF4J logging (no platform equivalent) |
| `rest/logging/database/DatabaseClientLoggingHandler.java` | Client-side DB logging |
| `rest/logging/database/DatabaseContainerLoggingHandler.java` | Container-side DB logging |
| `rest/logging/database/DatabaseWriterInterceptor.java` | DB body logging |
| `rest/annotations/Message.java` | `@Message` annotation for DB logging integration |
| `servlet/Heartbeat.java`, `servlet/ImportErrors.java`, etc. | Servlet endpoints |
| `services/configuration/*` | Full custom configuration stack |
| `util/CustomizationUtilityStatic.java` | |
| `util/RestAuthenticationBean.java` | |
| `ordervalidation/ValidateMandatoryPropertiesPTBean.java` | |

The following must be kept but **updated**:

Locate each file by class name using `find src/main/java -name "FileName.java"`.

**`DynamicLoggingFeature.java`**
- Replace `SLF4JContainerLoggingHandler` → `com.intershop.oms.rest.logging.LoggingHandler` (from platform)
- Replace `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor` (from platform)
- Remove the old imports; update imports accordingly
- Also search the entire source tree for further usages of these two class names and apply the same replacements

**`ClientBuilder.java`**
- Replace `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor` (platform class works client-side too)
- Keep `SLF4JClientLoggingHandler` unchanged — no platform equivalent exists for client-side SLF4J logging
- Update imports accordingly

**`RestServiceApplication.java`**
- Remove any import or registration of the deleted classes (see §2.9 table)
- If `IOMAuthFilter` was registered here, replace with explicit registration of `com.intershop.oms.rest.provider.AuthenticationFilter` (not `@Provider`-annotated; auto-scan will not pick it up)

**`LoggingIOStreamHandler.java`** (if still present and not superseded):
- Remove any methods that use `org.apache.http.*` types as parameters or return types
- Remove the corresponding `import org.apache.http.*` statements; keep all other methods unchanged

**`MaskedHeaders.java`** (if still present and not superseded):
- Remove any methods that use `org.apache.http.Header` as a parameter or return type
- Remove the `import org.apache.http.Header` statement; keep all other methods unchanged

**`rest/logging/database/DatabaseContainerLoggingHandler.java`** (if present):
- Replace any reference to `SLF4JContainerLoggingHandler` → `com.intershop.oms.rest.logging.LoggingHandler`
- Update imports accordingly

**`rest/logging/database/DatabaseWriterInterceptor.java`** (if present):
- Replace any reference to `SLF4JWriterInterceptor` → `com.intershop.oms.rest.logging.LoggingWriterInterceptor`
- Update imports accordingly

## 2.11 Java Source Files — Apache HttpClient 4.x Scan

WildFly 40 dropped Apache HttpClient 4.x. Scan the **entire** source tree:

```bash
grep -rn "import org.apache.http" src/main/java/
```

For every file found — regardless of package — remove or replace the `org.apache.http.*` usages. If a method's only purpose was to use Apache HttpClient types and has no alternative, remove the method. Flag for manual review if a replacement requires project-specific business logic knowledge.

## 2.12 `azure-pipelines.yml`

A standard generated project delegates CI entirely to `ci-job-template.yml` in `iom-partner-devops` and contains no version-pinned JDK, Helm chart, or Maven task references. In that case, **no changes are needed** — record this in the migration protocol.

If the project has added custom stages or jobs, scan those additions for:
- Hardcoded JDK version `17` → change to `21`
- `Maven@4` → revert to `Maven@3` (IOM uses Maven 3.9.x)

Preserve all project-specific content.

**Helm values files:** `helm-values-test.yaml` exists only in the `iom-project-archetype` repository itself (used for the archetype's own CI test). It is **not part of generated customer projects** — Helm deployment is managed centrally by `iom-partner-devops`. If Helm values files are found in a customer project, they are a project-specific addition and must be flagged for manual review.

## 2.13 What NOT to Change

- Enum constant names, IDs, and JNDI strings (project-registered DB values that exist in the database)
- Project-specific dependencies in `pom.xml` (only remove those explicitly listed in §2.2)
- `org.junit.version`, `org.mockito.version`, `net.bytebuddy.version` — these are unrelated to the IOM platform and may require separate test code migration
- `src/sql-config/` — project-specific SQL migration scripts are not affected by IOM 6
- `src/etc/`, `src/xsl-templates/`, `src/mail-templates/`, `src/json-config/` — project configuration
- Helm values files (standard projects have none; project-specific ones need manual review)

## 2.14 Build Verification

```bash
mvn clean install
```

A successful build confirms:
- Compilation against IOM 6 platform APIs
- No missing imports from removed dependencies
- No Apache HttpClient references in Java sources

For full integration verification, the CI pipeline must pass — it exercises Helm deployment, DB migration, and platform integration.

## 2.15 Migration Protocol

Document every file touched, skipped, and every decision made in `iom6-migration-protocol.md` at the project root. See `.claude/iom6-project-migration-agent.md` Step 10 for the required structure. The protocol is the primary deliverable alongside the code changes, enabling reviewers to understand every decision without reading diffs.

---

# Appendix: WildFly Migration Document Index

Each intermediate WildFly upgrade is documented individually in `doc/`:

| Document | Notable changes |
|----------|-----------------|
| `wildfly-30-to-31-migration.md` | Hibernate 6 dialect detection requires live DB (`OMS_DB_HOSTLIST`); `@JoinTable` `updatable=false` fix; Ubuntu 22→24 compat; jq `--argfile` removed |
| `wildfly-31-to-32-migration.md` | byte-buddy `<scope>test</scope>` removed from `dependencyManagement` (annotation processor compile-phase fix) |
| `wildfly-32-to-33-migration.md` | — |
| `wildfly-33-to-34-migration.md` | — |
| `wildfly-34-to-35-migration.md` | `infinispan-core-jakarta` → `infinispan-core`; dropped from WF BOM (category-2 → category-3); three module POMs updated |
| `wildfly-35-to-36-migration.md` | — |
| `wildfly-36-to-37-migration.md` | — |
| `wildfly-37-to-38-migration.md` | No code changes; `smallrye-config` override in ci-project removed |
| `wildfly-38-to-39-migration.md` | — |
| `wildfly-39-to-40-migration.md` | Hibernate 7 `LocalDateTime` casts; empty JSP compile failure; CDI injection by interface; OIDC `logout-path`; SOAP `BindingProvider.ENDPOINT_ADDRESS_PROPERTY` override; Apache HttpClient 4.x removed; `hibernate-jpamodelgen` → `hibernate-processor`; dead-code NPE in `InvoicingTransmissionLogicBean` |
