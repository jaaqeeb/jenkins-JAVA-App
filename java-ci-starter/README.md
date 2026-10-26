# CI Demo Java Application

A small Spring Boot app for learning Git, Maven, Jenkins CI, SonarQube, dependency vulnerability scanning, and private artifact publishing. EKS is intentionally out of scope for this first exercise.

## What the app does

- `GET /actuator/health` — health endpoint for smoke checks.
- `GET /api/features/greeting` — reports whether the greeting feature is enabled.
- `GET /api/greet?name=Ada` — returns a greeting only when the feature flag is enabled.
- Feature flag: `FEATURE_GREETING_ENABLED`, defaults to `false`.

The flag is controlled by environment configuration, not by an unauthenticated endpoint. This is safer than exposing a public API that can change application features at runtime.

## Requirements

- JDK 17
- Maven 3.9+ (or add the Maven Wrapper before standardizing the team workflow)
- Git
- Optional for CI: Jenkins and SonarQube
- Optional for private artifact publishing: Nexus Repository

## Run locally

```bash
mvn clean verify
mvn spring-boot:run
```

In another terminal:

```bash
curl -i http://localhost:8080/actuator/health
curl http://localhost:8080/api/features/greeting
curl -i 'http://localhost:8080/api/greet?name=Ada'
```

The greeting endpoint returns HTTP 501 while the feature is disabled. Start the app with the feature enabled:

```bash
FEATURE_GREETING_ENABLED=true mvn spring-boot:run
```

Then:

```bash
curl 'http://localhost:8080/api/greet?name=Ada'
```

Expected response:

```json
{"message":"Hello, Ada!"}
```

## Local staging smoke test

On Linux/macOS, after packaging:

```bash
chmod +x scripts/stage-local.sh
./scripts/stage-local.sh
```

This runs tests, starts the packaged JAR with the feature disabled, checks health, and verifies the disabled feature behavior. It does not provision a real staging server.

## Git repository setup

Create an empty private repository on your Git hosting service, then run from this folder:

```bash
git init
git add .
git commit -m "Add Java CI demo application"
git branch -M main
git remote add origin YOUR_PRIVATE_GIT_REPOSITORY_URL
git push -u origin main
```

Replace `YOUR_PRIVATE_GIT_REPOSITORY_URL` with your repository URL. Never commit credentials, tokens, private keys, or production configuration.

## Jenkins setup

1. Install/configure JDK 17 and Maven in Jenkins with names `JDK17` and `Maven3`, or change the names in `Jenkinsfile`.
2. Configure a SonarQube server in Jenkins with installation name `SonarQube`.
3. Install/configure the SonarQube Scanner for Jenkins integration and configure the webhook:
   `https://YOUR-JENKINS/sonarqube-webhook/`
4. Create a SonarQube project and supply its project key, for example:
   `-Dsonar.projectKey=ci-demo-app`
   Add that property to the Sonar Maven command in `Jenkinsfile`, along with any required organization or server settings for your SonarQube edition.
5. Create a Pipeline job from SCM and point it at this repository.
6. Configure the SonarQube quality gate. The Jenkins `waitForQualityGate` step requires the webhook and the SonarQube Jenkins integration.
7. Run the pipeline and inspect test results and archived Dependency-Check reports.

### Dependency scan notes

The first OWASP Dependency-Check run may take longer because vulnerability data must be downloaded. In a restricted or production environment, configure an approved data-update strategy and a current plugin version. Tune the fail threshold to the organization's vulnerability policy; this example uses CVSS 7.0.

### SonarQube notes

SonarQube editions and versions differ in their security-analysis features. Treat SonarQube as one part of security testing, not a replacement for dependency scanning, secret scanning, or runtime testing.

## Publish to private Nexus later

Do not enable the Nexus stage until a private Maven hosted repository exists.

1. Create hosted repositories such as `maven-releases` and `maven-snapshots` in Nexus.
2. Add `distributionManagement` URLs to `pom.xml` (uncomment and replace `YOUR-NEXUS`).
3. In the Jenkins-managed Maven `settings.xml`, add a `<server>` whose `<id>` exactly matches `nexus-releases` or `nexus-snapshots`. Store the username/password in Jenkins credentials, not in Git.
4. Uncomment the `Publish to Nexus` stage in `Jenkinsfile`. Restrict publishing to protected branches/tags and use a release versioning policy.

Important: the sample version is `0.1.0-SNAPSHOT`; Maven sends this version to the snapshot repository. For a release repository, use a non-SNAPSHOT version and an approved release process.

## Later roadmap

1. Complete local build and tests.
2. Push to private Git repository.
3. Run Jenkins CI and make SonarQube/dependency scans blocking quality gates.
4. Publish the tested artifact to Nexus.
5. Add a Dockerfile and container scan.
6. Push images to Amazon ECR.
7. Create EKS and deploy through a controlled CD pipeline.

## Security reminders

- Use Jenkins credentials and IAM roles rather than hard-coded secrets.
- Do not expose the actuator environment/configuration endpoints.
- Keep dependencies and scanning plugins updated.
- A green pipeline only means the configured checks passed; it is not a guarantee that the application is vulnerability-free.
