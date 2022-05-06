package com.github.badpop.easyhttp.control;

import com.github.badpop.easyhttp.AbstractEasyHttpClient;
import io.vavr.control.Option;
import lombok.Getter;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

import static lombok.AccessLevel.NONE;
import static lombok.AccessLevel.PROTECTED;

@Value
@NonFinal
@FieldDefaults(makeFinal = true, level = PROTECTED)
abstract sealed class AbstractEasyHttpResponse<T> implements HttpResponse<T> permits EasyHttpResponse {
  @Getter(NONE)
  protected AbstractEasyHttpClient usedClient;

  protected HttpResponse<T> originalResponse;
  protected BodyHandler<T> originalBodyHandler;
  protected HttpRequest originalRequest;

  protected AbstractEasyHttpResponse(
    @NonNull HttpResponse<T> originalResponse,
    @NonNull BodyHandler<T> originalBodyHandler,
    @NonNull HttpRequest originalRequest,
    @NonNull AbstractEasyHttpClient usedClient) {
    this.originalResponse = originalResponse;
    this.originalBodyHandler = originalBodyHandler;
    this.originalRequest = originalRequest;
    this.usedClient = usedClient;
  }

  @Override
  public int statusCode() {
    return originalResponse.statusCode();
  }

  @Override
  public HttpRequest request() {
    return originalResponse.request();
  }

  @Override
  public Optional<HttpResponse<T>> previousResponse() {
    return originalResponse.previousResponse();
  }

  /**
   * Same as {@link #previousResponse()} but with an Option instead of an Optional
   */
  public Option<HttpResponse<T>> previousResponseOption() {
    return Option.ofOptional(previousResponse());
  }

  @Override
  public HttpHeaders headers() {
    return originalResponse.headers();
  }

  @Override
  public T body() {
    return originalResponse.body();
  }

  @Override
  public Optional<SSLSession> sslSession() {
    return originalResponse.sslSession();
  }

  /**
   * Same as {@link #sslSession()} but with an Option instead of an Optional
   */
  public Option<SSLSession> sslSessionOption() {
    return Option.ofOptional(sslSession());
  }

  @Override
  public URI uri() {
    return originalResponse.uri();
  }

  @Override
  public Version version() {
    return originalResponse.version();
  }

  /**
   * Check if the given http response is 1xx or not
   *
   * @return true if status code is 1xx, false otherwise
   */
  public boolean is1xx() {
    return statusCode() >= 100 && statusCode() <= 199;
  }

  /**
   * Check if the given http response is 2xx or not
   *
   * @return true if status code is 2xx, false otherwise
   */
  public boolean is2xx() {
    return statusCode() >= 200 && statusCode() <= 299;
  }

  /**
   * Check if the given http response is 200 OK or not
   *
   * @return true if status code is 200, false otherwise
   */
  public boolean isOk() {
    return statusCode() == 200;
  }

  /**
   * Check if the given http response is 3xx or not
   *
   * @return true if status code is 3xx, false otherwise
   */
  public boolean is3xx() {
    return statusCode() >= 300 && statusCode() <= 399;
  }

  /**
   * Check if the given http response is 4xx or not
   *
   * @return true if status code is 4xx, false otherwise
   */
  public boolean is4xx() {
    return statusCode() >= 400 && statusCode() <= 499;
  }

  /**
   * Check if the given http response is 5xx or not
   *
   * @return true if status code is 5xx, false otherwise
   */
  public boolean is5xx() {
    return statusCode() >= 500 && statusCode() <= 599;
  }

  /**
   * Transform the current EasyHttpResponse to a standard java {@link HttpResponse}
   * @return a standard HttpResponse
   */
  public HttpResponse<T> toJavaResponse() {
    return this;
  }
}
