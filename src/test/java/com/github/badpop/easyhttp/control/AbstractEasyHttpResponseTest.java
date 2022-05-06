package com.github.badpop.easyhttp.control;

import com.github.badpop.easyhttp.EasyHttpClient;
import com.github.badpop.easyhttp.EasyHttpClientProvider;
import com.github.badpop.easyhttp.extension.EasyHttpResponseVoidMockExtension;
import io.vavr.control.Option;
import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.net.ssl.SSLSession;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class AbstractEasyHttpResponseTest {

  private final EasyHttpClient client = EasyHttpClientProvider.newClient();
  private final HttpRequest originalRequest = HttpRequest.newBuilder()
    .GET()
    .uri(URI.create("http://localhost:8080/"))
    .build();
  private final BodyHandler<Void> originalBodyHandler = BodyHandlers.discarding();
  private final HttpResponse<Void> originalResponse = new HttpResponse<>() {
    @Override
    public int statusCode() {
      return 200;
    }

    @Override
    public HttpRequest request() {
      return originalRequest;
    }

    @Override
    public Optional<HttpResponse<Void>> previousResponse() {
      return Optional.empty();
    }

    @Override
    public HttpHeaders headers() {
      return originalRequest.headers();
    }

    @Override
    public Void body() {
      return null;
    }

    @Override
    public Optional<SSLSession> sslSession() {
      return Optional.empty();
    }

    @Override
    public URI uri() {
      return originalRequest.uri();
    }

    @Override
    public HttpClient.Version version() {
      return null;
    }
  };

  private final AbstractEasyHttpResponse<Void> response = new EasyHttpResponse<>(originalResponse, originalBodyHandler, originalRequest, client);

  @Test
  void should_return_HttpResponse_values() {
    assertThat(response.statusCode()).isEqualTo(originalResponse.statusCode());
    assertThat(response.request()).isEqualTo(originalResponse.request());
    assertThat(response.previousResponse()).isEqualTo(originalResponse.previousResponse());
    assertThat(response.previousResponseOption()).isEqualTo(Option.ofOptional(originalResponse.previousResponse()));
    assertThat(response.headers()).isEqualTo(originalResponse.headers());
    assertThat(response.body()).isEqualTo(originalResponse.body());
    assertThat(response.sslSession()).isEqualTo(originalResponse.sslSession());
    assertThat(response.sslSessionOption()).isEqualTo(Option.ofOptional(originalResponse.sslSession()));
    assertThat(response.uri()).isEqualTo(originalResponse.uri());
    assertThat(response.version()).isEqualTo(originalResponse.version());
  }

  @Nested
  @ExtendWith(EasyHttpResponseVoidMockExtension.class)
  class UtilityStatusMethods {

    @ParameterizedTest
    @ValueSource(ints = {100, 199})
    void should_be_1xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is1xx()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {99, 200})
    void should_not_be_1xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is1xx()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 299})
    void should_be_2xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is2xx()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {199, 300})
    void should_not_be_2xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is2xx()).isFalse();
    }

    @Test
    void should_be_ok(EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(200);
      assertThat(response.is2xx()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {199, 201})
    void should_not_be_ok(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.isOk()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {300, 399})
    void should_be_3xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is3xx()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {299, 400})
    void should_not_be_3xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is3xx()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {400, 499})
    void should_be_4xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is4xx()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {399, 500})
    void should_not_be_4xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is4xx()).isFalse();
    }

    @ParameterizedTest
    @ValueSource(ints = {500, 599})
    void should_be_5xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is5xx()).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {499, 600})
    void should_not_be_5xx(int status, EasyHttpResponse<Void> response, HttpResponse<Void> originalResponse) {
      when(originalResponse.statusCode()).thenReturn(status);
      assertThat(response.is5xx()).isFalse();
    }

    @Test
    void should_transform_to_java_http_response(EasyHttpResponse<Void> response) {
      val actual = response.toJavaResponse();
      assertThat(actual).isInstanceOf(HttpResponse.class).isEqualTo(response);
    }
  }
}
