set -e

export BC_JDK8=/usr/lib/jvm/java-8-openjdk-amd64
export BC_JDK11=/usr/lib/jvm/java-11-openjdk-amd64
export BC_JDK17=/usr/lib/jvm/java-17-openjdk-amd64
export BC_JDK21=/usr/lib/jvm/java-21-openjdk-amd64
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
# ./gradlew clean build
./gradlew build
