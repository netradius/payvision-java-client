package com.netradius.payvision.http;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.netradius.payvision.PayvisionException;
import com.netradius.payvision.response.ResponseContentType;
import com.netradius.payvision.util.LogFilter;
import com.netradius.payvision.util.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.xml.bind.DatatypeConverter;

/**
 * A simple HTTP client used to send POST requests. This class uses HttpURLConnection internally.
 *
 * @author Erik R. Jensen
 * @author Abhinav Nahar
 */
public class HttpURLConnectionClient implements HttpClient {

  protected static ObjectMapper mapper = new ObjectMapper();
  protected static ObjectMapper xmlMapper = new XmlMapper();
  protected String authorization;
  protected String username;
  protected String password;
  protected LogFilter log;

  /**
   * Creates a new HTTP client.
   *
   * @param username the usernameN
   * @param password the password
   */
  public HttpURLConnectionClient(String username, String password) {
    log = new LogFilter(LoggerFactory.getLogger(getClass()), password);
    try {
      String encoded = URLEncoder.encode(password, "UTF-8");
      if (!password.equals(encoded)) {
        log.addFilteredValue(encoded);
      }
    } catch (UnsupportedEncodingException x) { /* do nothing */ }
    authorization = username + ":" + password;
    authorization = "Basic " + DatatypeConverter.printBase64Binary(authorization.getBytes(Charset
        .forName("UTF-8")));
    this.username = username;
    this.password = password;
    SimpleModule module = new SimpleModule();
    module.setDeserializerModifier(new BeanDeserializerModifier() {
      @Override
      public JsonDeserializer<Enum> modifyEnumDeserializer(DeserializationConfig config,
                                                           JavaType type,
                                                           BeanDescription beanDesc,
                                                           JsonDeserializer<?> deserializer) {
        return new JsonDeserializer<Enum>() {
          @Override
          @SuppressWarnings("unchecked")
          public Enum deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException {
            Class<? extends Enum> rawClass = (Class<Enum<?>>) type.getRawClass();
            return Enum.valueOf(rawClass, jp.getValueAsString().toUpperCase());
          }
        };
      }
    });
    module.addSerializer(Enum.class, new StdSerializer<Enum>(Enum.class) {
      @Override
      public void serialize(Enum value, JsonGenerator jgen, SerializerProvider provider) throws
          IOException {
        jgen.writeString(value.name().toLowerCase());
      }
    });
    mapper.registerModule(module);
  }

  protected void logRequest(HttpURLConnection conn, String body) {
    StringBuilder sb = new StringBuilder("\nHTTP Request:\n")
        .append("  URL: ").append(conn.getURL()).append("\n")
        .append("  Request Method: ").append(conn.getRequestMethod()).append("\n");
    Map<String, List<String>> headers = conn.getRequestProperties();
    sb.append("  Request Headers:\n");
    headers.keySet().stream().filter(key -> key != null).forEach(
        key -> sb.append("    ").append(key).append(": ").append(headers.get(key)).append("\n"));
    sb.append("  Request Body:\n").append(body);
    log.debug(sb.toString());
  }

  protected void logResponse(HttpURLConnection conn, String body) throws IOException {
    StringBuilder sb = new StringBuilder("\nHTTP Response:\n")
        .append("  Response Code: ").append(conn.getResponseCode()).append("\n");
    Map<String, List<String>> headers = conn.getHeaderFields();
    sb.append("  Response Headers:\n");
    headers.keySet().stream().filter(key -> key != null).forEach(
        key -> sb.append("    ").append(key).append(": ").append(headers.get(key)).append("\n"));
    sb.append("  Response Body:\n").append(body);
    log.debug(sb.toString());
  }

  /**
   * Helper method to read the contents of an InputStream to a String.
   * This method will not close the stream.
   *
   * @param in the InputStream to rea
   * @return the contents of the stream as a String
   * @throws IOException if an I/O error occurs
   */
  protected String readString(InputStream in) throws IOException {
    InputStreamReader reader = new InputStreamReader(in, Charset.forName("UTF-8"));
    StringBuilder sb = new StringBuilder();
    char[] buf = new char[8192];
    for (int read = reader.read(buf); read >= 0; read = reader.read(buf)) {
      sb.append(buf, 0, read);
    }
    return sb.toString();
  }

  /**
   * Helper method to completely read the error stream.
   *
   * @param conn the connection
   * @return the error message or null
   */
  protected String readError(HttpURLConnection conn) {
    InputStream err = null;
    try {
      err = conn.getErrorStream();
      if (err != null) {
        return readString(err);
      }
    } catch (IOException x) {
      log.warn("An I/O error occurred reading the HTTP error stream: " + x.getMessage(), x);
    } finally {
      if (err != null) {
        try {
          err.close();
        } catch (IOException x) { /* do nothing */ }
      }
    }
    return null;
  }

  /**
   * Used for error handling.
   *
   * @param x    the IO error that occurred
   * @param conn the connection
   * @return the exception containing the parsed error message and HTTP status code if any
   * @throws IOException if an I/O error occurs
   */
  protected PayvisionException getError(IOException x, HttpURLConnection conn) throws IOException {
    if (conn != null) {
      String error = readError(conn);
      if (log.isDebugEnabled()) {
        logResponse(conn, error);
      }
      int status = conn.getResponseCode();
      return new PayvisionException(status, error, x);
    }
    throw x;
  }

  /**
   * Helper method to setup the connection.
   *
   * @param url the URL to query
   * @return the configured connection
   * @throws IOException if an I/O error occurs
   */
  protected HttpURLConnection setup(String url) throws IOException {
    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
    conn.setRequestProperty("Authorization", authorization);
    conn.setRequestMethod("POST");
    conn.setConnectTimeout(120 * 1000); // 2 minutes // TODO Make configurable
    conn.setReadTimeout(120 * 1000); // 2 minutes // TODO Make configurable
    return conn;
  }

  /**
   * Helper method to cleanup connection resources after use.
   *
   * @param conn the connection or null
   * @param in   the input stream or null
   * @param out  the output stream or null
   */
  protected void cleanup(HttpURLConnection conn, InputStream in, OutputStream out) {
    if (in != null) {
      try {
        in.close();
      } catch (IOException x) { /* do nothing */ }
    }
    if (out != null) {
      try {
        out.close();
      } catch (IOException x) { /* do nothing */ }
    }
    if (conn != null) {
      conn.disconnect();
    }
  }

  @SuppressWarnings("unchecked")
  private String mapToQueryParams(Map<String, Object> params) {
    DecimalFormat fmt = new DecimalFormat("0.00");
    QueryStringBuilder qb = new QueryStringBuilder()
        .add("username", username)
        .add("password", password);
    for (Map.Entry<String, Object> entrySet : params.entrySet()) {
      if (entrySet.getValue() instanceof Map) {
        Map<String, Object> temp = (Map) entrySet.getValue();
        temp.entrySet()
            .stream()
            .filter(entrySet1 -> entrySet1.getValue() != null && StringUtils.hasText(entrySet1
                .getValue().toString()))
            .forEach(entrySet1 -> {
              if (entrySet1.getValue() instanceof Double) {
                String amtFmt = fmt.format(entrySet1.getValue());
                qb.add(entrySet1.getKey(), amtFmt);
              } else if (entrySet1.getValue() instanceof Enum) {
                qb.add(entrySet.getKey(), entrySet1.getValue().toString().toLowerCase());
              } else {
                qb.add(entrySet1.getKey(), entrySet1.getValue());
              }
            });
      } else {
        if (entrySet.getValue() != null && StringUtils.hasText(entrySet.getValue().toString())) {
          if (entrySet.getValue() instanceof Double) {
            String amtFmt = fmt.format(entrySet.getValue());
            qb.add(entrySet.getKey(), amtFmt);
          } else if (entrySet.getValue() instanceof Enum) {
            qb.add(entrySet.getKey(), entrySet.getValue().toString().toLowerCase());
          } else {
            qb.add(entrySet.getKey(), entrySet.getValue());
          }
        }
      }
    }
    return qb.toQueryString();
  }

  private <T> T convertStringToResponse(String response, Class<T> responseTypeClazz,
                                        ResponseContentType contentType) {
    int index = response.indexOf("&");
    Map<String, String> responseParams = new HashMap<>();
    while (index != -1) {
      String temp = response.substring(0, index);
      response = response.substring(index + 1);
      index = response.indexOf("&");
      int indexOfEqual = temp.indexOf("=");
      if (indexOfEqual != -1) {
        String paramName = temp.substring(0, indexOfEqual);
        String paramValue = temp.substring(indexOfEqual + 1);
        responseParams.put(paramName, paramValue);
      }
    }
    int indexOfEqual = response.indexOf("=");
    if (indexOfEqual != -1) {
      String paramName = response.substring(0, indexOfEqual);
      String paramValue = response.substring(indexOfEqual + 1);
      responseParams.put(paramName, paramValue);
    }
    if (contentType == ResponseContentType.XML) {
      return xmlMapper.convertValue(responseParams, responseTypeClazz);
    } else {
      return mapper.convertValue(responseParams, responseTypeClazz);
    }
    //TODO res.setTransactionStatus(TransactionStatus.getTransactionStatusById(
    // res.getTransactionStatusId()));
    //TODO res.setResponseStatus(PayVisionResponseStatus.getTransactionStatusById(
    // res.getResponseCode()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T post(String url, Class<T> responseTypeClazz, Object requestObject,
                    ResponseContentType contentType)
      throws IOException {
    HttpURLConnection conn = null;
    InputStream in = null;
    OutputStream out = null;
    try {
      conn = setup(url);
      conn.setDoOutput(true);

      TreeMap<String, Object> params = mapper.convertValue(requestObject, TreeMap.class);
      String queryString = mapToQueryParams(params);
      if (log.isDebugEnabled()) {
        logRequest(conn, queryString);
      }
      conn.connect();
      out = conn.getOutputStream();
      out.write(queryString.getBytes(Charset.forName("UTF-8")));

      in = conn.getInputStream();
      String res = readString(in);
      if (log.isDebugEnabled()) {
        logResponse(conn, res);
      }
      if (contentType == ResponseContentType.XML) {
        return xmlMapper.readValue(res, responseTypeClazz);
      } else {
        return convertStringToResponse(res, responseTypeClazz, contentType);
      }

    } catch (IOException x) {
      throw getError(x, conn);
    } finally {
      cleanup(conn, in, out);
    }
  }
}
