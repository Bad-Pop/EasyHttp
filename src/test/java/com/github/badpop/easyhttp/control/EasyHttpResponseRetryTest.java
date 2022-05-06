package com.github.badpop.easyhttp.control;

import com.github.badpop.easyhttp.EasyHttpClient;
import com.github.badpop.easyhttp.extension.EasyHttpResponseVoidMockExtension;
import io.vavr.concurrent.Future;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandler;

import static io.vavr.API.*;
import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(EasyHttpResponseVoidMockExtension.class)
public class EasyHttpResponseRetryTest {

  @Test
  void should_retry(
    EasyHttpResponse<Void> easyHttpResponse, BodyHandler<Void> originalBodyHandler, HttpRequest originalRequest, EasyHttpClient usedClient) {

    val secondCallException = new RuntimeException();
    when(usedClient.sendEasy(originalRequest, originalBodyHandler))
      .thenReturn(Success(easyHttpResponse))
      .thenReturn(Failure(secondCallException));

    val actualSuccess = easyHttpResponse.retry();
    val actualFailure = easyHttpResponse.retry();

    assertThat(actualSuccess).containsSame(easyHttpResponse);
    assertThat(actualFailure).failBecauseOf(RuntimeException.class);
    verify(usedClient, times(2)).sendEasy(originalRequest, originalBodyHandler);
    verifyNoMoreInteractions(usedClient);
  }

  @Test
  void should_retry_async(
    EasyHttpResponse<Void> easyHttpResponse, BodyHandler<Void> originalBodyHandler, HttpRequest originalRequest, EasyHttpClient usedClient) {

    val secondCallException = new RuntimeException();
    when(usedClient.sendAsyncEasy(originalRequest, originalBodyHandler))
      .thenReturn(Future(easyHttpResponse))
      .thenReturn(Future.failed(secondCallException));

    val actualSuccess = easyHttpResponse.retryAsync();
    val actualFailure = easyHttpResponse.retryAsync();

    Assertions.assertThat(actualSuccess.isSuccess()).isTrue();
    Assertions.assertThat(actualSuccess.get()).isEqualTo(easyHttpResponse);
    Assertions.assertThat(actualFailure.isSuccess()).isFalse();
    assertThat(actualFailure.getCause()).containsSame(secondCallException);
  }
}
