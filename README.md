# Logback appender for Idobata [![Build Status][travis-image]][travis-url]

> logback-idobata is simple logback appender for [Idobata](https://idobata.io/).

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

[travis-image]: https://travis-ci.org/fossamagna/logback-idobata.svg?branch=master
[travis-url]: https://travis-ci.org/fossamagna/logback-idobata
