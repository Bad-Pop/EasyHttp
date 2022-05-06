package com.github.badpop.easyhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;

import java.net.http.HttpClient;

/**
 * Interface to be used to create new {@link EasyHttpClient} instances.
 *
 * <ul>
 *   <li>{@link #newClient()}</li>
 *   <li>{@link #newClient(ObjectMapper)}</li>
 *   <li>{@link #newClient(HttpClient)}</li>
 *   <li>{@link #newClient(ObjectMapper, HttpClient)}</li>
 * </ul>
 */
public interface EasyHttpClientProvider {

  /**
   * Builds a new {@link EasyHttpClient} with a default {@link ObjectMapper} and {@link HttpClient}
   */
  static EasyHttpClient newClient() {
    return new EasyHttpClient();
  }

  /**
   * Builds a new {@link EasyHttpClient} with a custom {@link ObjectMapper} and a default {@link HttpClient}
   *
   * @param objectMapper the objectMapper that will be used by the EasyHttp client
   * @throws NullPointerException if the given param is null
   */
  static EasyHttpClient newClient(@NonNull ObjectMapper objectMapper) {
    return new EasyHttpClient(objectMapper);
  }

  /**
   * Builds a new {@link EasyHttpClient} with a default {@link ObjectMapper} and a custom {@link HttpClient}
   *
   * @param httpClient the httpClient that will be used by the EasyHttp client
   * @throws NullPointerException if the given param is null
   */
  static EasyHttpClient newClient(@NonNull HttpClient httpClient) {
    return new EasyHttpClient(httpClient);
  }

  /**
   * Builds a new {@link EasyHttpClient} with a custom {@link ObjectMapper} and {@link HttpClient}
   *
   * @param objectMapper the objectMapper that will be used by the EasyHttp client
   * @param httpClient   the httpClient that will be used by the EasyHttp client
   * @throws NullPointerException if a param is null
   */
  static EasyHttpClient newClient(@NonNull ObjectMapper objectMapper, @NonNull HttpClient httpClient) {
    return new EasyHttpClient(objectMapper, httpClient);
  }
}
