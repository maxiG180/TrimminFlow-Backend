# SonarCloud Configuration

## Current Issue
SonarCloud requires 80% code coverage but the project currently has ~13% coverage.

## Solutions

### Option 1: Adjust Quality Gate in SonarCloud (Recommended for now)
1. Go to [SonarCloud](https://sonarcloud.io)
2. Navigate to your project: `maxiG180_TrimminFlow-Backend`
3. Go to **Project Settings** → **Quality Gates**
4. Either:
   - Select a different quality gate (e.g., "Sonar way - Relaxed")
   - OR create a custom quality gate with lower coverage requirements (e.g., 60%)

### Option 2: Increase Test Coverage (Long-term solution)
To reach 80% coverage, you would need to:
- Add more unit tests for untested services
- Enable and fix the integration tests in `/src/test/java/com/trimminflow/demo/integration/`
- Add tests for controllers, DTOs, and utility classes

## Current Test Coverage
- **Unit Tests**: ~16 tests covering services (BarberManagementService, ServiceManagementService, AppointmentService, AuthService, JwtUtil)
- **Integration Tests**: Available but currently excluded (need H2 database setup)

## Running SonarQube Locally
```bash
./gradlew clean build sonar
```

## Note
The gradle build is configured to run tests and generate coverage reports before sonar analysis.
