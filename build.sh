# MAVEN_GOAL=deploy (set by CI when publishing) packages and uploads in one build
mvn -B --no-transfer-progress clean ${MAVEN_GOAL:-package}
