package com.github.fossamagna.logback.idobata;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.net.URLStreamHandler;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.ContextBase;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.layout.EchoLayout;
import ch.qos.logback.core.status.Status;

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
  }

  @Test
  public void testAppendILoggingEvent() throws IOException {
    final String enc = "UTF-8";
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
    final HttpURLConnection connection = mock(HttpURLConnection.class);
    when(connection.getResponseCode()).thenReturn(200);
    when(connection.getOutputStream()).thenReturn(outputStream);
    when(connection.getInputStream()).thenReturn(inputStream);
    doNothing().when(connection).setDoOutput(true);
    doNothing().when(connection).setRequestMethod("POST");
    final URL url = getEndpointURL(connection);

    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.toString()).thenReturn("log message.");

    appender.setLayout(new EchoLayout<ILoggingEvent>());
    appender.setEndpointUrl(url);
    appender.start();
    appender.doAppend(event);
    appender.stop();

    assertThat(new String(outputStream.toByteArray(), enc), is("format=html&source=" + URLEncoder.encode("log message." + CoreConstants.LINE_SEPARATOR, enc)));

    verify(connection).setDoOutput(true);
    verify(connection).setRequestMethod("POST");
    verify(connection).getResponseCode();
    verify(connection).getOutputStream();
    verify(connection).getInputStream();
    verify(connection, never()).getErrorStream();
  }

  @Test
  public void testAppendILoggingEvent_NoHtml() throws IOException {
    final String enc = "UTF-8";
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final ByteArrayInputStream inputStream = new ByteArrayInputStream(new byte[0]);
    final HttpURLConnection connection = mock(HttpURLConnection.class);
    when(connection.getResponseCode()).thenReturn(200);
    when(connection.getContentEncoding()).thenReturn(enc);
    when(connection.getOutputStream()).thenReturn(outputStream);
    when(connection.getInputStream()).thenReturn(inputStream);
    doNothing().when(connection).setDoOutput(true);
    doNothing().when(connection).setRequestMethod("POST");
    final URL url = getEndpointURL(connection);

    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.toString()).thenReturn("log message.");

    appender.setLayout(new EchoLayout<ILoggingEvent>());
    appender.setHtml(false);
    appender.setEndpointUrl(url);
    appender.start();
    appender.doAppend(event);
    appender.stop();

    assertThat(new String(outputStream.toByteArray(), enc), is("source=" + URLEncoder.encode("log message." + CoreConstants.LINE_SEPARATOR, enc)));

    verify(connection).setDoOutput(true);
    verify(connection).setRequestMethod("POST");
    verify(connection).getResponseCode();
    verify(connection).getContentEncoding();
    verify(connection).getOutputStream();
    verify(connection).getInputStream();
    verify(connection, never()).getErrorStream();
  }

  @Test
  public void testAppendILoggingEvent_OnErrorResponse() throws IOException {
    final String enc = "UTF-8";
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    final ByteArrayInputStream inputStream = new ByteArrayInputStream("Bad Request".getBytes(enc));
    final HttpURLConnection connection = mock(HttpURLConnection.class);
    when(connection.getResponseCode()).thenReturn(400);
    when(connection.getOutputStream()).thenReturn(outputStream);
    when(connection.getErrorStream()).thenReturn(inputStream);
    doNothing().when(connection).setDoOutput(true);
    doNothing().when(connection).setRequestMethod("POST");
    final URL url = getEndpointURL(connection);

    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.toString()).thenReturn("log message.");

    appender.setLayout(new EchoLayout<ILoggingEvent>());
    appender.setEndpointUrl(url);
    appender.start();
    appender.doAppend(event);
    appender.stop();

    assertThat(new String(outputStream.toByteArray(), enc), is("format=html&source=" + URLEncoder.encode("log message." + CoreConstants.LINE_SEPARATOR, enc)));

    verify(connection).setDoOutput(true);
    verify(connection).setRequestMethod("POST");
    verify(connection).getResponseCode();
    verify(connection).getContentEncoding();
    verify(connection).getOutputStream();
    verify(connection).getErrorStream();
    verify(connection, never()).getInputStream();
  }
  
  @Test
  public void testAppend_postMessageThrowIOException() throws IOException {
    final String enc = "UTF-8";
    final ByteArrayOutputStream outputStream = spy(new ByteArrayOutputStream());
    doThrow(IOException.class).when(outputStream).flush();
    final ByteArrayInputStream inputStream = new ByteArrayInputStream("Bad Request".getBytes(enc));
    final HttpURLConnection connection = mock(HttpURLConnection.class);
    when(connection.getResponseCode()).thenReturn(400);
    when(connection.getOutputStream()).thenReturn(outputStream);
    when(connection.getErrorStream()).thenReturn(inputStream);
    doNothing().when(connection).setDoOutput(true);
    doNothing().when(connection).setRequestMethod("POST");
    final URL url = getEndpointURL(connection);

    ILoggingEvent event = mock(ILoggingEvent.class);
    when(event.toString()).thenReturn("log message.");

    appender.setLayout(new EchoLayout<ILoggingEvent>());
    appender.setEndpointUrl(url);
    appender.start();
    appender.doAppend(event);
    appender.stop();
    
    List<Status> statusList = appender.getContext().getStatusManager().getCopyOfStatusList();
    assertThat(statusList, hasSize(1));
    Status status = statusList.get(0);
    assertThat(status.getLevel(), is(Status.ERROR));
    assertThat(status.getMessage(), is("Error posting log to Idobata"));

    verify(connection).setDoOutput(true);
    verify(connection).setRequestMethod("POST");
    verify(connection, never()).getResponseCode();
    verify(connection, never()).getContentEncoding();
    verify(connection).getOutputStream();
    verify(connection, never()).getErrorStream();
    verify(connection, never()).getInputStream();
  }

  @Test
  public void testHtml() {
    assertThat(appender.isHtml(), is(true));
    appender.setHtml(false);
    assertThat(appender.isHtml(), is(false));
  }

  @Test
  public void testEndpointUrl() throws MalformedURLException {
    final String url = "https:https://idobata.io/hook/custom/secret";
    assertThat(appender.getEndpointUrl(), is(nullValue()));
    appender.setEndpointUrl(new URL(url));
    assertThat(appender.getEndpointUrl(), is(new URL(url)));
  }

  @Test
  public void testLayout() {
    assertThat(appender.getLayout(), is(instanceOf(IdobataLayout.class)));
    appender.setLayout(new EchoLayout<ILoggingEvent>());
    assertThat(appender.getLayout(), is(instanceOf(EchoLayout.class)));
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
