package com.github.badpop.easyhttp.control;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.badpop.easyhttp.exception.ReadBodyException;
import com.github.badpop.easyhttp.extension.EasyHttpResponseStringMockExtension;
import com.github.badpop.easyhttp.extension.Value;
import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.http.HttpResponse;

import static org.assertj.vavr.api.VavrAssertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(EasyHttpResponseStringMockExtension.class)
public class EasyHttpResponseReadBodyForStatusTest {

  private static final String JSON_VALUE = "{\"value\":200}";
  private final ObjectMapper om = mock(ObjectMapper.class);

  @Test
  void should_read_body_for_class_for_status(EasyHttpResponse<String> easyHttpResponse, HttpResponse<String> originalResponse) throws JsonProcessingException {
    val status = 200;
    val value = new Value(0);

    when(originalResponse.statusCode()).thenReturn(status);
    when(originalResponse.body()).thenReturn(JSON_VALUE);
    when(easyHttpResponse.usedClient.getObjectMapper()).thenReturn(om);
    when(om.readValue(easyHttpResponse.body(), Value.class)).thenReturn(value);

    val actual = easyHttpResponse.readBodyForStatus(status, Value.class);

    assertThat(actual).isRight();
    assertThat(actual.get()).containsSame(value);
  }

  @Test
  void should_not_read_body_for_class_for_status(EasyHttpResponse<String> easyHttpResponse, HttpResponse<String> originalResponse) {
    val status = 200;
    val responseStatus = 404;

    when(originalResponse.statusCode()).thenReturn(responseStatus);

    val actual = easyHttpResponse.readBodyForStatus(status, Value.class);

    assertThat(actual).isRight();
    assertThat(actual.get()).isEmpty();

    verifyNoInteractions(om);
  }

  @Test
  void should_read_body_for_class_for_status_but_return_left_on_json_exception(
    EasyHttpResponse<String> easyHttpResponse, HttpResponse<String> originalResponse) throws JsonProcessingException {
    val status = 200;
    val jsonException = new JsonMappingException(null, "");

    when(originalResponse.statusCode()).thenReturn(status);
    when(originalResponse.body()).thenReturn(JSON_VALUE);
    when(easyHttpResponse.usedClient.getObjectMapper()).thenReturn(om);
    when(om.readValue(easyHttpResponse.body(), Value.class)).thenThrow(jsonException);

    val actual = easyHttpResponse.readBodyForStatus(status, Value.class);

    assertThat(actual).isLeft().containsLeftInstanceOf(ReadBodyException.class);
    Assertions.assertThat(actual.getLeft().getCause()).isInstanceOf(jsonException.getClass());
  }

  @Test
  void should_read_body_for_type_for_status(EasyHttpResponse<String> easyHttpResponse, HttpResponse<String> originalResponse) throws JsonProcessingException {
    val status = 200;
    val value = new Value(0);
    val typeRef = new TypeReference<Value>() {
    };

    when(originalResponse.statusCode()).thenReturn(status);
    when(originalResponse.body()).thenReturn(JSON_VALUE);
    when(easyHttpResponse.usedClient.getObjectMapper()).thenReturn(om);
    when(om.readValue(easyHttpResponse.body(), typeRef)).thenReturn(value);

    val actual = easyHttpResponse.readBodyForStatus(status, typeRef);

    assertThat(actual).isRight();
    assertThat(actual.get()).containsSame(value);
  }

  @Test
  void should_not_read_body_for_type_for_status(EasyHttpResponse<String> easyHttpResponse, HttpResponse<String> originalResponse) {
    val status = 200;
    val responseStatus = 404;
    val typeRef = new TypeReference<Value>() {
    };

    when(originalResponse.statusCode()).thenReturn(responseStatus);

    val actual = easyHttpResponse.readBodyForStatus(status, typeRef);

    assertThat(actual).isRight();
    assertThat(actual.get()).isEmpty();

    verifyNoInteractions(om);
  }

  @Test
  void should_read_body_for_type_for_status_but_return_left_on_json_exception(
    EasyHttpResponse<String> easyHttpResponse, HttpResponse<String> originalResponse) throws JsonProcessingException {
    val status = 200;
    val typeRef = new TypeReference<Value>() {
    };
    val jsonException = new JsonMappingException(null, "");

    when(originalResponse.statusCode()).thenReturn(status);
    when(originalResponse.body()).thenReturn(JSON_VALUE);
    when(easyHttpResponse.usedClient.getObjectMapper()).thenReturn(om);
    when(om.readValue(easyHttpResponse.body(), typeRef)).thenThrow(jsonException);

    val actual = easyHttpResponse.readBodyForStatus(status, typeRef);

    assertThat(actual).isLeft().containsLeftInstanceOf(ReadBodyException.class);
    Assertions.assertThat(actual.getLeft().getCause()).isInstanceOf(jsonException.getClass());
  }
}
