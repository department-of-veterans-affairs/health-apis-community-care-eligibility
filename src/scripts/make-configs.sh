#! /usr/bin/env bash

usage() {
cat <<EOF

$0 [options]

Generate configurations for local development.

Options
     --debug               Enable debugging
 -h, --help                Print this help and exit.
     --secrets-conf <file> The configuration file with secrets!

Secrets Configuration
 This bash file is sourced and expected to set the following variables
 - EE_ENDPOINT_URL
 - EE_HEADER_PASSWORD
 - EE_HEADER_USERNAME
 - EE_TRUSTSTORE_PASSWORD
 - KEYSTORE_PASSWORD
 - VA_FACILITIES_API_KEY
 - VA_FACILITIES_URL

$1
EOF
exit 1
}

REPO=$(cd $(dirname $0)/../.. && pwd)
SECRETS="$REPO/secrets.conf"
PROFILE=dev
MARKER=$(date +%s)
ARGS=$(getopt -n $(basename ${0}) \
    -l "debug,help,secrets-conf:" \
    -o "h" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    --debug) set -x;;
    -h|--help) usage "halp! what this do?";;
    --secrets-conf) SECRETS="$2";;
    --) shift;break;;
  esac
  shift;
done

echo "Loading secrets: $SECRETS"
[ ! -f "$SECRETS" ] && usage "File not found: $SECRETS"
. $SECRETS

MISSING_SECRETS=false
[ -z "$EE_ENDPOINT_URL" ] && echo "Missing configuration: EE_ENDPOINT_URL" && MISSING_SECRETS=true
[ -z "$EE_HEADER_PASSWORD" ] && echo "Missing configuration: EE_HEADER_PASSWORD" && MISSING_SECRETS=true
[ -z "$EE_HEADER_USERNAME" ] && echo "Missing configuration: EE_HEADER_USERNAME" && MISSING_SECRETS=true
[ -z "$EE_TRUSTSTORE_PASSWORD" ] && echo "Missing configuration: EE_TRUSTSTORE_PASSWORD" && MISSING_SECRETS=true
[ -z "$KEYSTORE_PASSWORD" ] && echo "Missing configuration: KEYSTORE_PASSWORD" && MISSING_SECRETS=true
[ -z "$VA_FACILITIES_API_KEY" ] && echo "Missing configuration: VA_FACILITIES_API_KEY" && MISSING_SECRETS=true
[ -z "$VA_FACILITIES_URL" ] && echo "Missing configuration: VA_FACILITIES_URL" && MISSING_SECRETS=true
[ $MISSING_SECRETS == true ] && usage "Missing configuration secrets, please update $SECRETS"

makeConfig() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  [ -f "$target" ] && mv -v $target $target.$MARKER
  grep -E '(.*= *unset)' "$REPO/$project/src/main/resources/application.properties" \
    > "$target"
}

configValue() {
  local project="$1"
  local profile="$2"
  local key="$3"
  local value="$4"
  local target="$REPO/$project/config/application-${profile}.properties"
  local escapedValue=$(echo $value | sed -e 's/\\/\\\\/g; s/\//\\\//g; s/&/\\\&/g')
  sed -i "s/^$key=.*/$key=$escapedValue/" $target
}

checkForUnsetValues() {
  local project="$1"
  local profile="$2"
  local target="$REPO/$project/config/application-${profile}.properties"
  echo "checking $target"
  grep -E '(.*= *unset)' "$target"
  [ $? == 0 ] && echo "Failed to populate all unset values" && exit 1
  diff -q $target $target.$MARKER
  [ $? == 0 ] && rm -v $target.$MARKER
}

whoDis() {
  local me=$(git config --global --get user.name)
  [ -z "$me" ] && me=$USER
  echo $me
}

sendMoarSpams() {
  local spam=$(git config --global --get user.email)
  [ -z "$spam" ] && spam=$USER@aol.com
  echo $spam
}

makeConfig community-care-eligibility $PROFILE
configValue community-care-eligibility $PROFILE ee.endpoint.url "$EE_ENDPOINT_URL"
configValue community-care-eligibility $PROFILE ee.header.password "$EE_HEADER_PASSWORD"
configValue community-care-eligibility $PROFILE ee.header.username "$EE_HEADER_USERNAME"
configValue community-care-eligibility $PROFILE ee.truststore.password "$EE_TRUSTSTORE_PASSWORD"
configValue community-care-eligibility $PROFILE ee.truststore.path "eligibilityandenrollment-nonprod-truststore.jks"
configValue community-care-eligibility $PROFILE va-facilities.api-key "$VA_FACILITIES_API_KEY"
configValue community-care-eligibility $PROFILE va-facilities.url "$VA_FACILITIES_URL"

checkForUnsetValues community-care-eligibility $PROFILE
