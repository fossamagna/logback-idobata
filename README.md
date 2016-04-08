# Logback appender for Idobata [![Build Status][travis-image]][travis-url] [![Maven Central][maven-central-image]][maven-central-url]

> logback-idobata is simple logback appender for [Idobata](https://idobata.io/).

## Including it in your project

### Maven

Add logback-idobata to dependencies in your pom.xml file.

```xml
<dependencies>
  <dependency>
    <groupId>com.github.fossamagna</groupId>
    <artifactId>logback-idobata</artifactId>
    <version>0.1.0</version>
  </dependency>
</dependencies>
```

### Gradle

Add logback-idobata to dependencies in your build.gradle file.

```groovy
dependencies {
  compile 'com.github.fossamagna:logback-idobata:0.1.0'
}
```

## Usage

Add IdobataAppender configuration to logback.groovy file.

```groovy
import static ch.qos.logback.classic.Level.*

import com.github.fossamagna.logback.idobata.IdobataAppender

def webHookUrl = System.getenv("IDOBATA_NOTIFICATION_URL")
if (webHookUrl) {
  appender("Idobata", IdobataAppender) {
    endpointUrl = new URL(webHookUrl)
  }
  root(ERROR, ["Idobata"])
}
```

## License
logback-idobata is Open Source software released under the [Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0.html).

[travis-image]:https://travis-ci.org/fossamagna/logback-idobata.svg?branch=master
[travis-url]:https://travis-ci.org/fossamagna/logback-idobata
[maven-central-image]:https://maven-badges.herokuapp.com/maven-central/com.github.fossamagna/logback-idobata/badge.svg
[maven-central-url]:https://maven-badges.herokuapp.com/maven-central/com.github.fossamagna/logback-idobata
