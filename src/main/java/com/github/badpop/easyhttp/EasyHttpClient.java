package com.github.badpop.easyhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.badpop.easyhttp.control.EasyHttpResponse;
import io.vavr.concurrent.Future;
import io.vavr.control.Try;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.ToString;
import lombok.Value;
import lombok.extern.slf4j.Slf4j;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import static io.vavr.API.Try;

/**
 * {@link EasyHttpClient} provides useful API to work with http requests and {@link HttpClient}.
 * EasyHttpClient is a class that makes it easier and safer to work with http requests via the http client introduced in java 11.
 * This class also provides a complete API to delegate some tedious tasks to the EasyHttpClient.
 * EasyHttpClient and all the classes provided by this library simply extend the functionalities of the http client introduced in Java 11, making this library completely interoperable with it.
 * <p>
 * Finally, the {@link EasyHttpResponse} class complements this client. It also make it simpler, easier and safer to work with http responses.
 *
 * <ul>
 *   <li>{@link #withObjectMapper(ObjectMapper)}</li>
 *   <li>{@link #withClient(HttpClient)}</li>
 *   <li>{@link #send(HttpRequest, BodyHandler)}</li>
 *   <li>{@link #sendAsync(HttpRequest, BodyHandler)}</li>
 *   <li>{@link #sendEasy(HttpRequest, BodyHandler)}</li>
 *   <li>{@link #sendAsyncEasy(HttpRequest, BodyHandler)}</li>
 *   <li>{@link #createBodyPublisher(Object)} </li>
 * </ul>
 */
@Slf4j
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EasyHttpClient extends AbstractEasyHttpClient {

  EasyHttpClient() {
    super();
  }

  EasyHttpClient(@NonNull ObjectMapper objectMapper) {
    super(objectMapper);
  }

  EasyHttpClient(@NonNull HttpClient client) {
    super(client);
  }

  EasyHttpClient(@NonNull ObjectMapper objectMapper, @NonNull HttpClient client) {
    super(objectMapper, client);
  }

  @Override
  public EasyHttpClient withObjectMapper(@NonNull ObjectMapper objectMapper) {
    return this.objectMapper == objectMapper ? this : new EasyHttpClient(objectMapper, this.client);
  }

  @Override
  public EasyHttpClient withClient(@NonNull HttpClient httpClient) {
    return this.client == httpClient ? this : new EasyHttpClient(this.objectMapper, httpClient);
  }

  @Override
  public <T> Try<HttpResponse<T>> send(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler) {
    return Try(() -> execute(request, responseBodyHandler));
  }

  @Override
  public <T> Future<HttpResponse<T>> sendAsync(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler) {
    return Future.fromCompletableFuture(executeAsync(request, responseBodyHandler))
      .mapTry(response -> wrapResponse(request, response, responseBodyHandler, this));
  }

  @Override
  public <T> Try<EasyHttpResponse<T>> sendEasy(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler) {
    return Try(() -> execute(request, responseBodyHandler));
  }

  @Override
  public <T> Future<EasyHttpResponse<T>> sendAsyncEasy(@NonNull HttpRequest request, @NonNull BodyHandler<T> responseBodyHandler) {
    return Future.fromCompletableFuture(executeAsync(request, responseBodyHandler))
      .map(response -> wrapResponse(request, response, responseBodyHandler, this));
  }

  /**
   * Use this method to wrap a java object in a {@link BodyPublisher} by serializing the object to json
   * <p>
   * Note: if the object passed as an argument is null or not serializable in json, this method returns {@link BodyPublishers#noBody()}.
   * Also, if an error occurs during the serialization you will also get a {@link BodyPublishers#noBody()}.
   *
   * @param body the object you want to serialize to json
   * @return a {@link BodyPublishers#ofString(String)} if the object is not null, serializable and no error occurred while serializing it.
   * Otherwise, it returns a {@link BodyPublishers#noBody()}
   */
  public <U> BodyPublisher createBodyPublisher(U body) {
    if (body == null || !objectMapper.canSerialize(body.getClass())) {
      log.warn("Unable to serialize object it may be null or not serializable, returning a noBody Publisher");
      return BodyPublishers.noBody();
    } else {
      return Try(() -> objectMapper.writeValueAsString(body))
        .map(BodyPublishers::ofString)
        .toEither()
        .peekLeft(throwable -> log.warn("Unable to serialize object into json, returning a noBody publisher", throwable))
        .getOrElse(BodyPublishers::noBody);
    }
  }
}
