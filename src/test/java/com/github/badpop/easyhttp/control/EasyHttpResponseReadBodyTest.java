package com.github.badpop.easyhttp.control;

import com.fasterxml.jackson.core.io.JsonEOFException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.badpop.easyhttp.EasyHttpClientProvider;
import com.github.badpop.easyhttp.exception.ReadBodyException;
import com.github.badpop.easyhttp.extension.Value;
import lombok.SneakyThrows;
import lombok.val;
import org.assertj.vavr.api.VavrAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient.Version;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class EasyHttpResponseReadBodyTest {

  private static final String JSON_VALUE = "{\"value\": 200}";
  private static final String INVALID_JSON_VALUE = "{";

  private static Stream<Arguments> provideValidReadBodyTestCasesParams() throws URISyntaxException {
    File file = Paths.get(
        Objects.requireNonNull(EasyHttpResponseReadBodyTest.class
            .getClassLoader()
            .getResource("valueTest.json"))
          .toURI())
      .toFile();

    var path = Path.of(URI.create("file:///" + file.getAbsolutePath()));

    return Stream.of(
      Arguments.of(buildStringResponse(true), BodyHandlers.ofString(), buildFakeRequest()),
      Arguments.of(buildPathResponse(true), BodyHandlers.ofFile(path), buildFakeRequest()),
      Arguments.of(buildInputStreamResponse(true), BodyHandlers.ofInputStream(), buildFakeRequest()),
      Arguments.of(buildByteArrayResponse(true), BodyHandlers.ofByteArray(), buildFakeRequest()),
      Arguments.of(buildStreamResponse(true), BodyHandlers.ofLines(), buildFakeRequest()),
      Arguments.of(buildStreamMultiLineResponse(true), BodyHandlers.ofLines(), buildFakeRequest()));
  }

  private static Stream<Arguments> provideInvalidReadBodyTestCasesParams() throws URISyntaxException {
    File file = Paths.get(
        Objects.requireNonNull(EasyHttpResponseReadBodyTest.class
            .getClassLoader()
            .getResource("invalidValueTest.json"))
          .toURI())
      .toFile();

    var path = Path.of(URI.create("file:///" + file.getAbsolutePath()));

    return Stream.of(
      Arguments.of(buildStringResponse(false), BodyHandlers.ofString(), buildFakeRequest()),
      Arguments.of(buildEmptyStringResponse(), BodyHandlers.ofString(), buildFakeRequest()),
      Arguments.of(buildPathResponse(false), BodyHandlers.ofFile(path), buildFakeRequest()),
      Arguments.of(buildInputStreamResponse(false), BodyHandlers.ofInputStream(), buildFakeRequest()),
      Arguments.of(buildByteArrayResponse(false), BodyHandlers.ofByteArray(), buildFakeRequest()),
      Arguments.of(buildStreamResponse(false), BodyHandlers.ofLines(), buildFakeRequest()),
      Arguments.of(buildStreamMultiLineResponse(false), BodyHandlers.ofLines(), buildFakeRequest()));
  }

  private static HttpRequest buildFakeRequest() {
    return HttpRequest.newBuilder().GET().uri(URI.create("http://localhost:8080/")).build();
  }

  private static HttpResponse<String> buildStringResponse(boolean provideValid) {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public String body() {
        return provideValid ? JSON_VALUE : INVALID_JSON_VALUE;
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<String> buildEmptyStringResponse() {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<String>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public String body() {
        return "";
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<Path> buildPathResponse(boolean provideValid) {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<Path>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      @SneakyThrows
      public Path body() {
        File file;
        if (provideValid) {
          file = Paths.get(
              Objects.requireNonNull(getClass()
                  .getClassLoader()
                  .getResource("valueTest.json"))
                .toURI())
            .toFile();

        } else {
          file = Paths.get(
              Objects.requireNonNull(getClass()
                  .getClassLoader()
                  .getResource("invalidValueTest.json"))
                .toURI())
            .toFile();

        }
        return Path.of(URI.create("file:///" + file.getAbsolutePath()));

      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<InputStream> buildInputStreamResponse(boolean provideValid) {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<InputStream>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public InputStream body() {
        return provideValid
          ? new ByteArrayInputStream(JSON_VALUE.getBytes(UTF_8))
          : new ByteArrayInputStream(INVALID_JSON_VALUE.getBytes(UTF_8));
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<byte[]> buildByteArrayResponse(boolean provideValid) {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<byte[]>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public byte[] body() {
        return provideValid ? JSON_VALUE.getBytes(UTF_8) : INVALID_JSON_VALUE.getBytes(UTF_8);
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<Stream<String>> buildStreamResponse(boolean provideValid) {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<Stream<String>>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public Stream<String> body() {
        return provideValid ? Stream.of(JSON_VALUE) : Stream.of(INVALID_JSON_VALUE);
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<Stream<String>> buildStreamMultiLineResponse(boolean provideValid) {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<Stream<String>>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public Stream<String> body() {
        return provideValid
          ? Stream.of("{", "\"value\": 200", "}")
          : Stream.of("{");
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<Void> buildVoidResponse() {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<Void>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public Void body() {
        return null;
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<Value> buildUnsupportedBodyTypeResponse() {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<Value>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public Value body() {
        return new Value(0);
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  private static HttpResponse<Stream<Value>> buildInvalidStreamWithNoStrings() {
    return new HttpResponse<>() {
      @Override
      public int statusCode() {
        return 0;
      }

      @Override
      public HttpRequest request() {
        return null;
      }

      @Override
      public Optional<HttpResponse<Stream<Value>>> previousResponse() {
        return Optional.empty();
      }

      @Override
      public HttpHeaders headers() {
        return null;
      }

      @Override
      public Stream<Value> body() {
        return Stream.of(new Value(0), new Value(1));
      }

      @Override
      public Optional<SSLSession> sslSession() {
        return Optional.empty();
      }

      @Override
      public URI uri() {
        return null;
      }

      @Override
      public Version version() {
        return null;
      }
    };
  }

  @ParameterizedTest
  @MethodSource("provideValidReadBodyTestCasesParams")
  <T> void should_read_body(HttpResponse<T> response, BodyHandler<T> bodyHandler, HttpRequest request) {
    // GIVEN
    val usedClient = EasyHttpClientProvider.newClient();
    val easyResponse = new EasyHttpResponse<>(response, bodyHandler, request, usedClient);

    // WHEN
    val actualForClass = easyResponse.readBody(Value.class);
    val actualForTypeRef = easyResponse.readBody(new TypeReference<Value>() {
    });

    // THEN
    VavrAssertions.assertThat(actualForClass).containsRightInstanceOf(Value.class);
    VavrAssertions.assertThat(actualForTypeRef).containsRightInstanceOf(Value.class);
    assertThat(actualForClass.get()).isEqualTo(new Value(200));
    assertThat(actualForTypeRef.get()).isEqualTo(new Value(200));
  }

  @ParameterizedTest
  @MethodSource("provideInvalidReadBodyTestCasesParams")
  <T> void should_not_read_body_on_ObjectMapper_exception(HttpResponse<T> response, BodyHandler<T> bodyHandler, HttpRequest request) {
    // GIVEN
    val usedClient = EasyHttpClientProvider.newClient();
    val easyResponse = new EasyHttpResponse<>(response, bodyHandler, request, usedClient);

    // WHEN
    val actualForClass = easyResponse.readBody(Value.class);
    val actualForTypeRef = easyResponse.readBody(new TypeReference<Value>() {
    });

    // THEN
    VavrAssertions.assertThat(actualForClass).containsLeftInstanceOf(ReadBodyException.class);
    VavrAssertions.assertThat(actualForTypeRef).containsLeftInstanceOf(ReadBodyException.class);

    assertThat(actualForClass.getLeft().getCause())
      .isInstanceOfAny(JsonEOFException.class, MismatchedInputException.class);
    assertThat(actualForTypeRef.getLeft().getCause())
      .isInstanceOfAny(JsonEOFException.class, MismatchedInputException.class);
  }

  @Test
  void should_not_throws_NPE_if_body_is_null() {
    // GIVEN
    val response = buildVoidResponse();
    val bodyHandler = BodyHandlers.discarding();
    val request = buildFakeRequest();
    val usedClient = EasyHttpClientProvider.newClient();

    // WHEN
    val easyResponse = new EasyHttpResponse<>(response, bodyHandler, request, usedClient);

    // THEN
    val actualForClass = easyResponse.readBody(Value.class);
    val actualForTypeRef = easyResponse.readBody(new TypeReference<Value>() {
    });

    VavrAssertions.assertThat(actualForClass).containsLeftInstanceOf(ReadBodyException.class);
    VavrAssertions.assertThat(actualForTypeRef).containsLeftInstanceOf(ReadBodyException.class);
  }

  @Test
  void should_not_read_body_on_unsupported_body_type() {
    // GIVEN
    val response = buildUnsupportedBodyTypeResponse();
    BodyHandler<Value> bodyHandler = mock(BodyHandler.class);
    val request = buildFakeRequest();
    val usedClient = EasyHttpClientProvider.newClient();

    val easyResponse = new EasyHttpResponse<>(response, bodyHandler, request, usedClient);

    // WHEN
    val actualForClass = easyResponse.readBody(Value.class);
    val actualForTypeRef = easyResponse.readBody(new TypeReference<Value>() {
    });

    // THEN
    VavrAssertions.assertThat(actualForClass).containsLeftInstanceOf(ReadBodyException.class);
    VavrAssertions.assertThat(actualForTypeRef).containsLeftInstanceOf(ReadBodyException.class);
    val expectedReadBodyException = new ReadBodyException("Operation not supported : unknown body type, unable to read it", response.body());
    assertThat(actualForClass.getLeft()).usingRecursiveComparison().isEqualTo(expectedReadBodyException);
    assertThat(actualForTypeRef.getLeft()).usingRecursiveComparison().isEqualTo(expectedReadBodyException);
  }

  @Test
  void should_not_read_body_if_stream_does_not_contains_strings() {
    // GIVEN
    val response = buildInvalidStreamWithNoStrings();
    BodyHandler<Stream<Value>> bodyHandler = mock(BodyHandler.class);
    val request = buildFakeRequest();
    val usedClient = EasyHttpClientProvider.newClient();

    val easyResponse = new EasyHttpResponse<>(response, bodyHandler, request, usedClient);

    // WHEN
    val actualForClass = easyResponse.readBody(Value.class);
    val actualForTypeRef = easyResponse.readBody(new TypeReference<Value>() {
    });

    // THEN
    VavrAssertions.assertThat(actualForClass).containsLeftInstanceOf(ReadBodyException.class);
    VavrAssertions.assertThat(actualForTypeRef).containsLeftInstanceOf(ReadBodyException.class);
    assertThat(actualForClass.getLeft().getCause()).isInstanceOf(ClassCastException.class);
    assertThat(actualForTypeRef.getLeft().getCause()).isInstanceOf(ClassCastException.class);
  }
}
