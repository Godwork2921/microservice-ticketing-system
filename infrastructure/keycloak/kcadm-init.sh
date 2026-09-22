#!/usr/bin/env bash
# Creates application users in the "ticketing" realm (imported from realm-export.json)
# using the Keycloak Admin CLI. Run as a docker-compose init service, or manually:
#   docker run --network ticketing -e KC_HOST=http://keycloak:8080 \
#     -v "$PWD:/opt/keycloak/data/init" \
#     quay.io/keycloak/keycloak:26.0.7 /bin/bash /opt/keycloak/data/init/kcadm-init.sh
set -euo pipefail

KC_HOST="${KC_HOST:-http://localhost:8180}"
REALM="${KC_REALM:-ticketing}"
ADMIN_USER="${KEYCLOAK_ADMIN:-admin}"
ADMIN_PASSWORD="${KEYCLOAK_ADMIN_PASSWORD:-change-me}"
USER_PASSWORD="${KEYCLOAK_USER_PASSWORD:-Change123!}"
KCADM="/opt/keycloak/bin/kcadm.sh"

echo "Waiting for Keycloak at ${KC_HOST} ..."
until "$KCADM" get realms/master --server "$KC_HOST" --realm master \
    --user "$ADMIN_USER" --password "$ADMIN_PASSWORD" >/dev/null 2>&1; do
  sleep 5
done
echo "Keycloak is available."

"$KCADM" config credentials --server "$KC_HOST" \
    --realm master --user "$ADMIN_USER" --password "$ADMIN_PASSWORD"

ensure_user() {
  local username="$1" email="$2" password="$3" roles="$4"
  if "$KCADM" create users -r "$REALM" \
      -s "username=$username" \
      -s "email=$email" \
      -s "emailVerified=true" \
      -s "enabled=true" \
      -s "credentials=[{\"type\":\"password\",\"value\":\"$password\",\"temporary\":false}]" \
      >/dev/null 2>&1; then
    echo "Created user '$username'."
  else
    echo "User '$username' already exists, updating roles only."
  fi
  for role in $roles; do
    if "$KCADM" add-roles -r "$REALM" --uusername "$username" --rolename "$role" >/dev/null 2>&1; then
      echo "  assigned realm role '$role' to '$username'."
    else
      echo "  could not assign realm role '$role' (already assigned or missing)."
    fi
  done
}

ensure_user "admin@yeab.com"   "admin@yeab.com"   "$USER_PASSWORD" "ADMIN"
ensure_user "operator@yeab.com" "operator@yeab.com" "$USER_PASSWORD" "OPERATOR"
ensure_user "staff@yeab.com"   "staff@yeab.com"   "$USER_PASSWORD" "STAFF"
ensure_user "customer@yeab.com" "customer@yeab.com" "$USER_PASSWORD" "CUSTOMER"

echo "Keycloak initialization complete."