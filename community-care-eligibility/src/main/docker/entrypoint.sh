#!/usr/bin/env bash

ENDPOINT_DOMAIN_NAME="$K8S_LOAD_BALANCER"
ENVIRONMENT="$K8S_ENVIRONMENT"
TOKEN="$TOKEN"
BASE_PATH="$BASE_PATH"
STREET="$STREET"
CITY="$CITY"
STATE="$STATE"
ZIP="$ZIP"
SERVICE_TYPE="$SERVICE_TYPE"
PATIENT="$PATIENT"

#Put Health endpoints here if you got them
PATHS=(/actuator/health \
/openapi.json \
/openapi.yaml)

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
    --base-path=/community-care/v0/eligibility
    --street='46 W New Haven Ave'
    --city=Melbourne
    --state=Fl
    --zip=32901
    --service-type=PrimaryCare
    --patient=12345678V90

$1
EOF
exit 1
}

doCurl () {
  REQUEST_URL="$ENDPOINT_DOMAIN_NAME$BASE_URL${path// /%20}"
  if [[ -n "$2" ]]
  then
    status_code=$(curl -k -H "Authorization: Bearer $2" --write-out %{http_code} --silent --output /dev/null "$REQUEST_URL")
  else
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

  for path in "${PATHS[@]}"
    do
      doCurl 200
    done

  # Happy Path
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 200 $TOKEN

  # Token validation
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 401 "BADTOKEN"

  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=123NOTME"
  doCurl 403 $TOKEN

  # Single missing parameter check for smoke test
  path="/search?city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 500 $TOKEN

  # Unknown ICN
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=UNKNOWN"
  doCurl 404 $TOKEN

  printResults
}

regressionTest() {

  for path in "${PATHS[@]}"
    do
      doCurl 200
    done

  # Happy Path Primary Care
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=PrimaryCare&patient=$PATIENT"
  doCurl 200 $TOKEN

  # Happy Path Primary Care
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=PrimaryCare&patient=$PATIENT"
  doCurl 200 $TOKEN

  # Happy Path Specialty Care (Audiology)
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=Audiology&patient=$PATIENT"
  doCurl 200 $TOKEN

  # Happy Path Specialty Care (Audiology)
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=Audiology&patient=$PATIENT"
  doCurl 200 $TOKEN

  # Token validation
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 401 "BADTOKEN"

  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=123NOTME"
  doCurl 403 $TOKEN

  # Missing Parameters
  path="/search?city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 500 $TOKEN

  path="/search?street=$STREET&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 500 $TOKEN

  path="/search?street=$STREET&city=$CITY&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 500 $TOKEN

  path="/search?street=$STREET&city=$CITY&state=$STATE&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 500 $TOKEN

  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&patient=$PATIENT"
  doCurl 500 $TOKEN

  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=$PATIENT"
  doCurl 500 $TOKEN

  # Unknown ICN
  path="/search?street=$STREET&city=$CITY&state=$STATE&zip=$ZIP&serviceType=$SERVICE_TYPE&patient=UNKNOWN"
  doCurl 404 $TOKEN

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
    -l "endpoint-domain-name:,environment:,token:,base-path:,street:,city:,state:,zip:,service-type:,patient:,help" \
    -o "d:e:t:b:s:c:a:z:v:p:h" -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    -d|--endpoint-domain-name) ENDPOINT_DOMAIN_NAME=$2;;
    -e|--environment) ENVIRONMENT=$2;;
    -t|--token) TOKEN=$2;;
    -b|--base-path) BASE_PATH=$2;;
    -s|--street) STREET=$2;;
    -c|--city) CITY=$2;;
    -a|--state) STATE=$2;;
    -z|--zip) ZIP=$2;;
    -v|--service-type) SERVICE_TYPE=$2;;
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

if [[ -z "$STREET" || -e "$STREET" ]]; then
  usage "Missing variable STREET or option --street|-s."
fi

if [[ -z "$CITY" || -e "$CITY" ]]; then
  usage "Missing variable CITY or option --city|-c."
fi

if [[ -z "$STATE" || -e "$STATE" ]]; then
  usage "Missing variable STATE or option --state|-a."
fi

if [[ -z "$ZIP" || -e "$ZIP" ]]; then
  usage "Missing variable ZIP or option --zip|-z."
fi

if [[ -z "$SERVICE_TYPE" || -e "$SERVICE_TYPE" ]]; then
  usage "Missing variable SERVICE_TYPE or option --service-type|-v."
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
