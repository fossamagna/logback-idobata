package com.github.fossamagna.logback.idobata;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.ThrowableProxyUtil;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.helpers.Transform;

/**
 * Default Layout for Idobata
 * @author fossamagna
 */
public class IdobataLayout extends LayoutBase<ILoggingEvent> {

  private static final Map<Integer, String> DEFAULT_LEVEL_TO_BACKGROUND_COLOR;
  private static final Map<Integer, String> DEFAULT_LEVEL_TO_COLOR;
  private static final DateFormat DEFAULT_DATEFORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

  static {
    Map<Integer, String> levelToBackgroundColor = new HashMap<Integer, String>();
    levelToBackgroundColor.put(Level.TRACE_INTEGER, "blue");
    levelToBackgroundColor.put(Level.DEBUG_INTEGER, "green");
    levelToBackgroundColor.put(Level.INFO_INTEGER, "lightgrey");
    levelToBackgroundColor.put(Level.WARN_INTEGER, "orange");
    levelToBackgroundColor.put(Level.ERROR_INTEGER, "red");
    DEFAULT_LEVEL_TO_BACKGROUND_COLOR = Collections.unmodifiableMap(levelToBackgroundColor);

    Map<Integer, String> levelToColor = new HashMap<Integer, String>();
    levelToColor.put(Level.TRACE_INTEGER, "white");
    levelToColor.put(Level.DEBUG_INTEGER, "white");
    levelToColor.put(Level.INFO_INTEGER, "black");
    levelToColor.put(Level.WARN_INTEGER, "white");
    levelToColor.put(Level.ERROR_INTEGER, "white");
    DEFAULT_LEVEL_TO_COLOR = Collections.unmodifiableMap(levelToColor);
  }

  private Map<Integer, String> levelToBackgroundColor = new HashMap<Integer, String>(DEFAULT_LEVEL_TO_BACKGROUND_COLOR);
  private Map<Integer, String> levelToColor = new HashMap<Integer, String>(DEFAULT_LEVEL_TO_COLOR);
  private DateFormat dateFormat = DEFAULT_DATEFORMAT;
  private boolean outputSystemProperties = true;

  /**
   * {@inheritDoc}
   */
  @Override
  public String doLayout(ILoggingEvent event) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("<p>");
    buffer.append(Transform.escapeTags(format(event.getTimeStamp())));
    buffer.append(" ");
    level(event, buffer);
    buffer.append(" [");
    buffer.append(Transform.escapeTags(event.getThreadName()));
    buffer.append("] ");
    buffer.append(Transform.escapeTags(event.getLoggerName()));
    buffer.append(" - ");
    buffer.append("<b>").append(Transform.escapeTags(event.getFormattedMessage())).append("</b>");
    stacktrace(event, buffer);
    systemProperties(buffer);
    buffer.append("</p>");
    return buffer.toString();
  }

  String format(long timestamp) {
    return dateFormat.format(new Date(timestamp));
  }

  String toBackgroundColor(Level level) {
    return levelToBackgroundColor.get(level.toInt());
  }

  String toColor(Level level) {
    return levelToColor.get(level.toInt());
  }

  void level(ILoggingEvent event, StringBuilder buffer) {
    Level level = event.getLevel();
    buffer.append(String.format(
        "<span class=\"label\" style=\"background-color: %s; color: %s;\">%s</span>",
        toBackgroundColor(level),
        toColor(level),
        level)
    );
  }

  void stacktrace(ILoggingEvent event, StringBuilder buffer) {
    IThrowableProxy tp = event.getThrowableProxy();
    if (tp != null) {
      buffer.append("<pre lang=\"java\"><code>");
      buffer.append(ThrowableProxyUtil.asString(tp));
      buffer.append("</code></pre>");
    }
  }
  
  Properties getSystemProperties() {
    return System.getProperties();
  }

  void systemProperties(StringBuilder buffer) {
    if (!outputSystemProperties) {
      return;
    }
    Properties properties = getSystemProperties();
    buffer.append("<table style=\"table-layout: fixed; width: 100%;\">");
    buffer.append("<caption>System Properties</caption>");
    buffer.append("<thead>");
    tableRow(buffer, "key", "value", true);
    buffer.append("</thead>");
    buffer.append("<tbody>");
    Set<String> names = new TreeSet<String>(properties.stringPropertyNames());
    for (String name : names) {
      tableRow(buffer, name, properties.getProperty(name), false);
    }
    buffer.append("</tbody>");
    buffer.append("</table>");
  }

  void tableRow(StringBuilder buffer, String name, String value, boolean header) {
    String tag = header ? "th" : "td";
    buffer.append("<tr>");
    buffer.append("<").append(tag).append(">").append(Transform.escapeTags(name)).append("</").append(tag).append(">");
    buffer.append("<").append(tag).append(">").append(Transform.escapeTags(value)).append("</").append(tag).append(">");
    buffer.append("</tr>");
  }

  public DateFormat getDateFormat() {
    return dateFormat;
  }

  public void setDateFormat(DateFormat dateFormat) {
    this.dateFormat = dateFormat;
  }

  public void setColor(Level level, String color) {
    if (level == null) {
      throw new IllegalArgumentException("Level must not be null.");
    }
    this.levelToColor.put(level.toInteger(), color);
  }

  public void setBackgroundColor(Level level, String color) {
    if (level == null) {
      throw new IllegalArgumentException("Level must not be null.");
    }
    this.levelToBackgroundColor.put(level.toInteger(), color);
  }

  public boolean isOutputSystemProperties() {
    return outputSystemProperties;
  }

  public void setOutputSystemProperties(boolean outputSystemProperties) {
    this.outputSystemProperties = outputSystemProperties;
  }
}
