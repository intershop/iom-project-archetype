---
name: dev-local
description: Local archetype development workflow for iom-project-archetype on Apple M1. Use this skill whenever you need to install the archetype locally, generate a test project, or verify archetype changes work end-to-end. Covers: reading the required IOM version from archetype metadata, checking that the IOM Docker image is present, tagging it with the correct intershopDockerRepo prefix, running `mvn clean install` on the archetype, and generating a new project with the ci-project coordinates.
---

# Local Archetype Development Workflow

Use this skill to test archetype changes locally on an Apple M1 Mac before committing.

## Overview

The full cycle:
1. Read `platformVersion` and `intershopDockerRepo` from archetype metadata
2. Verify the IOM Docker image exists locally; if not, ask the user to build it in `../order-iom`
3. Tag the local IOM image with the `intershopDockerRepo` prefix so Maven can find it
4. `mvn clean install` the archetype
5. Generate a new project using the ci-project coordinates

---

## Step 1 — Read required values from archetype metadata

```bash
grep -E 'defaultValue' src/main/resources/META-INF/maven/archetype-metadata.xml
```

Extract:
- `platformVersion` — the IOM version the archetype targets (e.g., `5.0.0`)
- `intershopDockerRepo` — the Docker repo prefix for the IOM base image (e.g., `docker.tools.intershop.com/iom/intershophub/`)

Also check `src/test/resources/projects/basic/archetype.properties` — the values there override the metadata defaults during the Maven verify phase and are used as the test project coordinates.

---

## Step 2 — Verify the IOM Docker image is available

The `project2docker` Maven plugin pulls the IOM base image from `intershopDockerRepo` + `iom:` + `platformVersion` during the build.
On Apple M1 the image cannot be pulled from the remote registry; it must be built locally in `../order-iom`.

Check whether the image exists:

```bash
docker image inspect iom:<platformVersion>
```

If the image is **missing or stale**: stop and ask the user to build it using the `iom-build-images` skill in `../order-iom` (Steps 1 and 2 of that skill — WildFly base image if needed, then IOM app image). Do not proceed until the image is available.

---

## Step 3 — Tag the local IOM image with the intershopDockerRepo prefix

The `project2docker` plugin references the image as `<intershopDockerRepo>iom:<platformVersion>`.
Tag the local `iom:<platformVersion>` image accordingly so Docker resolves it without a network pull:

```bash
PLATFORM_VERSION=<platformVersion>
DOCKER_REPO=<intershopDockerRepo>   # e.g. docker.tools.intershop.com/iom/intershophub/

docker tag "iom:${PLATFORM_VERSION}" "${DOCKER_REPO}iom:${PLATFORM_VERSION}"
```

---

## Step 4 — Install the archetype locally

Run from the archetype root:

```bash
mvn clean install
```

This builds the archetype jar, runs the integration test (generates a project from the archetype and verifies it), and installs the artifact into the local Maven repository.

---

## Step 5 — Generate a test project

Generate a new project using the ci-project coordinates. Run this from a scratch directory **outside** the archetype repo (e.g., `/tmp/iom-archetype-test/`):

```bash
mkdir -p /tmp/iom-archetype-test && cd /tmp/iom-archetype-test

mvn org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate \
  -DinteractiveMode=false \
  -DarchetypeGroupId=com.intershop.oms \
  -DarchetypeArtifactId=iom-project-archetype \
  -DarchetypeVersion=<archetype-version-from-pom.xml> \
  -DgroupId=com.intershop.oms.ciproject \
  -DartifactId=ci-project-app \
  -Dversion=1.0.0-SNAPSHOT \
  -DprojectName=ci-project-app \
  -DplatformVersion=<platformVersion> \
  -DintershopDockerRepo=<intershopDockerRepo>
```

The `archetypeVersion` must match what was just installed — read it from `pom.xml` in the archetype root.

After generation, inspect the output under `/tmp/iom-archetype-test/ci-project-app/` and compare it against `../order-iom/ci-project/` to verify the generated project structure is correct.
