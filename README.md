# TierApp

## Git convention

This repository follow the Git Flow. 

The branch name must follow this pattern : 
 - for feature : `feature/us-[S].[U]-[name]` where `S` = Scope ID, `U` = User Story ID, `name` = User story name (for space, use and underscore `_`)
 - for release : `release/v.[M].[m]-sprint-[N]` where `M` is the major, `m` is minor, `N` sprint number
 - for fix     : `fix/us-[S].[U]-[name]` where `S` = Scope ID, `U` = User Story ID, `name` = User story name (for space, use and underscore `_`)

## Application architecture

<img src="./documentation/img/android-mvvm.png" alt="Complete architecture scheme" width="600" /> 

### Modules

There is 1 production modules : 
 
 - `:app` : Contains the entry point of the application, UI and navigation

For now, there is one testing module :

 - `:testing:shared` : Shared code used for testing (Unit Tests and Instrumented Test). All production modules reference it for testing

### Flavors

`:app` contains 4 flavors registered in dimension `environment`:

 - `development` : target the STAR API on development environment
 - `staging` : target the STAR API on staging environment
 - `production` : target the STAR API on production environment

For CLI build, it doesn't matter. In fact, `gradlew assembleProductionRelease` will use the following module configuration : 

 - `:app` : Production - Release

### Keystore

All information about Keystore should be provided via a secured mean

For the CI/CD, the variable is Base64 encoded embedded as environment variable

### Version

Versions are available in the file [`project-versions.gradle`](./project-versions.gradle).

### Tools

The directory [.tools](./.tools) contains Kotlin KTS file : 

 - [`junit-report-converter.gradle.kts`](./.tools/junit-report-converter.gradle.kts) : it creates a Gradle task to convert lint report to JUnit Report

## CI/CD 

CI/CD system is GitLab CI. All templates are located at [`.gitlab/ci/.gitlab-ci-template.yml`](./.gitlab/ci/.gitlab-ci-template.yml)

Application version code is provided by the pipeline individual ID (restricted to the project).

#Tools

- Testing : Used MockK and Junit for Unit testing (Not enough time for instrumented and Ui tests)
- DI : Used Dagger Hilt for dependency injection
- View Binding
- Used google maps sdk for android
- Leak canary for Memory leaks and performace report
- Used Many other libraries : RxPreferences , LiveData , Flow...