package com.github.badpop.easyhttp.extension;

import com.github.badpop.easyhttp.EasyHttpClient;
import com.github.badpop.easyhttp.control.EasyHttpResponse;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;

import static io.vavr.API.*;
import static org.mockito.Mockito.mock;

public class EasyHttpResponseStringMockExtension implements ParameterResolver {

  EasyHttpClient client = mock(EasyHttpClient.class);
  HttpResponse<String> originalResponse = mock(HttpResponse.class);
  BodyHandler<String> bodyHandler = mock(BodyHandler.class);
  HttpRequest request = mock(HttpRequest.class);
  EasyHttpResponse<String> response = new EasyHttpResponse<>(originalResponse, bodyHandler, request, client);

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isSupported(parameterContext);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return Match(parameterContext).of(
      Case($(this::isResponse), response),
      Case($(this::isClient), client),
      Case($(this::isOriginalResponse), originalResponse),
      Case($(this::isBodyHandler), bodyHandler),
      Case($(this::isRequest), request),
      Case($(), (Object) null));
  }

  private boolean isSupported(ParameterContext param) {
    return isResponse(param) || isClient(param) || isOriginalResponse(param) || isBodyHandler(param) || isRequest(param);
  }

  private boolean isResponse(ParameterContext param) {
    return param.getParameter().getType().equals(EasyHttpResponse.class);
  }

  private boolean isClient(ParameterContext param) {
    return param.getParameter().getType().equals(EasyHttpClient.class);
  }

  private boolean isOriginalResponse(ParameterContext param) {
    return param.getParameter().getType().equals(HttpResponse.class);
  }

  private boolean isBodyHandler(ParameterContext param) {
    return param.getParameter().getType().equals(BodyHandler.class);
  }

  private boolean isRequest(ParameterContext param) {
    return param.getParameter().getType().equals(HttpRequest.class);
  }
}
