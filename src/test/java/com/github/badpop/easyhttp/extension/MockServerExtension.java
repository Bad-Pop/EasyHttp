package com.github.badpop.easyhttp.extension;

import org.junit.jupiter.api.extension.*;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.socket.PortFactory;

import static io.vavr.API.*;

public class MockServerExtension implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, ParameterResolver {

  private static final String HOST = "http://localhost";
  private static Integer port;
  private static ClientAndServer mockServer;

  @Override
  public void afterAll(ExtensionContext context) {
    if (mockServer.isRunning()) {
      mockServer.close();
    }
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    if (port == null && mockServer == null) {
      port = PortFactory.findFreePort();
      mockServer = ClientAndServer.startClientAndServer(port);
    }
  }

  @Override
  public void beforeEach(ExtensionContext context) {
    if (mockServer.isRunning()) {
      mockServer.reset();
    } else {
      mockServer = ClientAndServer.startClientAndServer(port);
    }
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return isSupported(parameterContext);
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return Match(parameterContext).of(
      Case($(this::isPort), port),
      Case($(this::isMockServer), mockServer),
      Case($(this::isHost), HOST),
      Case($(), (Object) null));
  }

  private boolean isSupported(ParameterContext param) {
    return isPort(param) || isMockServer(param) || isHost(param);
  }

  private boolean isPort(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(Integer.class);
  }

  private boolean isMockServer(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(ClientAndServer.class);
  }

  private boolean isHost(ParameterContext parameterContext) {
    return parameterContext.getParameter().getType().equals(String.class);

  }
}
