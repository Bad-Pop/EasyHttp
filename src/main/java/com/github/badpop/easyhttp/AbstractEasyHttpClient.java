package com.github.badpop.easyhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.badpop.easyhttp.control.EasyHttpResponse;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import io.vavr.jackson.datatype.VavrModule;
import lombok.NonNull;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.util.concurrent.CompletableFuture;

import static lombok.AccessLevel.PROTECTED;

@Value
@NonFinal
@FieldDefaults(makeFinal = true, level = PROTECTED)
public abstract sealed class AbstractEasyHttpClient permits EasyHttpClient {

  protected ObjectMapper objectMapper;
  protected HttpClient client;

  protected AbstractEasyHttpClient() {
    this.objectMapper = defaultObjectMapper();
    this.client = defaultHttpClient();
  }

  protected AbstractEasyHttpClient(ObjectMapper objectMapper, HttpClient client) {
    this.objectMapper = objectMapper;
    this.client = client;
  }

  protected AbstractEasyHttpClient(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
    this.client = defaultHttpClient();
  }

  protected AbstractEasyHttpClient(HttpClient httpClient) {
    this.objectMapper = defaultObjectMapper();
    this.client = httpClient;
  }

  /**
   * Builds a new instance by copying the current instance and using a new {@link ObjectMapper}
   *
   * @param objectMapper the {@link ObjectMapper} you want to use to build a new instance
   * @return the current instance if the objectMapper is the same as actual or a new instance with the new one
   * @throws NullPointerException is the given object mapper is null
   */
  public abstract AbstractEasyHttpClient withObjectMapper(@NonNull ObjectMapper objectMapper);

  /**
   * Builds a new instance by copying the current instance and using a new {@link HttpClient}
   *
   * @param httpClient the {@link HttpClient} you want to use to build a new instance
   * @return the current instance if the HttpClient is the same as actual or a new instance with the new one
   * @throws NullPointerException is the given http client is null
   */
  public abstract AbstractEasyHttpClient withClient(@NonNull HttpClient httpClient);

  /**
   * Sends a synchronous http request and wraps the processing in a functional {@link Try}.
   * <p>
   * This is a simple method to interact with the {@link HttpClient} of java 11 as if EasyHttp was not there, but with better exception handling
   *
   * @param request             the request to send
   * @param responseBodyHandler a response body handler
   * @throws NullPointerException if one of the parameters is null
   */
  public abstract <T> Try<HttpResponse<T>> send(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler);

  /**
   * Sends an asynchronous http request and wraps the processing in a functional {@link Future}.
   * <p>
   * This is a simple method to interact with the {@link HttpClient} of java 11 as if EasyHttp was not there, but with better exception handling
   *
   * @param request             the request to send
   * @param responseBodyHandler a response body handler
   * @throws NullPointerException if one of the parameters is null
   */
  public abstract <T> Future<HttpResponse<T>> sendAsync(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler);

  /**
   * Sends a synchronous http request, wraps the processing in a functional {@link Try} and returns an {@link EasyHttpResponse}
   *
   * @param request             the request to send
   * @param responseBodyHandler a response body handler
   * @throws NullPointerException if one of the parameters is null
   */
  public abstract <T> Try<EasyHttpResponse<T>> sendEasy(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler);

  /**
   * Sends an asynchronous http request, wraps the processing in a functional {@link Future} and returns an {@link EasyHttpResponse}
   *
   * @param request             the request to send
   * @param responseBodyHandler a response body handler
   * @throws NullPointerException if one of the parameters is null
   */
  public abstract <T> Future<EasyHttpResponse<T>> sendAsyncEasy(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler);

  protected <T> EasyHttpResponse<T> execute(HttpRequest httpRequest, BodyHandler<T> bodyHandler) throws IOException, InterruptedException {
    val jdkResponse = client.send(httpRequest, bodyHandler);
    return wrapResponse(httpRequest, jdkResponse, bodyHandler, (EasyHttpClient) this);
  }

  protected <T> CompletableFuture<HttpResponse<T>> executeAsync(HttpRequest httpRequest, BodyHandler<T> bodyHandler) {
    return client.sendAsync(httpRequest, bodyHandler);
  }

  protected <T> EasyHttpResponse<T> wrapResponse(HttpRequest request, HttpResponse<T> response, BodyHandler<T> bodyHandler, EasyHttpClient usedClient) {
    return new EasyHttpResponse<>(response, bodyHandler, request, usedClient);
  }

  private ObjectMapper defaultObjectMapper() {
    return new ObjectMapper().registerModules(new VavrModule(), new JavaTimeModule());
  }

  private HttpClient defaultHttpClient() {
    return HttpClient.newHttpClient();
  }
}
