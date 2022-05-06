# EasyHttp

![GitHub](https://img.shields.io/github/license/Bad-Pop/EasyHttp?style=flat-square)
![GitHub Workflow Status (branch)](https://img.shields.io/github/workflow/status/Bad-Pop/EasyHttp/EasyHttp%20Build%20Action/main?style=flat-square)
![Sonar Coverage](https://img.shields.io/sonar/coverage/Bad-Pop_EasyHttp/main?server=https%3A%2F%2Fsonarcloud.io&style=flat-square)
![Sonar Tests](https://img.shields.io/sonar/tests/Bad-Pop_EasyHttp/main?compact_message&server=https%3A%2F%2Fsonarcloud.io&style=flat-square)


## What is EasyHttp ?

EasyHttp is a library providing a java http client designed to interact in a faster, more secure and simpler way with
the http client introduced by Java 11. EasyHttp is fully interoperable with the java http client as it is completely
based on it.

With EasyHttp, you can delegate some repetitive tasks such as JSON serialization and deserialization, exception
handling, but also interaction with http responses thanks to an extended and more complete API.

Finally, EasyHttp relies on the Vavr library in order to provide its users with a much more concise and powerful
programming style and more maintainable thanks to functional programming. And all this in Java 17 and of course fully
tested.

## How to use EasyHttp ? /!\ Not yet pushed into the maven central

**Maven :**

```xml

<dependency>
    <groupId>com.github.bad-pop</groupId>
    <artifactId>easy-http</artifactId>
    <version>x.x.x</version>
</dependency>
```

**Gradle :**

```yml
implementation 'com.github.bad-pop:easy-http:x.x.x'
```

**Example of usage :**

```java
var client = EasyHttpClientProvider.newClient();

var body = new Foo("bar");
var request = HttpRequest.newBuilder()
    .POST(client.createBodyPublisher(body))
    .uri(URI.create("https://domain.com/api/resources"))
    .build();

var reponse = client.sendEasy(request, BodyHandlers.ofString());
var responseBody = response.get().readBody(Bar.class);
```
