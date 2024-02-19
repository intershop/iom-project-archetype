[![Maven Central](https://img.shields.io/maven-central/v/com.intershop.oms.archetype/iom-project-archetype)](https://mvnrepository.com/artifact/com.intershop.oms.archetype/iom-project-archetype)
# Introduction 
In order to enable implementation partners and customers to quickly ramp up new IOM customization projects, Intershop provides a so called "maven archetype" that allows them to create a standardized project/directory structure according to our best practices, within a matter of minutes. The objective of this tool is to create projects that fulfill these typical requirements:

1. Compatibility with standardized CI/CD pipelines in Azure DevOps
2. Compatibility with / preconfigured for devenv-4-iom - the local development environment toolkit for IOM
3. Including a selection of reasonably configured property files for IOM - e.g. to add custom java packages to the logging framework in wildfly
4. Including some prepared Java classes that have to be used to extend IOM functionality according to the cookbooks

# Usage: Create an empty project from the archetype

## Get Information from *Azure DevOps Environment* of *Intershop Commerce Platform*

### Git Repository

The *Azure DevOps Environment* already provides a Git repository to host your IOM project. It is named *\<project-name\>-iom*. You have to use this Git repository, when setting up the new IOM project.

### Maven Repository URL

The URL of the Maven repository, providing the IOM build artifacts, is unique for your project. You have to pass this URL for property *mavenRepoURL* when creating the project from archetype.

1. Login into Azure DevOps Environment
2. Open Artifacts in left menu
3. Select feed *iom-maven-artifacts*
4. Click *Connect to Feed*
5. Select *Maven*
6. Copy the URL from *pom.xml*, shown on this page, to pass it as value for property *mavenRepoURL*.

## Generate maven artifact

In order to create a new project structure from the archetype you have to define a set of variables that will be used during the creation.

|Name|Example|Description
|---|---|---|
|platformVersion|4.1.0|Initial IOM version for the project. Should be the latest release.|
|intershopDockerRepo|docker.tools.intershop.com/iom/intershophub/|Docker registry for the standard IOM images - e.g. a proxy repo / mirror of dockerhub. This parameter needs a trailing slash.|
|mavenRepoURL|https://pkgs.dev.azure.com/intershop-com/Products/_packaging/iom-maven-artifacts/maven/v1|URL of Maven Repository *iom-maven-artifacts* as provided by *Azure DevOps Environment* |
|version|1.0.0-SNAPSHOT|Initial version of the project package|
|groupId|com.intershop.oms.blueprint|Maven groupId for the created project package.|
|artifactId|blueprint-project|Maven artifactId for the created project package.|
|package|com.intershop.oms.blueprint|"Top level" java package that should include all project sources. It is used to setup the initial logger configuration for the CUSTOMIZATION logger. <br/>Defaults to groupId parameter.|
|projectName|blueprint-project|Used for some placeholders like the image name.<br/>Defaults to artifactId parameter.|

To start archetype generation execute the following command in an interactive terminal - please do this in an empty directory, there is no need to clone this repository:

```bash
mvn org.apache.maven.plugins:maven-archetype-plugin:3.2.1:generate -DaskForDefaultPropertyValues -DarchetypeArtifactId=iom-project-archetype -DarchetypeGroupId=com.intershop.oms.archetype -DarchetypeVersion=LATEST
```

After entering the requested parameters you'll find a new directory named like the "artifactId" in your current working directory. To generate a project in non-interactive mode please have a look at the according [documentation](https://maven.apache.org/archetype/maven-archetype-plugin/examples/generate-batch.html).

## Push project to a git repository
Please create a new git repository for the project and push the **content** of the new directory to the remote repository. When using git solutions such as GitHub, GitLab, Azure DevOps etc. they provide instructions on how to do that after creating the repository itself.

Example:
```bash
cd my-new-project
git init
git add .
git commit -m "Initial commit"
git remote add origin git@github.com:username/my-new-project
git push -u origin HEAD
```

## Integrate devenv-4-iom
_Note: you can find the full documentation for devenv-4-iom in the according [git repository.][devenv git repo]_

The general recommendation is to integrate devenv-4-iom as a [git subtree](https://www.atlassian.com/git/tutorials/git-subtree). In the most simple form this can be achieved by running the following commands in your project directory:
```bash
git subtree add --prefix devenv-4-iom git@github.com:intershop/iom-devenv.git main --squash
git push
```

To update devenv-4-iom at a later point run the following commands in the same directory:
```bash
git subtree pull --prefix devenv-4-iom git@github.com:intershop/iom-devenv.git main --squash
git push
```

__TODO add link__ After adding devenv-4-iom to your project structure, please follow the quick start guide.

# Usage: Typical developer tasks
**Note:** Basic knowledge about maven build lifecycle is expected at this point.

|Task|Instructions|
|---|---|
|Update project to a new IOM version|Open pom.xml files and update the "platform.version" property to the target release. Furthermore follow the migration steps in the release notes.<br/>Additionally it is a good idea to update 3rd party dependencies and build plugins at this point.|
|Build / deploy images locally|<ul><li>run "mvn package -Pdocker"</li><li>set the according image version/tag in devenv-4-iom properties and redeploy them</li></ul>|

For further information please refer to the [devenv-4-iom documentation][devenv git repo].

[devenv git repo]: https://github.com/intershop/iom-devenv

# Version compatibility
iom-project-archetype is only used once in a project lifecycle - hence there are no plans to support any IOM version other than the latest release. The following table will show the compatibility of iom-project-archetype to the according IOM minor versions. If the latest IOM version is not listed yet it is (1) not supported by iom-project-archetype or (2) still supported by the previous release - this might be the case if there were no "breaking" changes like a wildfly version upgrade in the latest release.
|IOM Version|iom-project-archetype Version|
|-----------|-----------------------------|
|3.6        |1.0.0                        |
|3.7        |1.0.1                        |
|4.0        |2.0.0                        |
|4.1        |2.1.0                        |
|4.2        |2.2.0                        |
|4.4        |2.3.0                        |
|4.5-4.6    |2.4.0                        |
|4.7        |2.5.0                        |
|5.0        |2.6.0                        |