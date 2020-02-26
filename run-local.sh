#! /usr/bin/env bash



usage() {
cat<<EOF
$0 [options] <command>

Start and stop applications.
If no application options are specified then default applications
will be processed for the operations

Commands
 r, restart   Restart applications
 s, start     Start applications
 st, status   Report status of applications
 k, stop      Stop applications

Options
  -p, --profile         The <profile>-application.properties to use when running the application
  --h2                  If the application support it, use an embedded H2 database
  -h, --help            Display this message and exit
EOF

for name in ${APP_NAME[@]}
do
  printf -- "  -%s, --%-15s Include %s\n" "${APP_SHORT_OPTION[$name]}" "${APP_LONG_OPTION[$name]}" "$name"
done

echo -e "\n$1"
exit 1
}


rebuildHere() {
  local app=$1
  echo "Rebuilding $app (non-standard, not cleaning, no tests)"
  mvn package -P'!standard' -DskipTests -q
  [ $? != 0 ] && echo "Aborting" && exit 1
}

startApp() {
  local app=$1
  local where=${APP_LOCATION[$app]}
  local pid=$(pidOf $app)
  [ -n "$pid" ] && echo "$app appears to already be running ($pid)" && return
  echo "Starting $app"
  cd $REPO
  [ ! -d "$where" ] && echo "$where does not exist" && exit 1
  cd $where
  [ "$REBUILD" == true ] && rebuildHere $app
  local jar=$(find target -maxdepth 1 -name "$app-*.jar" | grep -v -E 'tests|library')
  [ -z "$jar" ] && echo "Cannot find $app application jar" && exit 1
  local options="-Dapp.name=$app"
  if [ "$EMBEDDED_H2_ENABLED" == true -a "${APP_H2[$app]}" == "true" ]
  then
    local pathSeparator=':'
    [ "$(uname)" != "Darwin" ] && [ "$(uname)" != "Linux" ] && echo "Add support for your operating system" && exit 1
    echo "Using local H2 database"
    options+=" -cp $(readlink -f $jar)${pathSeparator}$(readlink -f ~/.m2/repository/com/h2database/h2/${H2_VERSION}/h2-${H2_VERSION}.jar)"
    options+=" -Dspring.jpa.generate-ddl=true"
    options+=" -Dspring.jpa.hibernate.ddl-auto=create-drop"
    options+=" -Dspring.jpa.hibernate.globally_quoted_identifiers=false"
    options+=" -Dspring.datasource.driver-class-name=org.h2.Driver"
    options+=" -Dspring.datasource.url=jdbc:h2:mem:local"
    options+=" -Dspring.h2.console.enabled=true"
    java ${options} org.springframework.boot.loader.PropertiesLauncher &
  else
    java ${options} -jar $jar &
  fi
}

stopApp() {
  local app=$1
  local pid=$(pidOf $app)
  [ -z "$pid" ] && echo "$app does not appear to be running" && return
  echo "Stopping $app ($pid)"
  if [ "$OSTYPE" == "msys" ]; then
    taskkill //F //PID $pid
  else
    kill $pid
  fi
}

pidOf() {
  local app=$1
  jps -v | grep -F -- "-Dapp.name=$app" | cut -d ' ' -f 1
}

statusOf() {
  local app=$1
  local pid=$(pidOf $app)
  local running="RUNNING"
  [ -z "$pid" ] && running="NOT RUNNING"
  printf "%-20s   %-11s   %s\n" $app "$running" $pid
}

doStatus() {
  for n in ${APP_NAME[@]}; do statusOf $n; done
}

doStart() {
  export SPRING_PROFILES_ACTIVE
  echo "Using profile: $SPRING_PROFILES_ACTIVE"
  for n in $(enabledApps); do startApp $n; done
}

doStop() {
  for n in $(enabledApps); do stopApp $n; done
}



applicationUsage() {
  cat<<EOF
application [options]

Options
  --name <name> The application name
  --short-option <letter> The short command line option enabling this application
  --long-option <name> The long command line option enabling this application
  --enabled-by-default <true|false> Whether this application should be enabled by default
  --location <path> The relative path to the application directory from this script

$1
EOF
  exit 1
}

application() {
  local name
  local shortOption
  local longOption
  local enabledByDefault
  local location
  local h2=false
  local args=$(getopt -n "application" \
    -l "name:,short-option:,long-option:,enabled-by-default:,location:,h2:" \
    -o "" -- "$@")
  [ $? != 0 ] && applicationUsage
  eval set -- "$args"
  while true
  do
    case "$1" in
      --name) name="$2";;
      --short-option) shortOption="$2";;
      --long-option) longOption="$2";;
      --enabled-by-default) enabledByDefault="$2";;
      --location) location="$2";;
      --h2) h2="$2";;
      --) shift; break;;
    esac
    shift
  done
  [ -z "$name" ] && applicationUsage "--name not specified"
  [ -z "$shortOption" ] && applicationUsage "--short-option not specified"
  [ -z "$longOption" ] && applicationUsage "--long-option not specified"
  [ -z "$h2" ] && applicationUsage "--h2 not specified"
  [ "$enabledByDefault" != true -a "$enabledByDefault" != false ] \
    && applicationUsage "--enabled-by-default must be true or false, was: $enabledByDefault"
  [ -z "$location" ] && applicationUsage "--location not specified"
  APP_ENABLED[$name]="false"
  APP_ENABLED_BY_DEFAULT[$name]="$enabledByDefault"
  APP_H2[$name]="$h2"
  APP_LOCATION[$name]="$location"
  APP_LONG_OPTION[$name]="$longOption"
  APP_NAME[$name]="$name"
  APP_SHORT_OPTION[$name]="$shortOption"
  LONG_OPTION_TO_APP[$longOption]="$name"
  SHORT_OPTION_TO_APP[$shortOption]="$name"
}

isAnyAppEnabled() {
  for e in ${APP_ENABLED[@]}; do [ "$e" == true ] && return 0; done
  return 1
}

enableDefaultApps() {
  for n in ${!APP_ENABLED_BY_DEFAULT[@]}
  do
    [ "${APP_ENABLED_BY_DEFAULT[$n]}" == "true" ] && APP_ENABLED[$n]=true
  done
}

enabledApps() {
  for n in ${!APP_ENABLED[@]}; do [ "${APP_ENABLED[$n]}" == true ] && echo $n; done
}

declare -A APP_ENABLED
declare -A APP_ENABLED_BY_DEFAULT
declare -A APP_H2
declare -A APP_LOCATION
declare -A APP_LONG_OPTION
declare -A APP_NAME
declare -A APP_SHORT_OPTION
declare -A LONG_OPTION_TO_APP
declare -A SHORT_OPTION_TO_APP



REPO=$(cd $(dirname $0) && pwd)
. $REPO/.run-local.conf

REBUILD=false
SPRING_PROFILES_ACTIVE=dev
EMBEDDED_H2_ENABLED=false
H2_VERSION=1.4.197

ARGS=$(getopt -n $(basename ${0}) \
    -l "debug,help,h2,profile:,rebuild,$(echo "${!LONG_OPTION_TO_APP[@]}" | sort | tr ' ' ,)" \
    -o "rhp:$(echo "${!SHORT_OPTION_TO_APP[@]}" | sort | tr -d ' ')" \
    -- "$@")
[ $? != 0 ] && usage
eval set -- "$ARGS"
while true
do
  case "$1" in
    --debug) set -x;;
    -h|--help) usage "halp! what this do?";;
    --h2) EMBEDDED_H2_ENABLED=true;;
    -p|--profile) SPRING_PROFILES_ACTIVE="$2";;
    -r|--rebuild) REBUILD=true;;
    --) shift;break;;
  esac
  SIMPLE_OPTION=${1##*-}
  MAYBE_APP="${SHORT_OPTION_TO_APP[$SIMPLE_OPTION]}"
  [ -z "$MAYBE_APP" ] && APP_OPTION="${LONG_OPTION_TO_APP[$SIMPLE_OPTION]}"
  [ -n "$MAYBE_APP" ] && APP_ENABLED[$MAYBE_APP]=true
  shift;
done

if ! isAnyAppEnabled; then enableDefaultApps; fi

[ $# != 1 ] && usage
COMMAND=$1

case $COMMAND in
  s|start) doStart;;
  st|status) doStatus;;
  k|stop) doStop;;
  r|restart) doStop;doStart;;
  *) usage "Unknown command: $COMMAND";;
esac
