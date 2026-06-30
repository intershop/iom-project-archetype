# IOM 6 Migration Strategy — iom-project-archetype

## Background and Key Insight

The `ci-project` in `../order-iom` is not the reference implementation of the archetype — it is a **project generated from the archetype** that then had project-specific code added and modified on top. It evolved continuously alongside IOM development.

This means: **the current ci-project cannot be used directly as a diff target** for what the archetype needs to change. Doing so was the mistake in the first migration attempt — changes that belong to the ci-project as a project (not to the archetype template) would have been incorrectly pulled into the archetype.

---

## Baseline Analysis

**Archetype version at IOM 5.1.0:** `2.6.0` (targets IOM 5.0.0; ci-project migrated to it at commit `2631ac6278`).

**Reference points used:**
- Archetype baseline: `refs/tags/2.6.0`
- IOM 5 ci-project: `refs/tags/5.1.0` in `order-iom`
- IOM 6 ci-project: `origin/hotfix/6.0` (latest) in `order-iom` — all files taken from here, **except** `platformVersion` which is pinned to `6.0.0` (the release), not the current `6.0.1-SNAPSHOT` tip of that branch.

### Finding 1 — Enum files: ci-project had no modifications at baseline

All 21 expanded enum files present in archetype 2.6.0 are **byte-for-byte identical** in ci-project at 5.1.0. The ci-project never modified these files — they are purely archetype-provided.

Conclusion: any difference between archetype 2.6.0 and ci-project `hotfix/6.0` for these 21 files is **100% IOM 6 platform-driven** and belongs in the archetype.

### Finding 2 — 5 extra enum files are ci-project-specific, and unchanged in IOM 6

The 5 enum files present in ci-project but not in the archetype (`ExpandedDocumentTypeDefDO`, `ExpandedFileTransferConfigurationPropertyKeyDefDO`, `ExpandedPaymentEventDefDO`, `ExpandedRightDefDO`, `ExpandedRightGroupDefDO`) are **ci-project additions** — they existed at 5.1.0 and are **unchanged between 5.1.0 and hotfix/6.0**.

Conclusion: these 5 files must **not** be added to the archetype.

### Finding 3 — pom.xml: clear separation between ci-project additions and IOM 6 changes

**ci-project-specific pom additions** (present at 5.1.0, not in archetype 2.6.0 — must be ignored):
- `process-common`, `oms.utils`, `bakery.schemas-communication` (project-specific dependencies)
- `json-path`, `json-lib`, `javalite-common` (ci-project test dependencies)
- `jakarta.xml.ws-api`, `jaxws-rt` (ci-project web service dependencies)
- `jaxws-maven-plugin` (ci-project build plugin)
- `junit-jupiter-params` (ci-project test dependency — but generic enough to consider for archetype)

**Archetype 2.6.0 items not in ci-project 5.1.0** (ci-project removed/replaced these):
- `resteasy-core-spi`, `resteasy-client` (provided)
- `slf4j-api` (provided), `slf4j-simple` (test)
- `jackson-datatype-jsr310`
- `commons-lang3` (ci-project uses it but without explicit declaration — gets it transitively)

**IOM 6 changes to the pom** (in ci-project between 5.1.0 and hotfix/6.0, excluding ci-project-specific additions):
- `wildfly.version`: `30.0.1.Final` → `40.0.0.Final`
- `org.junit.version`: `5.9.2` → `6.1.0`
- `testframework.version`: `7.1.0` → `8.0.0`
- `compiler release`: `17` → `21`
- `postgresql`: `42.7.2` → `42.7.11`
- All plugin versions bumped (dependency-plugin, clean, surefire, failsafe, war, compiler, assembly, enforcer, site, exec, jacoco, antrun, release, deploy/install, resources)
- `commons-lang3` added with `scope: provided`
- `maven-clean-plugin` configured to preserve `target/` directory for devenv bind-mounts

### Finding 4 — javax.naming: still used in IOM 6

ci-project `hotfix/6.0` still uses `javax.naming.NamingException` in one file. WildFly 40 continues to support `javax.naming` (it's standard JDK, not Jakarta EE namespace). No migration needed.

### Finding 5 — ps/ Java source files: not in ci-project at all

All `com/intershop/oms/ps/` files are exclusively archetype-provided — ci-project never had them at any point. They cannot be cross-referenced against ci-project for IOM 6 changes. These files need to be reviewed independently against IOM 6 API changes.

---

## Required Changes

### 1. `pom.xml` (archetype-resources) — versions

| Item | Current archetype | Target |
|---|---|---|
| `platformVersion` default | `5.0.0` | `6.0.0` |
| `wildfly.version` | `30.0.1.Final` | `40.0.0.Final` |
| `org.junit.version` | `5.9.2` | `6.1.0` |
| `testframework.version` | `7.1.0` | `8.0.0` |
| compiler `release` | `17` | `21` |
| `postgresql` | current | `42.7.11` |
| All plugin versions | current | align with ci-project hotfix/6.0 |

### 2. `pom.xml` (archetype-resources) — dependencies

**Remove** (not present in ci-project 5.1.0 — ci-project dropped these, IOM 6 confirms they're gone):
- `resteasy-core-spi` (provided)
- `resteasy-client` (provided)
- `slf4j-api` (provided)
- `slf4j-simple` (test)
- `jackson-datatype-jsr310`

**Add:**
- `commons-lang3` with `scope: provided` (ci-project uses it provided; archetype 2.6.0 had it compile-scoped, which was wrong)

**Do NOT add** (ci-project-specific, not archetype concerns):
- `process-common`, `oms.utils`, `bakery.schemas-communication`
- `json-path`, `json-lib`, `javalite-common`
- `jakarta.xml.ws-api`, `jaxws-rt`, `jaxws-maven-plugin`

**Do not add** `junit-jupiter-params` — never present in any archetype release (1.0.0 through 2.6.0); it is a ci-project addition.

### 3. `pom.xml` (archetype-resources) — repositories

The archetype 2.6.0 had a single parameterized `iom-maven-artifacts` repository with snapshots **disabled**. ci-project 5.1.0 split this into `order-iom-releases` + `order-iom-snapshots` — but `order-iom-snapshots` is an internal Intershop feed not intended for customer projects. Its presence in ci-project is a ci-project-specific need (building against SNAPSHOT platform versions during development).

**For the archetype template:** replace `iom-maven-artifacts` with `order-iom-releases` only (snapshots disabled). Do **not** add `order-iom-snapshots`.

`mavenRepoURL` is a customer-facing parameter — each customer gets their own unique `order-iom-releases` feed URL from their Azure DevOps environment (documented in the README under "Maven Repository URL"). It must remain a `requiredProperty`. Only its **default value** needs updating from the old `iom-maven-artifacts` URL to the `order-iom-releases` URL.

Note: `mavenRepoURL` only ever covered *reading* IOM dependencies. The project's own artifact publishing is a separate concern, handled by `<distributionManagement>` which contains a `https://project-url` placeholder that the customer fills in with their own project-specific repository. That placeholder is correct and stays as-is.

### 4. `pom.xml` (archetype-resources) — `maven-clean-plugin`

Add configuration to preserve `target/` directory on `mvn clean` (from hotfix/6.0).

### 5. Expanded enum files (21 files)

Apply only **semantically meaningful** IOM 6 changes to each file — do not copy cosmetic noise from ci-project `hotfix/6.0`. Specifically:

**Apply:**
- New or changed imports that are actually required (e.g. a new type used in the file)
- New or removed fields, methods, or annotations that change the API or behaviour
- Changes to `@ExpandedEnum` annotation parameters if the referenced class changed
- Enum constant value/type changes driven by IOM 6 API changes (e.g. `String` → `EnumPayment`)

**Do NOT apply:**
- Import reordering (if the same imports are present, keep the existing order)
- Whitespace-only changes (spacing inside annotations, blank lines, indentation)
- Cosmetic reformatting of existing code that does not change meaning

**Absolute rule — archetype enum constants must only use negative IDs:**
An archetype must never define real values. Every enum constant in every `*DefDO.java` must have a negative integer ID (e.g. `-9999`). Negative IDs are recognised by IOM as non-persistent example values and are never written to the database. A positive ID would define a real, potentially conflicting value — that is strictly forbidden in an archetype template.

This means:
- Keep existing negative placeholder IDs from the IOM 5 archetype version
- Never copy positive IDs from ci-project (e.g. `1000`, `1001`, `10000`) — those are real ci-project values
- JNDI strings must remain example placeholders (e.g. `java:global/example-app/...`), not ci-project-specific values
- Enum constant names must remain generic examples (e.g. `EXAMPLE`, `TEST`), not ci-project-specific names

### 6. `ps/` Java source files — platform consolidation + IOM 6 API review

The IOM platform now ships an `iom/rest/` module (`com.intershop.oms.rest.*`) that contains evolved versions of several tools that originated in the archetype's `ps/` layer. The rule is: **use platform tools when available; adapt usage examples in remaining archetype files to reference the platform classes**.

#### Files to DELETE from archetype (replaced by platform)

| Delete | Platform replacement | Package |
|---|---|---|
| `rest/DefaultOptionsExceptionHandler.java` | `DefaultOptionsMethodExceptionMapper` | `com.intershop.oms.rest.exceptions` |
| `rest/filter/BasicAuthSecurityContext.java` | `BasicSecurityContext` | `com.intershop.oms.rest.authentication` |
| `rest/filter/IOMAuthFilter.java` | `AuthenticationFilter` | `com.intershop.oms.rest.provider` |
| `rest/filter/CORSFilter.java` | `CORSFilter` | `com.intershop.oms.rest.provider` |
| `rest/ExceptionHandler.java` | `ExceptionHandler` | `com.intershop.oms.rest.exceptions` |
| `rest/JacksonObjectMapperProvider.java` | `JacksonContextResolver` | `com.intershop.oms.rest.provider` |
| `rest/logging/MaskedHeaders.java` | `MaskedHeaders` | `com.intershop.oms.rest.logging` |
| `rest/logging/LoggingIOStreamHandler.java` | `LoggingIOStreamHandler` | `com.intershop.oms.rest.logging` |
| `rest/logging/sl4j/SLF4JContainerLoggingHandler.java` | `LoggingHandler` | `com.intershop.oms.rest.logging` |
| `rest/logging/sl4j/SLF4JWriterInterceptor.java` | `LoggingWriterInterceptor` | `com.intershop.oms.rest.logging` |

Note on `AuthenticationFilter`: the platform class is not `@Provider`-annotated — it must be explicitly registered in `RestServiceApplication` rather than picked up by auto-scan.

#### Files to KEEP in archetype (no platform equivalent)

- `rest/filter/BasicAuthenticationFilter.java` — client-side Basic Auth injection filter
- `rest/filter/BearerAuthenticationFilter.java` — client-side Bearer token injection filter
- `rest/filter/ClientCorrelationIdFilter.java` — client-side correlation ID propagation
- `rest/filter/ContainerCorrelationIdFilter.java` — container-side correlation ID propagation
- `rest/RestServiceApplication.java` — project's own JAX-RS `Application` class (every project needs its own)
- `rest/logging/DynamicLoggingFeature.java` — kept, but **update** references from `SLF4JContainerLoggingHandler` → `LoggingHandler`, `SLF4JWriterInterceptor` → `LoggingWriterInterceptor`
- `rest/logging/sl4j/SLF4JClientLoggingHandler.java` — client-side SLF4J logging (no platform equivalent)
- `rest/logging/database/DatabaseClientLoggingHandler.java` — client-side DB logging
- `rest/logging/database/DatabaseContainerLoggingHandler.java` — container-side DB logging; **update** reference from `SLF4JContainerLoggingHandler` → `LoggingHandler` if present
- `rest/logging/database/DatabaseWriterInterceptor.java` — DB body logging; **update** reference from `SLF4JWriterInterceptor` → `LoggingWriterInterceptor` if present
- `rest/annotations/Message.java` — `@Message` annotation for DB logging integration
- `servlet/Heartbeat.java`, `servlet/ImportErrors.java`, `servlet/ImportStatus.java`, `servlet/Testservlet.java`
- `services/configuration/*` — full custom configuration stack
- `util/ClientBuilder.java` — **update**: replace `SLF4JWriterInterceptor` → `LoggingWriterInterceptor` (platform; works client-side too); `SLF4JClientLoggingHandler` stays (no platform equivalent)
- `util/CustomizationUtilityStatic.java`
- `util/RestAuthenticationBean.java`
- `ordervalidation/ValidateMandatoryPropertiesPTBean.java`

#### IOM 6 API compatibility

IOM 6 was an infrastructure update (Java 21, WildFly 40) with one REST API removal (Order-State v1 — no overlap with archetype). All IOM API classes used in remaining `ps/` files exist unchanged in IOM 6.0.0.

WildFly 40 dropped Apache HttpClient 4.x — but since `LoggingIOStreamHandler` and `MaskedHeaders` are now replaced by the platform versions (which have no HttpClient dependency), this is resolved by deletion rather than modification.

### 7. `archetype-metadata.xml` + `archetype.properties`

**`archetype-metadata.xml`** (default values shown to users during project creation):

| Property | Current default | Target |
|---|---|---|
| `platformVersion` | `5.0.0` | `6.0.0` |
| `intershopDockerRepo` | `docker.tools.intershop.com/iom/intershophub/` | **no change** |
| `mavenRepoURL` | `iom-maven-artifacts` URL | **no change** |

**`archetype.properties`** (used by the Maven archetype integration test — must use Intershop-internal values):

| Property | Current value | Target |
|---|---|---|
| `platformVersion` | `5.0.0` | `6.0.0` |
| `intershopDockerRepo` | `docker.tools.intershop.com/iom/intershophub/` | **no change** — this is the correct registry for CI |
| `mavenRepoURL` | `iom-maven-artifacts` URL | **no change** — `iom-maven-artifacts` is the correct feed for CI |

The distinction: `archetype-metadata.xml` defaults are for customers generating new projects (they get `docker.io/library/` and their own `order-iom-releases` feed). `archetype.properties` drives the CI integration test inside Intershop's own pipeline, which uses the internal Docker registry and `iom-maven-artifacts` feed.

### 8. `azure-pipelines.yml`

| Setting | Current | Target |
|---|---|---|
| `pool` | static `ubuntu-20.4-DS2_v2-adopt-adoptium-jdk` | variable from `azure-devops-build-machines` group |
| `JDK_MAJOR_VERSION` | `17` | `21` |
| `IOM_HELM_VERSION` | `3.0.0` | `3.1.1` |
| `IOM_DOCKERHUB_*` variables | present | **no change** — keep |
| Dockerhub login step | present | **no change** — keep |
| Kubernetes auth | `Kubernetes@1` service connection | az login + kubelogin |
| Pull secret creation | `KubernetesManifest@0` | copy `iomdevops-pull-secret` from default namespace |
| `MavenAuthenticate` feeds | `iom-maven-artifacts` | **no change** — keep |
| Maven task version | `Maven@3` | **no change** — IOM uses Maven 3.9.x (see `mvnw`), keep `Maven@3` |

### 9. `helm-values-test.yaml`

`dbaccount.image.tag`: `2.0.0` → `2.1.0`

`kubectlImageRepository`: add override `registry.k8s.io/kubectl:v1.32.1` — the IOM Helm chart defaults to `docker.io/bitnami/kubectl:1.32.1` which is unavailable in the CI AKS environment. Use the official Kubernetes project image (`registry.k8s.io`) instead: publicly accessible without authentication, no Docker Hub rate limits, works for all customers.

### 10. `dependency-helper/pom.xml`

**Remove** `com.intershop.oms:order-state-app` — the Order State REST API v1 was removed in IOM 6, and with it the `order-state-app` module no longer exists in the platform. The ci-project still references it (against a 5.x SNAPSHOT), but it does not exist at version 6.0.0 in `iom-maven-artifacts`.

---

## General Rule: No SNAPSHOT versions in the archetype

The archetype must only reference released versions — for `platformVersion`, all dependency versions, plugin versions, and image tags. Any SNAPSHOT reference is a bug unless it is the generated project's own version (i.e. the `version` property defaulting to `1.0.0-SNAPSHOT`, which is correct — that's the customer project's starting version, not a dependency).

Current SNAPSHOT scan result: only `version=0.0.1-SNAPSHOT` (archetype.properties) and `<defaultValue>1.0.0-SNAPSHOT</defaultValue>` (archetype-metadata.xml) — both are the generated project version, both are correct.

---

## Open Questions

1. ~~**`junit-jupiter-params`**~~ — resolved: not in any archetype release, ci-project-specific, do not add.
2. ~~**`mavenRepoURL` property**~~ — resolved: keep as customer-facing parameter. Both `archetype-metadata.xml` default and `archetype.properties` value stay as `iom-maven-artifacts` — no change needed.
3. ~~**`ValidateMandatoryPropertiesPTBean.java`**~~ — resolved: all referenced classes exist in IOM 6.0.0, no changes needed beyond the Apache HttpClient removal.
4. ~~**`services/configuration/*`**~~ — resolved: no API changes, all classes confirmed present in IOM 6.0.0.
5. ~~**`dbaccount.image.tag`**~~ — resolved: `2.1.0`

---

## Suggested Order of Implementation

1. `pom.xml` — versions, dependencies, repositories, clean plugin
2. `archetype-metadata.xml` + `archetype.properties` defaults
3. `dependency-helper/pom.xml` — remove `order-state-app`
4. Enum files (21 replacements with archetype placeholder values)
5. `ps/` Java sources — API compatibility fixes
6. `azure-pipelines.yml`
6. `helm-values-test.yaml`
7. Local build verification via `/dev-local`
