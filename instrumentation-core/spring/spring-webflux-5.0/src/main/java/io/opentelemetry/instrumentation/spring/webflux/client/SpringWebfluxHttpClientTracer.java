/*
 * Copyright The OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.instrumentation.spring.webflux.client;

import static io.opentelemetry.OpenTelemetry.getPropagators;
import static io.opentelemetry.instrumentation.spring.webflux.client.HttpHeadersInjectAdapter.SETTER;

import io.grpc.Context;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.instrumentation.api.tracer.HttpClientTracer;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.Tracer;
import java.net.URI;
import java.util.List;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;

class SpringWebfluxHttpClientTracer extends HttpClientTracer<ClientRequest, ClientResponse> {

  public static final SpringWebfluxHttpClientTracer TRACER = new SpringWebfluxHttpClientTracer();

  public void onCancel(Span span) {
    span.setAttribute("event", "cancelled");
    span.setAttribute("message", "The subscription was cancelled");
  }

  public void inject(Context context, HttpHeaders httpHeaders) {
    getPropagators().getHttpTextFormat().inject(context, httpHeaders, SETTER);
  }

  @Override
  protected String method(ClientRequest httpRequest) {
    return httpRequest.method().name();
  }

  @Override
  protected URI url(ClientRequest httpRequest) {
    return httpRequest.url();
  }

  @Override
  protected Integer status(ClientResponse httpResponse) {
    return httpResponse.statusCode().value();
  }

  @Override
  protected String requestHeader(ClientRequest clientRequest, String name) {
    return clientRequest.headers().getFirst(name);
  }

  @Override
  protected String responseHeader(ClientResponse clientResponse, String name) {
    List<String> headers = clientResponse.headers().header(name);
    return !headers.isEmpty() ? headers.get(0) : null;
  }

  @Override
  protected Setter<ClientRequest> getSetter() {
    return null;
  }

  @Override
  protected String getInstrumentationName() {
    return "io.opentelemetry.auto.spring-webflux-5.0";
  }

  public Tracer getTracer() {
    return tracer;
  }
}
