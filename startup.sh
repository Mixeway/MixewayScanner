#!/bin/bash
PROFILES=("REST" "STANDALONE")
# Run DTrack

echo "Starting Dependency-Track"
cd /opt/dtrack/ && nohup java -Xmx4G -XX:ActiveProcessorCount=2 -jar dependency-track-embedded.war > /opt/dtrack/dtrack.log 2>&1 &
echo "Waiting for NVD to load"
sleep 25
echo "Starting Mixeway Scanner APP"

if [ -n "$MODE" ]; then
  if [[ "$MODE" = "REST" ]] ; then
    echo "Selected mode: REST"
    cd /app && java -jar /app/app.jar
  elif [[ "$MODE" = "STANDALONE" ]] ; then
    echo "Selected mode: STANDALONE"
    cd /app && java -jar -Dspring.main.web-application-type=NONE /app/app.jar
  else
    echo "Unknown MODE - $MODE, quitting.."
    exit 1
  fi
else
  echo "MODE variable is not set, running REST which is default mode"
  cd /app && java -jar /app/app.jar
fi

