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

All 21 are IOM 6 platform changes — replace each verbatim from ci-project `hotfix/6.0`. The only adjustment needed is the package declaration, which stays `com.intershop.oms.enums.expand` in the archetype (ci-project uses the same package, no change needed).

The nature of the IOM 6 changes in these files: import reordering, `@ExpandedEnum( Class )` spacing, updated example values. The ci-project also has project-specific JNDI strings and positive enum IDs — these must be replaced with the archetype placeholder pattern (`-999` IDs, empty/example JNDI strings) as in the IOM 5 version of each file.

### 6. `ps/` Java source files — IOM 6 API review

IOM 6 was an infrastructure update (Java 21, WildFly 40) with one REST API removal: the Order-State v1 API (`OrderSearchPO`, `CustomerOrderDetailsPO`, `OrderAddressPO`, `OrderPosEntryPO`, etc.). None of the archetype `ps/` files reference the removed v1 API.

All other classes used in `ps/` files were confirmed present in IOM 6.0.0 jars:
`MissingFieldException`, `ErrorTextFormatter`, `PaymentProviderLogicService`, `ShopLogicService`, `MessageLogContext`, `MessageLogManager`, `OrderMessageLogDO` — all exist unchanged.

The only source-level changes needed are due to WildFly 40 dropping Apache HttpClient 4.x:

- `rest/logging/LoggingIOStreamHandler.java` — remove the `readEntity(HttpResponse)` overload and its `org.apache.http.*` imports
- `rest/logging/MaskedHeaders.java` — remove the `of(Header[])` overload and its `org.apache.http.Header` import

All other `ps/` files (`javax.naming`, `services/configuration/*`, `util/ClientBuilder.java`) compile cleanly against IOM 6 with no changes needed.

### 7. `archetype-metadata.xml` + `archetype.properties`

| Property | Current default | Target |
|---|---|---|
| `platformVersion` | `5.0.0` | `6.0.0` |
| `intershopDockerRepo` | `docker.tools.intershop.com/iom/intershophub/` | `docker.io/library/` |

### 8. `azure-pipelines.yml`

| Setting | Current | Target |
|---|---|---|
| `pool` | static `ubuntu-20.4-DS2_v2-adopt-adoptium-jdk` | variable from `azure-devops-build-machines` group |
| `JDK_MAJOR_VERSION` | `17` | `21` |
| `IOM_HELM_VERSION` | `3.0.0` | `3.1.1` |
| `IOM_DOCKERHUB_*` variables | present | remove |
| Dockerhub login step | present | remove |
| Kubernetes auth | `Kubernetes@1` service connection | az login + kubelogin |
| Pull secret creation | `KubernetesManifest@0` | copy `iomdevops-pull-secret` from default namespace |
| `MavenAuthenticate` feeds | `iom-maven-artifacts` | `order-iom-releases,order-iom-snapshots` |
| Maven task version | `Maven@3` | `Maven@4` |

### 9. `helm-values-test.yaml`

`dbaccount.image.tag`: `2.0.0` → `2.1.0`

---

## General Rule: No SNAPSHOT versions in the archetype

The archetype must only reference released versions — for `platformVersion`, all dependency versions, plugin versions, and image tags. Any SNAPSHOT reference is a bug unless it is the generated project's own version (i.e. the `version` property defaulting to `1.0.0-SNAPSHOT`, which is correct — that's the customer project's starting version, not a dependency).

Current SNAPSHOT scan result: only `version=0.0.1-SNAPSHOT` (archetype.properties) and `<defaultValue>1.0.0-SNAPSHOT</defaultValue>` (archetype-metadata.xml) — both are the generated project version, both are correct.

---

## Open Questions

1. ~~**`junit-jupiter-params`**~~ — resolved: not in any archetype release, ci-project-specific, do not add.
2. ~~**`mavenRepoURL` property**~~ — resolved: keep as customer-facing parameter, update default value from `iom-maven-artifacts` URL to `order-iom-releases` URL.
3. ~~**`ValidateMandatoryPropertiesPTBean.java`**~~ — resolved: all referenced classes exist in IOM 6.0.0, no changes needed beyond the Apache HttpClient removal.
4. ~~**`services/configuration/*`**~~ — resolved: no API changes, all classes confirmed present in IOM 6.0.0.
5. ~~**`dbaccount.image.tag`**~~ — resolved: `2.1.0`

---

## Suggested Order of Implementation

1. `pom.xml` — versions, dependencies, repositories, clean plugin
2. `archetype-metadata.xml` + `archetype.properties` defaults
3. Enum files (21 replacements with archetype placeholder values)
4. `ps/` Java sources — API compatibility fixes
5. `azure-pipelines.yml`
6. `helm-values-test.yaml`
7. Local build verification via `/dev-local`
