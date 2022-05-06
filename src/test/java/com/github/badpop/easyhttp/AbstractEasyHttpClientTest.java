package com.github.badpop.easyhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.badpop.easyhttp.control.EasyHttpResponse;
import com.github.badpop.easyhttp.extension.MockServerExtension;
import io.vavr.Function0;
import io.vavr.concurrent.Future;
import lombok.val;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpError;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

import static java.lang.Boolean.TRUE;
import static org.assertj.core.api.Assertions.*;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;

class AbstractEasyHttpClientTest {

  @Nested
  class Clone {
    @Test
    void should_clone_with_new_objectMapper() {
      val om = new ObjectMapper();
      val hc = HttpClient.newHttpClient();
      val client = new EasyHttpClient(om, hc);
      val newOm = new ObjectMapper();

      val actual = client.withObjectMapper(newOm);
      assertThat(actual).isNotEqualTo(client);
      assertThat(actual.getObjectMapper()).isNotEqualTo(om).isEqualTo(newOm);
      assertThat(actual.getClient()).isEqualTo(hc);
    }

    @Test
    void should_not_clone_with_same_objectMapper() {
      val om = new ObjectMapper();
      val hc = HttpClient.newHttpClient();
      val client = new EasyHttpClient(om, hc);

      val actual = client.withObjectMapper(om);
      assertThat(actual).isEqualTo(client);
    }

    @Test
    void should_not_clone_with_null_objectMapper() {
      assertThatNullPointerException().isThrownBy(() -> new EasyHttpClient().withObjectMapper(null));
    }

    @Test
    void should_clone_with_new_httpClient() {
      val om = new ObjectMapper();
      val hc = HttpClient.newHttpClient();
      val client = new EasyHttpClient(om, hc);
      val newHc = HttpClient.newHttpClient();

      val actual = client.withClient(newHc);
      assertThat(actual).isNotEqualTo(client);
      assertThat(actual.getClient()).isNotEqualTo(hc).isEqualTo(newHc);
      assertThat(actual.getObjectMapper()).isEqualTo(om);
    }

    @Test
    void should_not_clone_with_same_httpClient() {
      val om = new ObjectMapper();
      val hc = HttpClient.newHttpClient();
      val client = new EasyHttpClient(om, hc);

      val actual = client.withClient(hc);
      assertThat(actual).isEqualTo(client);
    }

    @Test
    void should_not_clone_with_null_httpClient() {
      assertThatNullPointerException().isThrownBy(() -> new EasyHttpClient().withClient(null));
    }
  }

  @Nested
  @ExtendWith(MockServerExtension.class)
  class WrapAndExecute {

    private final AbstractEasyHttpClient client = EasyHttpClientProvider.newClient();

    @Test
    void should_execute(String host, Integer port, ClientAndServer mockServer) {
      val path = "/path";
      val request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(String.format("%s:%s%s", host, port, path)))
        .build();
      val bodyHandler = BodyHandlers.discarding();

      mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(
          response().withStatusCode(200));

      final Function0<HttpResponse<Void>> execution = () -> {
        try {
          return client.execute(request, bodyHandler);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      };

      assertThatNoException().isThrownBy(execution::apply);

      val actual = execution.apply();
      assertThat(actual.statusCode()).isEqualTo(200);
      assertThat(actual.body()).isNull();
      mockServer.verify(request().withMethod("GET").withPath(path), exactly(2));
    }

    @Test
    void should_throws_IOException_on_execute(String host, Integer port, ClientAndServer mockServer) {
      val path = "/path";
      val request = HttpRequest.newBuilder()
        .uri(URI.create(String.format("%s:%s%s", host, port, path)))
        .build();
      val bodyHandler = BodyHandlers.discarding();

      mockServer
        .when(request().withMethod("GET").withPath(path))
        .error(HttpError.error().withDropConnection(TRUE));

      assertThatExceptionOfType(IOException.class).isThrownBy(() -> client.execute(request, bodyHandler));
      mockServer.verify(request().withMethod("GET").withPath(path));
    }

    @Test
    void should_execute_async(String host, Integer port, ClientAndServer mockServer) {
      val path = "/path";
      val request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(String.format("%s:%s%s", host, port, path)))
        .build();
      val bodyHandler = BodyHandlers.discarding();

      mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(
          response().withStatusCode(200));

      final Function0<CompletableFuture<HttpResponse<Void>>> execution = () -> client.executeAsync(request, bodyHandler);

      assertThatNoException().isThrownBy(execution::apply);
      val actual = Future.fromCompletableFuture(execution.apply()).await();
      assertThat(actual.isCompleted()).isTrue();
      assertThat(actual.isSuccess()).isTrue();
      assertThat(actual.get().statusCode()).isEqualTo(200);
      assertThat(actual.get().body()).isNull();
      mockServer.verify(request().withMethod("GET").withPath(path), exactly(2));
    }

    @Test
    void should_fail_on_execute_async(String host, Integer port, ClientAndServer mockServer) {
      val path = "/path";
      val request = HttpRequest.newBuilder()
        .uri(URI.create(String.format("%s:%s%s", host, port, path)))
        .build();
      val bodyHandler = BodyHandlers.discarding();

      mockServer
        .when(request().withMethod("GET").withPath(path))
        .error(HttpError.error().withDropConnection(TRUE));

      val actual = Future.fromCompletableFuture(client.executeAsync(request, bodyHandler)).await();
      assertThat(actual.isCompleted()).isTrue();
      assertThat(actual.isFailure()).isTrue();
      VavrAssertions.assertThat(actual.getCause()).containsInstanceOf(CompletionException.class);
      VavrAssertions.assertThat(actual.getCause().map(Throwable::getCause)).containsInstanceOf(IOException.class);
      mockServer.verify(request().withMethod("GET").withPath(path));
    }

    @Test
    void should_wrap_response(String host, Integer port, ClientAndServer mockServer) throws IOException, InterruptedException {
      val path = "/path";
      val request = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(String.format("%s:%s%s", host, port, path)))
        .build();
      val bodyHandler = BodyHandlers.discarding();

      mockServer
        .when(request().withMethod("GET").withPath(path))
        .respond(
          response().withStatusCode(200));

      val response = client.execute(request, bodyHandler);
      val actual = client.wrapResponse(request, response, bodyHandler, (EasyHttpClient) client);

      assertThat(actual)
        .usingRecursiveComparison()
        .isEqualTo(
          new EasyHttpResponse<>(response, bodyHandler, request, client));
      mockServer.verify(request().withMethod("GET").withPath(path));
    }
  }
}
