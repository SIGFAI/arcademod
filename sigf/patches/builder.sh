#!/usr/bin/env bash
# Runs on a disposable Amazon Linux 2023 builder (driven through SSM, as root). Builds the SIGF ArcadeMod jar from
# the pinned upstream commit + this patch set, with a verified official Gradle (the repo's gradle-wrapper.jar is not used).
#   builder.sh <inputs_tgz_get_url> <jar_put_url> <log_put_url>
set -euo pipefail
IN_URL="$1"; JAR_URL="$2"; LOG_URL="$3"
PIN=eb09571cf6883651f7516da85682b5e9c9c6b166
GRADLE=8.8
W=/home/ec2-user/build
LOG=/home/ec2-user/build.log
exec > >(tee -a "$LOG") 2>&1
dnf install -y -q java-17-amazon-corretto-devel git unzip
rm -rf "$W"; mkdir -p "$W"; cd "$W"
curl -fsSL -o inputs.tgz "$IN_URL"; mkdir inputs; tar xzf inputs.tgz -C inputs
sha256sum inputs.tgz
git clone -q https://github.com/Bay4lly/ArcadeMod src
git -C src -c advice.detachedHead=false checkout -q "$PIN"
bash inputs/patches/apply.sh "$W/src" "$W/inputs/gen"
rm -f src/gradlew src/gradlew.bat src/gradle/wrapper/gradle-wrapper.jar
curl -fsSL -o gradle.zip "https://services.gradle.org/distributions/gradle-$GRADLE-bin.zip"
echo "$(curl -fsSL "https://services.gradle.org/distributions/gradle-$GRADLE-bin.zip.sha256")  gradle.zip" | sha256sum -c -
unzip -q gradle.zip
chown -R ec2-user:ec2-user "$W"
sudo -u ec2-user -H bash -c "cd '$W/src' && JAVA_HOME=/usr/lib/jvm/java-17-amazon-corretto '$W/gradle-$GRADLE/bin/gradle' --no-daemon build -x test"
JAR="$(ls "$W"/src/build/libs/arcademod-*.jar | grep -v -- '-sources' | head -1)"
ls -la "$W/src/build/libs/"; sha256sum "$JAR"
curl -fsS -X PUT --upload-file "$JAR" "$JAR_URL"
echo BUILD_OK
curl -fsS -X PUT --upload-file "$LOG" "$LOG_URL" || true
