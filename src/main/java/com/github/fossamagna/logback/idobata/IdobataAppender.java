package com.github.fossamagna.logback.idobata;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

/**
 * Logback appendar for Idobata.
 *
 * Appender send the same request that you run the following command.
 * <pre>
 * curl --data-urlencode "source='logging event object'" -d format=html https://idobata.io/hook/custom/TOKEN
 * </pre>
 * @author fossamagna
 */
public class IdobataAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

  private static final String ENCODE = "UTF-8";

  private URL endpointUrl;

  private Layout<ILoggingEvent> layout = new IdobataLayout();

  private boolean html = true;

  @Override
  protected void append(ILoggingEvent eventObject) {
    String message = layout.doLayout(eventObject);
    try {
      postMessage(message, html);
    } catch (IOException e) {
      e.printStackTrace();
      addError("Error posting log to Idobata", e);
    }
  }

  protected void postMessage(String message, boolean html) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) endpointUrl.openConnection();
    connection.setDoOutput(true);
    connection.setRequestMethod("POST");

    StringBuilder body = new StringBuilder();
    if (html) {
      body.append("format=html&");
    }
    body.append("source=").append(URLEncoder.encode(message, ENCODE));
    final byte[] content = body.toString().getBytes(ENCODE);
    connection.setFixedLengthStreamingMode(content.length);
    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
    OutputStream out = null;
    InputStream in = null;
    try {
      out = connection.getOutputStream();
      out.write(content);
      out.flush();

      final int status = connection.getResponseCode();
      final String encoding = connection.getContentEncoding();
      if (status < 400) {
        in = connection.getInputStream();
        toString(in, encoding);
      } else {
        in = connection.getErrorStream();
        addError("Error posting log to Idobata:" + toString(in, encoding));
      }
    } finally {
      close(out);
      close(in);
    }
  }

  private void close(Closeable closeable) throws IOException {
    if (closeable != null) {
      closeable.close();
    }
  }

  String toString(InputStream in, String encoding) throws IOException {
    ByteArrayOutputStream responseBody = new ByteArrayOutputStream();
    byte[] buffer = new byte[1024];
    int count = -1;
    while ((count = in.read(buffer, 0, buffer.length)) != -1) {
      responseBody.write(buffer, 0, count);
    }
    return responseBody.toString(encoding != null ? encoding : "UTF-8");
  }

  public URL getEndpointUrl() {
    return endpointUrl;
  }

  public void setEndpointUrl(URL endpointUrl) {
    this.endpointUrl = endpointUrl;
  }

  public Layout<ILoggingEvent> getLayout() {
    return layout;
  }

  public void setLayout(Layout<ILoggingEvent> layout) {
    this.layout = layout;
  }

  public boolean isHtml() {
    return html;
  }

  public void setHtml(boolean html) {
    this.html = html;
  }
}
