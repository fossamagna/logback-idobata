package com.github.fossamagna.logback.idobata;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.CoreConstants;

/**
 * Test for {@link IdobataLayout}.
 */
public class IdobataLayoutTest {
  @Rule
  public ExpectedException thrown = ExpectedException.none();

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
    LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "Log message{}: <br>", null, new Object[]{"!"});
    event.setTimeStamp(0);
    String result = layout.doLayout(event);
    String threadName = Thread.currentThread().getName();
    String expected = String.format(
        "<p>1970-01-01T00:00:00.000+0000 <span class=\"label\" style=\"background-color: green; color: white;\">DEBUG</span> [%s] com.github.fossamagna.logback.idobata.IdobataLayoutTest - <b>Log message!: &lt;br&gt;</b></p>", threadName);
    assertThat(result, is(expected));
  }
  
  @Test
  public void testStacktrace() {
    ThrowableProxy tp = spy(new ThrowableProxy(new RuntimeException("Error :scream:")));
    StackTraceElementProxy step1 = new StackTraceElementProxy(new StackTraceElement("Foo", "foo", "Foo.java", 123));
    StackTraceElementProxy step2 = new StackTraceElementProxy(new StackTraceElement("Boo", "boo", "Boo.java", 321));
    doReturn(new StackTraceElementProxy[]{step1, step2}).when(tp).getStackTraceElementProxyArray();
    
    LoggingEvent event = new LoggingEvent("", logger, Level.DEBUG, "Log message{}", null, new Object[]{"!"});
    event.setTimeStamp(0);
    event.setThrowableProxy(tp);
    StringBuilder buffer = new StringBuilder();
    layout.stacktrace(event, buffer);

    final char tab = CoreConstants.TAB;
    final String ls = CoreConstants.LINE_SEPARATOR;
    final String expected = String.format("<pre lang=\"java\"><code>java.lang.RuntimeException: Error :scream:%s%sat Foo.foo(Foo.java:123)%s%sat Boo.boo(Boo.java:321)%s</code></pre>", ls, tab, ls, tab, ls);
    assertThat(buffer.toString(), is(expected));

    verify(tp).getStackTraceElementProxyArray();
  }

  @Test
  public void testDateFormat() {
    assertThat(layout.getDateFormat(), is(notNullValue()));
    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-DD");
    layout.setDateFormat(dateFormat);
    assertThat(layout.getDateFormat(), sameInstance(dateFormat));
  }

  @Test
  public void testColor() {
    layout.setColor(Level.DEBUG, "lightgrey");
    assertThat(layout.toColor(Level.DEBUG), is("lightgrey"));
  }

  @Test
  public void testSetColorNullLevel() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Level must not be null.");
    layout.setColor(null, "lightgrey");
  }

  @Test
  public void testBackgroundColor() {
    layout.setBackgroundColor(Level.DEBUG, "lightgreen");
    assertThat(layout.toBackgroundColor(Level.DEBUG), is("lightgreen"));
  }

  @Test
  public void testSetBackgroundColorNullLevel() {
    thrown.expect(IllegalArgumentException.class);
    thrown.expectMessage("Level must not be null.");
    layout.setBackgroundColor(null, "lightgrey");
  }
}
