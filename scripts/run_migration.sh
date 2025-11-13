#!/usr/bin/env sh
set -e

# run-cmd: ./scripts/run_migration.sh application-dev
# =====================================
# Resolve project root path
# =====================================
PROFILE=${1:-application}
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
PROPS_FILE="$PROJECT_ROOT/src/main/resources/${PROFILE}.properties"

if [ ! -f "$PROPS_FILE" ]; then
  echo "❌ application.properties not found at $PROPS_FILE"
  exit 1
fi

# =====================================
# Read values from application.properties
# =====================================
DB_URL=$(grep "^spring.datasource.url" "$PROPS_FILE" | cut -d'=' -f2 | tr -d '\r')
DB_USER=$(grep "^spring.datasource.username" "$PROPS_FILE" | cut -d'=' -f2 | tr -d '\r')
DB_PASSWORD=$(grep "^spring.datasource.password" "$PROPS_FILE" | cut -d'=' -f2 | tr -d '\r')
FLYWAY_LOCATIONS="$PROJECT_ROOT/src/main/resources/db/migration"

# =====================================
# Validate required values
# =====================================
if [ -z "$DB_URL" ] || [ -z "$DB_USER" ] || [ -z "$DB_PASSWORD" ]; then
  echo "❌ Missing datasource configuration in application.properties"
  exit 1
fi

# =====================================
# Run Flyway CLI
# =====================================
echo "🚀 Running Flyway migrations with CLI..."
echo "➡️ URL: $DB_URL"
echo "➡️ USER: $DB_USER"
echo "➡️ LOCATIONS: $FLYWAY_LOCATIONS"
echo "➡️ BASELINE ON MIGRATE: true"

flyway -url="$DB_URL" \
       -user="$DB_USER" \
       -password="$DB_PASSWORD" \
       -locations="filesystem:$FLYWAY_LOCATIONS" \
       -baselineOnMigrate=true \
       migrate

echo "✅ Flyway migrations applied successfully!"
