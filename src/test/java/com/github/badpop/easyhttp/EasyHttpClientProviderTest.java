package com.github.badpop.easyhttp;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.net.http.HttpClient;

import static com.github.badpop.easyhttp.EasyHttpClientProvider.newClient;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

class EasyHttpClientProviderTest {

  @Test
  void should_build_default_client() {
    val actual = newClient();
    assertThat(actual).isNotNull();
    assertThat(actual.getObjectMapper()).isNotNull();
    assertThat(actual.getObjectMapper().getRegisteredModuleIds()).hasSize(2);
    assertThat(actual.getClient()).isNotNull();
  }

  @Test
  void should_build_client_with_custom_objectMapper() {
    val om = new ObjectMapper();
    val actual = newClient(om);

    assertThat(actual).isNotNull();
    assertThat(actual.getObjectMapper()).isEqualTo(om);
    assertThat(actual.getClient()).isNotNull();
  }

  @Test
  void should_throws_NPE_if_null_objectMapper() {
    assertThatNullPointerException().isThrownBy(() -> newClient((ObjectMapper) null));
  }

  @Test
  void should_build_client_with_custom_httpClient() {
    val hc = HttpClient.newHttpClient();
    val actual = newClient(hc);

    assertThat(actual).isNotNull();
    assertThat(actual.getObjectMapper()).isNotNull();
    assertThat(actual.getObjectMapper().getRegisteredModuleIds()).hasSize(2);
    assertThat(actual.getClient()).isEqualTo(hc);
  }

  @Test
  void should_throws_NPE_if_null_httpClient() {
    assertThatNullPointerException().isThrownBy(() -> newClient((HttpClient) null));
  }

  @Test
  void should_build_custom_client() {
    val om = new ObjectMapper();
    val hc = HttpClient.newHttpClient();
    val actual = newClient(om, hc);

    assertThat(actual).isNotNull();
    assertThat(actual.getObjectMapper()).isEqualTo(om);
    assertThat(actual.getClient()).isEqualTo(hc);
  }

  @Test
  void should_not_build_custom_client_on_null() {
    val om = new ObjectMapper();
    val hc = HttpClient.newHttpClient();
    assertThatNullPointerException().isThrownBy(() -> newClient(om, null));
    assertThatNullPointerException().isThrownBy(() -> newClient(null, hc));
  }
}
