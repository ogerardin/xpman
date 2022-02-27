#!/bin/zsh

set -e

readonly TARGET_JAR="target/xpman-fx-dist-*-repackaged.jar"
readonly TARGET_VER=17

#Directory to extract the jar
readonly TMP_DIR="target/unzip"
mkdir -p ${TMP_DIR}
rm -rf ${TMP_DIR}
#trap 'rm -rf ${TMP_DIR}' EXIT

#Extract the jar
unzip -q "${TARGET_JAR}" -d "${TMP_DIR}"

JARS=(${TMP_DIR}/BOOT-INF/lib/*.jar)
JARS=${(j.:.)JARS}

#echo $JARS

#output
jdeps \
    --recursive \
    --class-path "${TMP_DIR}/BOOT-INF/classes:${TMP_DIR}/BOOT-INF/lib" \
    --module-path "${TMP_DIR}/BOOT-INF/lib" \
    --print-module-deps \
    --ignore-missing-deps \
    --multi-release ${TARGET_VER} \
   ${TMP_DIR}/BOOT-INF/lib/xpman-fx-1.0-SNAPSHOT.jar