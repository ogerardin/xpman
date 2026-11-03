#!/bin/bash
# Run SonarQube analysis for XPman
# Prerequisites:
# - SonarQube container running: docker start sonarqube-xpman
# - sonar-scanner installed: brew install sonar-scanner
# - Token generated in SonarQube UI (default admin/admin credentials)

set -e

# Check if sonar-scanner is installed
if ! command -v sonar-scanner &> /dev/null; then
    echo "Error: sonar-scanner is not installed"
    echo "Install with: brew install sonar-scanner"
    exit 1
fi

# Check if Docker container is running
if ! docker ps | grep -q sonarqube-xpman; then
    echo "Error: SonarQube container 'sonarqube-xpman' is not running"
    echo "Start it with: docker start sonarqube-xpman"
    exit 1
fi

# Wait for SonarQube to be ready
echo "Waiting for SonarQube to start..."
for i in {1..30}; do
    if curl -s "http://localhost:9000" | grep -q 'data-server-status="UP"'; then
        echo "SonarQube is ready"
        break
    fi
    if [ $i -eq 30 ]; then
        echo "Error: SonarQube failed to start within 90 seconds"
        exit 1
    fi
    sleep 3
done

# Build the project first
echo "Building project..."
mvn -B -DskipTests clean package

# Run sonar-scanner
# Token must be provided as environment variable or argument
if [ -z "$SONAR_TOKEN" ]; then
    echo "Error: SONAR_TOKEN not set"
    echo "Generate a token in SonarQube UI: http://localhost:9000/account/security"
    echo "Then run: SONAR_TOKEN=<token> ./run-sonar.sh"
    exit 1
fi

echo "Running SonarQube analysis..."
sonar-scanner -Dsonar.host.url=http://localhost:9000 -Dsonar.token=$SONAR_TOKEN

echo ""
echo "Analysis complete!"
echo "View results at: http://localhost:9000/dashboard?id=com.ogerardin.xplane%3Axpman"
