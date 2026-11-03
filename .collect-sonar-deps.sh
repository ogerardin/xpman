#!/bin/bash
M2_REPO=~/.m2/repository
API_DEPS=(
  "org/projectlombok/lombok/1.18.38/lombok-1.18.38.jar"
  "ch/qos/logback/logback-core/1.5.20/logback-core-1.5.20.jar"
  "ch/qos/logback/logback-classic/1.5.20/logback-classic-1.5.20.jar"
  "io/github/classgraph/classgraph/4.8.189/classgraph-4.8.189.jar"
  "commons-io/commons-io/2.22.0/commons-io-2.22.0.jar"
  "org/apache/commons/commons-compress/1.27.1/commons-compress-1.27.1.jar"
  "org/tukaani/xz/1.10/xz-1.10.jar"
  "org/apache/commons/commons-configuration2/2.15.1/commons-configuration2-2.15.1.jar"
  "org/apache/commons/commons-lang3/3.20.0/commons-lang3-3.20.0.jar"
  "org/apache/commons/commons-text/1.15.0/commons-text-1.15.0.jar"
  "com/kichik/pecoff4j/0.4.0/pecoff4j-0.4.0.jar"
  "net/java/dev/jna/jna-platform/5.17.0/jna-platform-5.17.0.jar"
  "net/fornwall/jelf/0.12.0/jelf-0.12.0.jar"
  "com/github/petitparser/petitparser-core/2.4.0/petitparser-core-2.4.0.jar"
  "one/util/streamex/0.8.2/streamex-0.8.2.jar"
  "org/jsoup/jsoup/1.23.2/jsoup-1.23.2.jar"
  "com/google/code/gson/gson/2.13.2/gson-2.13.2.jar"
  "com/google/http-client/google-http-client/1.47.1/google-http-client-1.47.1.jar"
  "com/google/apis/google-api-services-drive/v3-rev20240123-2.0.0/google-api-services-drive-v3-rev20240123-2.0.0.jar"
  "com/google/guava/guava/33.7.1-jre/guava-33.7.1-jre.jar"
  "org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar"
  "net/java/dev/jna/jna/5.17.0/jna-5.17.0.jar"
  "org/apache/commons/commons-logging/1.2/commons-logging-1.2.jar"
  "com/google/guava/failureaccess/1.0.3/failureaccess-1.0.3.jar"
  "com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar"
  "com/google/errorprone/error_prone_annotations/2.36.0/error_prone_annotations-2.36.0.jar"
  "com/google/j2objc/j2objc-annotations/1.4/j2objc-annotations-1.4.jar"
)

FX_DEPS=(
  "org/openjfx/javafx-controls/25/javafx-controls-25-mac-aarch64.jar"
  "org/openjfx/javafx-fxml/25/javafx-fxml-25-mac-aarch64.jar"
  "org/openjfx/javafx-base/25/javafx-base-25-mac-aarch64.jar"
  "org/openjfx/javafx-graphics/25/javafx-graphics-25-mac-aarch64.jar"
  "org/openjfx/javafx-web/25/javafx-web-25-mac-aarch64.jar"
  "org/controlsfx/controlsfx/11.2.1/controlsfx-11.2.1.jar"
)

for dep in "${API_DEPS[@]}" "${FX_DEPS[@]}"; do
  if [ -f "$M2_REPO/$dep" ]; then
    echo "$M2_REPO/$dep"
  fi
done | sort -u | tr '\n' ','
