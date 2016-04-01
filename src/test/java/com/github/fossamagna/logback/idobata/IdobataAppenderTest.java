package com.github.fossamagna.logback.idobata;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URLStreamHandler;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.layout.EchoLayout;

/**
 * Test for {@link IdobataAppender}.
 */
public class IdobataAppenderTest {

  private IdobataAppender appender;
  
  @Before
  public void setUp() {
    appender = new IdobataAppender();
    Context context = new ContextBase();
    appender.setContext(context);
    appender.setHtml(false);
    appender.setLayout(new EchoLayout<ILoggingEvent>());
  }

  @Test
  public void testAppendILoggingEvent() throws IOException {
    final String enc = "UTF-8";
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final HttpURLConnection connection = mock(HttpURLConnection.class);
    when(connection.getOutputStream()).thenReturn(outputStream);
    doNothing().when(connection).setDoOutput(true);
    doNothing().when(connection).setRequestMethod("POST");
    final URL url = getEndpointURL(connection);
    
    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.toString()).thenReturn("log message.");
    
    appender.setEndpointUrl(url);
    appender.start();
    appender.doAppend(event);
    appender.stop();
    
    assertThat(new String(outputStream.toByteArray(), enc), is("source=" + URLEncoder.encode("log message." + CoreConstants.LINE_SEPARATOR, enc)));
  
    verify(connection).setDoOutput(true);
    verify(connection).setRequestMethod("POST");
    verify(connection).getOutputStream();
  }

  URL getEndpointURL(final HttpURLConnection connection) throws IOException {
    URLStreamHandler urlStreamHandler = new URLStreamHandler() {
      @Override
      protected URLConnection openConnection(URL u) throws IOException {
        return connection;
      }
    };
    return new URL(null, "https://idobata.io/hook/custom/xxxxxxxxxx", urlStreamHandler);
  }
}
