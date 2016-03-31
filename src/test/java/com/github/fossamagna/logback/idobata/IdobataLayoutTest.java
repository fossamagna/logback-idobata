package com.github.fossamagna.logback.idobata;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;

/**
 * Test for {@link IdobataLayout}.
 */
public class IdobataLayoutTest {
  
  private LoggerContext context;
  private Logger logger;
  private IdobataLayout layout;
  
  @Before
  public void setUp() {
    context = new LoggerContext();
    logger = context.getLogger(IdobataLayoutTest.class);
    layout = new IdobataLayout();
    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    df.setTimeZone(TimeZone.getTimeZone("UTC"));
    layout.setDateFormat(df);
  }

  @Test
  public void testDoLayout() {
    LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "Log message{}", null, new Object[]{"!"});
    event.setTimeStamp(0);
    String result = layout.doLayout(event);
    String threadName = Thread.currentThread().getName();
    String expected = String.format(
        "<p>1970-01-01T00:00:00.000+0000 <span class=\"label\" style=\"background-color: green; color: white;\">DEBUG</span> [%s] com.github.fossamagna.logback.idobata.IdobataLayoutTest - <b>Log message!</b></p>", threadName);
    assertThat(result, is(expected));
  }
}
