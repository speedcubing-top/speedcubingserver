# MAVEN_GOAL=deploy (set by CI when publishing) packages and uploads in one build
# -U: the CI Maven cache otherwise keeps a day-old SNAPSHOT of speedcubingCommon/Lib
mvn -B -U --no-transfer-progress clean ${MAVEN_GOAL:-package}
