package se.michaelthelin.spotify;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

import se.michaelthelin.spotify.UtilProtos.Url;
import se.michaelthelin.spotify.exceptions.UnexpectedResponseException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SpotifyHttpManager implements HttpManager {

  private HttpConnectionManager connectionManager = null;

  /**
   * Construct a new SpotifyHttpManager instance.
   */
  public SpotifyHttpManager(Builder builder) {
    if (builder.connectionManager != null) {
      connectionManager = builder.connectionManager;
    } else {
      connectionManager = new MultiThreadedHttpConnectionManager();
    }
  }

  @Override
  public String get(Url url) throws UnexpectedResponseException, IOException {
    assert (url != null);
    String uri = UrlUtil.assemble(url);
    GetMethod method = new GetMethod(uri);
    method.setQueryString(getParametersAsNamedValuePairArray(url));
    method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    method.getParams().setParameter(
            HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
    return execute(method);
  }

  private NameValuePair[] getParametersAsNamedValuePairArray(Url url) {
    List<NameValuePair> out = new ArrayList<NameValuePair>();
    for (Url.Parameter parameter : url.getParametersList()) {
      if (parameter.hasName() && parameter.hasValue()) {
        out.add(new NameValuePair(parameter.getName(), parameter.getValue().toString()));
      }
    }
    return out.toArray(new NameValuePair[out.size()]);
  }

  private String execute(HttpMethod method) throws UnexpectedResponseException, IOException {
    HttpClient httpClient = new HttpClient(connectionManager);
    try {
      httpClient.executeMethod(method);
      String responseBody = method.getResponseBodyAsString();
      if (responseBody == null) {
        throw new UnexpectedResponseException();
      }
      return responseBody;
    } catch (IOException e) {
      throw new IOException();
    } finally {
      method.releaseConnection();
    }
  }

  @Override
  public String post(UtilProtos.Url url) throws IOException, UnexpectedResponseException {
    assert (url != null);
    String uri = UrlUtil.assemble(url);
    PostMethod method = new PostMethod(uri);
    method.setQueryString(getParametersAsNamedValuePairArray(url));
    method.setRequestBody(getBodyParametersAsNamedValuePairArray(url));
    for (Url.Parameter header : url.getHeaderParametersList()) {
      method.setRequestHeader(header.getName(), header.getValue());
    }
    method.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
    method.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
    return execute(method);
  }

  private NameValuePair[] getBodyParametersAsNamedValuePairArray(Url url) {
    List<NameValuePair> out = new ArrayList<NameValuePair>();
    for (Url.Parameter parameter : url.getBodyParametersList()) {
      if (parameter.hasName() && parameter.hasValue()) {
        out.add(new NameValuePair(parameter.getName(), parameter.getValue().toString()));
      }
    }
    return out.toArray(new NameValuePair[out.size()]);
  }


  @Override
  public String delete(UtilProtos.Url url) {
    throw new RuntimeException("Not implemented");
  }

  @Override
  public String put(UtilProtos.Url url) {
    throw new RuntimeException("Not implemented");
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {

    private HttpConnectionManager connectionManager = null;

    public Builder() {}

    public SpotifyHttpManager build() {
      return new SpotifyHttpManager(this);
    }

  }

}
