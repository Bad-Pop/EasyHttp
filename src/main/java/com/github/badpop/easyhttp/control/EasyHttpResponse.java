package com.github.badpop.easyhttp.control;

import com.fasterxml.jackson.core.type.TypeReference;
import com.github.badpop.easyhttp.AbstractEasyHttpClient;
import com.github.badpop.easyhttp.exception.ReadBodyException;
import io.vavr.concurrent.Future;
import io.vavr.control.Either;
import io.vavr.control.Option;
import io.vavr.control.Try;
import lombok.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.vavr.API.*;

/**
 * This class represents an HTTP response. It implements the {@link HttpResponse} interface introduced in Java 11, which makes it fully interoperable.
 * On top of that, this class provides a complete API to work with HTTP responses in a faster, safer and simpler way than with a native {@link HttpResponse}.
 *
 * <ul>
 *   <li>{@link #statusCode()}</li>
 *   <li>{@link #request()}</li>
 *   <li>{@link #previousResponse()}</li>
 *   <li>{@link #previousResponseOption()}</li>
 *   <li>{@link #headers()}</li>
 *   <li>{@link #body()}</li>
 *   <li>{@link #sslSession()}</li>
 *   <li>{@link #sslSessionOption()}</li>
 *   <li>{@link #uri()}</li>
 *   <li>{@link #version()}</li>
 *   <li>{@link #is1xx()}</li>
 *   <li>{@link #is2xx()}</li>
 *   <li>{@link #isOk()}</li>
 *   <li>{@link #is3xx()}</li>
 *   <li>{@link #is4xx()}</li>
 *   <li>{@link #is5xx()}</li>
 *   <li>{@link #toJavaResponse()}</li>
 *   <li>{@link #readBody(Class)}</li>
 *   <li>{@link #readBody(TypeReference)}</li>
 *   <li>{@link #readBodyForStatus(int, Class)}</li>
 *   <li>{@link #readBodyForStatus(int, TypeReference)}</li>
 *   <li>{@link #retry()}</li>
 *   <li>{@link #retryAsync()}</li>
 *   <li>{@link #onOk(Runnable)}</li>
 *   <li>{@link #on2xx(Runnable)}</li>
 *   <li>{@link #onKo(Runnable)}</li>
 * </ul>
 */
@Value
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class EasyHttpResponse<T> extends AbstractEasyHttpResponse<T> {

  public EasyHttpResponse(
    @NonNull HttpResponse<T> originalResponse,
    @NonNull BodyHandler<T> originalBodyHandler,
    @NonNull HttpRequest originalRequest,
    @NonNull AbstractEasyHttpClient usedClient) {
    super(originalResponse, originalBodyHandler, originalRequest, usedClient);
  }

  /**
   * This method will attempt to deserialize the body of the current request into an instance of the requested {@link Class}.
   *
   * @param targetClass The class whose instance you want to build from the body of the response
   * @return An {@link Either.Right} if the deserialization is successful.
   * Otherwise, returns an {@link Either.Left} containing a {@link ReadBodyException} containing the cause of the failure.
   * @throws NullPointerException if the given class is null
   */
  public <U> Either<ReadBodyException, U> readBody(@NonNull final Class<U> targetClass) {
    return Try(() -> readBodyForClass(targetClass))
      .toEither()
      .mapLeft(throwable -> new ReadBodyException("An error occurred while trying to read response body", body(), throwable));
  }

  /**
   * This method will attempt to deserialize the body of the current request into an instance of a class
   * referenced by a {@link TypeReference}.
   *
   * @param targetTypeReference a Jackson {@link TypeReference} defining which type of object you want to deserialize
   * @return An {@link Either.Right} if the deserialization is successful.
   * * Otherwise, returns an {@link Either.Left} containing a {@link ReadBodyException} containing the cause of the failure.
   * @throws NullPointerException if the given TypeReference is null
   */
  public <U> Either<ReadBodyException, U> readBody(@NonNull final TypeReference<U> targetTypeReference) {
    return Try(() -> readBodyForTypeReference(targetTypeReference))
      .toEither()
      .mapLeft(throwable -> new ReadBodyException("An error occurred while trying to read response body", body(), throwable));
  }

  /**
   * Identical to the {@link #readBody(Class)} method but
   * only attempts to deserialize the response body if and only if the status of the response is equal to the status provided in the parameter.
   *
   * @param statusCode  The status for which you wish to deserialize the response body
   * @param targetClass The class whose instance you want to build from the body of the response
   * @return a new {@link Either} wrapping exception that may occur while deserializing the response body or an {@link Option}.
   * The Option will be empty if the given status code is not equal with the response status code.
   */
  public <U> Either<ReadBodyException, Option<U>> readBodyForStatus(int statusCode, Class<U> targetClass) {
    if (statusCode == statusCode()) {
      return readBody(targetClass).map(Option::of);
    }
    return Right(None());
  }

  /**
   * Identical to the {@link #readBody(TypeReference)} method but
   * only attempts to deserialize the response body if and only if the status of the response is equal to the status provided in the parameter.
   *
   * @param statusCode          The status for which you wish to deserialize the response body
   * @param targetTypeReference A Jackson {@link TypeReference} defining which type of object you want to deserialize
   * @return a new {@link Either} wrapping exception that may occur while deserializing the response body or an {@link Option}.
   * The Option will be empty if the given status code is not equal with the response status code.
   */
  public <U> Either<ReadBodyException, Option<U>> readBodyForStatus(int statusCode, TypeReference<U> targetTypeReference) {
    if (statusCode == statusCode()) {
      return readBody(targetTypeReference).map(Option::of);
    }
    return Right(None());
  }

  private <U> U readBodyForClass(Class<U> clazz) throws IOException {
    if (body() == null) {
      throw new IllegalArgumentException("The response body is null");
    }

    if (body() instanceof String sBody) {
      return usedClient.getObjectMapper().readValue(sBody, clazz);
    } else if (body() instanceof Path pathBody) {
      val file = pathBody.toFile();
      return usedClient.getObjectMapper().readValue(file, clazz);
    } else if (body() instanceof InputStream isBody) {
      return usedClient.getObjectMapper().readValue(isBody, clazz);
    } else if (body() instanceof byte[] baBody) {
      return usedClient.getObjectMapper().readValue(baBody, clazz);
    } else if (body() instanceof Stream streamBody) {
      return usedClient.getObjectMapper().readValue(
        ((Stream<String>) streamBody).collect(Collectors.joining()), clazz);
    }

    throw new UnsupportedOperationException("Operation not supported : unknown body type, unable to read it");
  }

  private <U> U readBodyForTypeReference(TypeReference<U> tr) throws IOException {
    if (body() == null) {
      throw new IllegalArgumentException("The response body is null");
    }

    if (body() instanceof String sBody) {
      return usedClient.getObjectMapper().readValue(sBody, tr);
    } else if (body() instanceof Path pathBody) {
      val file = pathBody.toFile();
      return usedClient.getObjectMapper().readValue(file, tr);
    } else if (body() instanceof InputStream isBody) {
      return usedClient.getObjectMapper().readValue(isBody, tr);
    } else if (body() instanceof byte[] baBody) {
      return usedClient.getObjectMapper().readValue(baBody, tr);
    } else if (body() instanceof Stream streamBody) {
      return usedClient.getObjectMapper().readValue(
        ((Stream<String>) streamBody).collect(Collectors.joining()), tr);
    }

    throw new UnsupportedOperationException("Operation not supported : unknown body type, unable to read it");
  }

  /**
   * Try to replay synchronously the request that was sent and returned this response
   *
   * @return A new EasyHttpResponse with the same body type
   */
  public Try<EasyHttpResponse<T>> retry() {
    return usedClient.sendEasy(originalRequest, originalBodyHandler);
  }

  /**
   * Try to replay asynchronously the request that was sent and returned this response
   *
   * @return A new EasyHttpResponse with the same body type
   */
  public Future<EasyHttpResponse<T>> retryAsync() {
    return usedClient.sendAsyncEasy(originalRequest, originalBodyHandler);
  }

  /**
   * Execute an action if the http response status code is 200
   *
   * @param action the action to execute
   * @return the current EasyHttpResponse
   * @throws NullPointerException is the given {@link Runnable} is null
   */
  public EasyHttpResponse<T> onOk(@NonNull Runnable action) {
    return run(action, isOk());
  }

  /**
   * Execute an action if the http response status code is 2xx
   *
   * @param action the action to execute
   * @return the current EasyHttpResponse
   * @throws NullPointerException is the given {@link Runnable} is null
   */
  public EasyHttpResponse<T> on2xx(@NonNull Runnable action) {
    return run(action, is2xx());
  }

  /**
   * Execute an action if the http response status code is not 2xx
   *
   * @param action the action to execute
   * @return the current EasyHttpResponse
   * @throws NullPointerException is the given {@link Runnable} is null
   */
  public EasyHttpResponse<T> onKo(@NonNull Runnable action) {
    return run(action, !is2xx());
  }

  private EasyHttpResponse<T> run(@NonNull Runnable action, boolean shouldExec) {
    if (shouldExec) {
      action.run();
    }
    return this;
  }
}
