package com.github.badpop.easyhttp.control;

import com.github.badpop.easyhttp.EasyHttpClient;
import lombok.Value;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EasyHttpResponseRunnerTest {

  private final MockRunner runner = mock(MockRunner.class);
  @InjectMocks
  private EasyHttpResponse<Void> easyResponse;
  @Mock
  private HttpResponse<Void> originalResponse;
  @Mock
  private BodyHandler<Void> originalBodyHandler;
  @Mock
  private HttpRequest originalRequest;
  @Mock
  private EasyHttpClient usedClient;

  @Test
  void should_execute_runnable_if_ok() {
    Runnable runnable = runner::run;

    doNothing().when(runner).run();
    when(originalResponse.statusCode()).thenReturn(200);

    assertThatNoException().isThrownBy(() -> easyResponse.onOk(runnable));
    verify(originalResponse).statusCode();
    verify(runner).run();
    verifyNoMoreInteractions(originalResponse, runner);
  }

  @ParameterizedTest
  @ValueSource(ints = {199, 201})
  void should_not_execute_runnable_if_not_ok(int status) {
    Runnable runnable = runner::run;

    doNothing().when(runner).run();
    when(originalResponse.statusCode()).thenReturn(status);

    assertThatNoException().isThrownBy(() -> easyResponse.onOk(runnable));
    verify(originalResponse).statusCode();
    verifyNoInteractions(runner);
    verifyNoMoreInteractions(originalResponse);
  }

  @ParameterizedTest
  @ValueSource(ints = {200, 299})
  void should_execute_runnable_if_2xx(int status) {
    Runnable runnable = runner::run;

    doNothing().when(runner).run();
    when(originalResponse.statusCode()).thenReturn(status);

    assertThatNoException().isThrownBy(() -> easyResponse.on2xx(runnable));
    verify(originalResponse, times(2)).statusCode();
    verify(runner).run();
    verifyNoMoreInteractions(originalResponse, runner);
  }

  @ParameterizedTest
  @ValueSource(ints = {199, 300})
  void should_not_execute_runnable_if_not_2xx(int status) {
    Runnable runnable = runner::run;

    doNothing().when(runner).run();
    when(originalResponse.statusCode()).thenReturn(status);

    assertThatNoException().isThrownBy(() -> easyResponse.on2xx(runnable));
    verifyNoInteractions(runner);
    verifyNoMoreInteractions(originalResponse);
  }

  @ParameterizedTest
  @ValueSource(ints = {199, 300})
  void should_execute_runnable_if_ko(int status) {
    Runnable runnable = runner::run;

    doNothing().when(runner).run();
    when(originalResponse.statusCode()).thenReturn(status);

    assertThatNoException().isThrownBy(() -> easyResponse.onKo(runnable));
    verify(runner).run();
    verifyNoMoreInteractions(originalResponse, runner);
  }

  @ParameterizedTest
  @ValueSource(ints = {200, 299})
  void should_not_execute_runnable_if_not_ko(int status) {
    Runnable runnable = runner::run;

    doNothing().when(runner).run();
    when(originalResponse.statusCode()).thenReturn(status);

    assertThatNoException().isThrownBy(() -> easyResponse.onKo(runnable));
    verify(originalResponse, times(2)).statusCode();
    verifyNoInteractions(runner);
    verifyNoMoreInteractions(originalResponse);
  }

  @Value
  public static class MockRunner {
    void run() {
      // DO NOTHING
    }
  }
}
