#!/usr/bin/env bash

ENDPOINT_DOMAIN_NAME="$K8S_LOAD_BALANCER"
ENVIRONMENT="$K8S_ENVIRONMENT"
TOKEN="$TOKEN"
BASE_PATH="$BASE_PATH"
PATIENT="$PATIENT"

#Current versions available
VERSION="/v0/eligibility"

#Put Health endpoints here if you got them
PATHS=(/actuator/health \
$VERSION/openapi.json \
$VERSION/openapi.yaml)

SUCCESS=0

FAILURE=0

# New phone who this?
usage() {
cat <<EOF
Commands
  smoke-test [--endpoint-domain-name|-d <endpoint>] [--environment|-e <env>]
  regression-test [--endpoint-domain-name|-d <endpoint>] [--environment|-e <env>]

Example
  smoke-test
    --endpoint-domain-name=localhost
    --environment=qa
    --base-path=/community-care
    --patient=12345678V90

$1
EOF
exit 1
}

doCurl () {
  if [[ -n "$2" ]]
  then
    REQUEST_URL="$ENDPOINT_DOMAIN_NAME$BASE_PATH$VERSION${path// /%20}"
    status_code=$(curl -k -H "Authorization: Bearer $2" --write-out %{http_code} --silent --output /dev/null "$REQUEST_URL")
  else
    REQUEST_URL="$ENDPOINT_DOMAIN_NAME$BASE_PATH${path// /%20}"
    status_code=$(curl -k --write-out %{http_code} --silent --output /dev/null "$REQUEST_URL")
  fi

  if [[ "$status_code" == $1 ]]
  then
    SUCCESS=$((SUCCESS + 1))
    echo "$REQUEST_URL: $status_code - Success"
  else
    FAILURE=$((FAILURE + 1))
    echo "$REQUEST_URL: $status_code - Fail"
  fi
}

smokeTest() {
  if [[ ! "$ENDPOINT_DOMAIN_NAME" == http* ]]; then
    ENDPOINT_DOMAIN_NAME="https://$ENDPOINT_DOMAIN_NAME"
  fi

  for path in "${PATHS[@]}"
    do
      doCurl 200
    done

  # Happy path
  path="/search?serviceType=PrimaryCare&patient=$PATIENT"
  doCurl 200 $TOKEN

  # extendedDriveMin validation (0 to 90)
  path="/search?serviceType=PrimaryCare&patient=$PATIENT&extendedDriveMin=-1"
  doCurl 400 $TOKEN

  path="/search?serviceType=PrimaryCare&patient=$PATIENT&extendedDriveMin=100"
  doCurl 400 $TOKEN

  # Token validation
  path="/search?serviceType=PrimaryCare&patient=$PATIENT"
  doCurl 401 "BADTOKEN"

  path="/search?serviceType=PrimaryCare&patient=123NOTME"
  doCurl 403 $TOKEN

  # Missing parameters
  path="/search?serviceType=PrimaryCare"
  doCurl 500 $TOKEN

  path="/search?patient=$PATIENT"
  doCurl 500 $TOKEN

  printResults
}

regressionTest() {
  if [[ ! "$ENDPOINT_DOMAIN_NAME" == http* ]]; then
    ENDPOINT_DOMAIN_NAME="https://$ENDPOINT_DOMAIN_NAME"
  fi

  for path in "${PATHS[@]}"
    do
      doCurl 200
    done

  # Happy paths
  path="/search?serviceType=Audiology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Cardiology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Dermatology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Gastroenterology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Gynecology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=MentalHealthCare&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Nutrition&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Ophthalmology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Optometry&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Orthopedics&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Podiatry&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=PrimaryCare&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=Urology&patient=$PATIENT"
  doCurl 200 $TOKEN

  path="/search?serviceType=WomensHealth&patient=$PATIENT"
  doCurl 200 $TOKEN

  # Token validation
  path="/search?serviceType=PrimaryCare&patient=$PATIENT"
  doCurl 401 "BADTOKEN"

  path="/search?serviceType=PrimaryCare&patient=123NOTME"
  doCurl 403 $TOKEN

  # Missing Parameters
  path="/search?serviceType=PrimaryCare"
  doCurl 500 $TOKEN

  path="/search?patient=$PATIENT"
  doCurl 500 $TOKEN

  printResults
}

printResults () {
  TOTAL=$((SUCCESS + FAILURE))

  echo "=== TOTAL: $TOTAL | SUCCESS: $SUCCESS | FAILURE: $FAILURE ==="

  if [[ "$FAILURE" -gt 0 ]]; then
  exit 1
  fi
}

# Let's get down to business
ARGS=$(getopt -n $(basename ${0}) \
    -l "endpoint-domain-name:,environment:,token:,base-path:,patient:,help" \
    -o "d:e:t:b:v:p:h" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -d|--endpoint-domain-name) ENDPOINT_DOMAIN_NAME=$2;;
    -e|--environment) ENVIRONMENT=$2;;
    -t|--token) TOKEN=$2;;
    -b|--base-path) BASE_PATH=$2;;
    -p|--patient) PATIENT=$2;;
    -h|--help) usage "I need a hero! I'm holding out for a hero...";;
    --) shift;break;;
  esac
  shift;
done

if [[ -z "$ENDPOINT_DOMAIN_NAME" || -e "$ENDPOINT_DOMAIN_NAME" ]]; then
  usage "Missing variable K8S_LOAD_BALANCER or option --endpoint-domain-name|-d."
fi

if [[ -z "$ENVIRONMENT" || -e "$ENVIRONMENT" ]]; then
  usage "Missing variable K8S_ENVIRONMENT or option --environment|-e."
fi

if [[ -z "$TOKEN" || -e "$TOKEN" ]]; then
  usage "Missing variable TOKEN or option --token|-t."
fi

if [[ -z "$PATIENT" || -e "$PATIENT" ]]; then
  usage "Missing variable PATIENT or option --patient|-p."
fi

[ $# == 0 ] && usage "No command specified"
COMMAND=$1
shift

case "$COMMAND" in
  s|smoke-test) smokeTest;;
  r|regression-test) regressionTest;;
  *) usage "Unknown command: $COMMAND";;
esac

exit 0
