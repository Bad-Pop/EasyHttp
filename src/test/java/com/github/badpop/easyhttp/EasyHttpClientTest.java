package com.github.badpop.easyhttp;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.badpop.easyhttp.control.EasyHttpResponse;
import com.github.badpop.easyhttp.extension.MockServerExtension;
import com.github.badpop.easyhttp.extension.Value;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpError;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.concurrent.CompletionException;

import static java.lang.Boolean.TRUE;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;
import static org.mockserver.verify.VerificationTimes.exactly;

@ExtendWith(MockServerExtension.class)
class EasyHttpClientTest {

  private final EasyHttpClient client = EasyHttpClientProvider.newClient();

  @Test
  void should_send_request_and_be_a_success(String host, Integer port, ClientAndServer mockServer) {
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

    val actualSend = client.send(request, bodyHandler);
    val actualSendEasy = client.sendEasy(request, bodyHandler);

    assertThat(actualSend).isSuccess().containsInstanceOf(HttpResponse.class);
    assertThat(actualSendEasy).isSuccess().containsInstanceOf(EasyHttpResponse.class);
    Assertions.assertThat(actualSend.get().statusCode()).isEqualTo(200);
    Assertions.assertThat(actualSendEasy.get().statusCode()).isEqualTo(200);

    mockServer.verify(request().withMethod("GET").withPath(path), exactly(2));
  }

  @Test
  void should_send_request_and_be_a_failure(String host, Integer port, ClientAndServer mockServer) {
    val path = "/path";
    val request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create(String.format("%s:%s%s", host, port, path)))
      .build();
    val bodyHandler = BodyHandlers.discarding();

    mockServer
      .when(request().withMethod("GET").withPath(path))
      .error(HttpError.error().withDropConnection(TRUE));

    val actualSend = client.send(request, bodyHandler);
    val actualSendEasy = client.sendEasy(request, bodyHandler);

    assertThat(actualSend).failBecauseOf(IOException.class);
    assertThat(actualSendEasy).failBecauseOf(IOException.class);

    mockServer.verify(request().withMethod("GET").withPath(path));
  }

  @Test
  void should_send_async_request_and_be_a_success(String host, Integer port, ClientAndServer mockServer) {
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

    val actualSend = client.sendAsync(request, bodyHandler).await();
    val actualSendEasy = client.sendAsyncEasy(request, bodyHandler).await();

    Assertions.assertThat(actualSend.isSuccess()).isTrue();
    Assertions.assertThat(actualSendEasy.isSuccess()).isTrue();
    Assertions.assertThat(actualSend.get()).isInstanceOf(HttpResponse.class);
    Assertions.assertThat(actualSendEasy.get()).isInstanceOf(EasyHttpResponse.class);
    Assertions.assertThat(actualSend.get().statusCode()).isEqualTo(200);
    Assertions.assertThat(actualSendEasy.get().statusCode()).isEqualTo(200);

    mockServer.verify(request().withMethod("GET").withPath(path), exactly(2));
  }

  @Test
  void should_send_async_request_and_be_a_failure(String host, Integer port, ClientAndServer mockServer) {
    val path = "/path";
    val request = HttpRequest.newBuilder()
      .GET()
      .uri(URI.create(String.format("%s:%s%s", host, port, path)))
      .build();
    val bodyHandler = BodyHandlers.discarding();

    mockServer
      .when(request().withMethod("GET").withPath(path))
      .error(HttpError.error().withDropConnection(TRUE));

    val actualSend = client.sendAsync(request, bodyHandler).await();
    val actualSendEasy = client.sendAsyncEasy(request, bodyHandler).await();

    Assertions.assertThat(actualSend.isFailure()).isTrue();
    Assertions.assertThat(actualSendEasy.isFailure()).isTrue();
    assertThat(actualSend.getCause()).containsInstanceOf(CompletionException.class);
    assertThat(actualSendEasy.getCause()).containsInstanceOf(CompletionException.class);

    Assertions.assertThat(actualSend.getCause().get().getCause()).isInstanceOf(IOException.class);
    Assertions.assertThat(actualSendEasy.getCause().get().getCause()).isInstanceOf(IOException.class);

    mockServer.verify(request().withMethod("GET").withPath(path));
  }

  @Test
  void should_provide_string_publisher() throws JsonProcessingException {
    val value = new Value(0);
    val actual = client.createBodyPublisher(value);

    val expectedJson = client.objectMapper.writeValueAsString(value);
    Assertions.assertThat(actual.contentLength()).isEqualTo(expectedJson.length());
  }
}
