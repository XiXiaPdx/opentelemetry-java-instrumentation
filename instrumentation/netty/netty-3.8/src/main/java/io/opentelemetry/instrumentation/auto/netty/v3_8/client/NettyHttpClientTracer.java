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

package io.opentelemetry.instrumentation.auto.netty.v3_8.client;

import static io.opentelemetry.context.ContextUtils.withScopedContext;
import static io.opentelemetry.instrumentation.auto.netty.v3_8.client.NettyResponseInjectAdapter.SETTER;
import static io.opentelemetry.trace.TracingContextUtils.withSpan;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.HttpTextFormat.Setter;
import io.opentelemetry.instrumentation.api.tracer.HttpClientTracer;
import io.opentelemetry.trace.Span;
import java.net.URI;
import java.net.URISyntaxException;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;

public class NettyHttpClientTracer extends HttpClientTracer<HttpRequest, HttpResponse> {
  public static final NettyHttpClientTracer TRACER = new NettyHttpClientTracer();

  @Override
  protected String method(HttpRequest httpRequest) {
    return httpRequest.getMethod().getName();
  }

  @Override
  protected URI url(HttpRequest request) throws URISyntaxException {
    URI uri = new URI(request.getUri());
    if ((uri.getHost() == null || uri.getHost().equals("")) && request.headers().contains(HOST)) {
      return new URI("http://" + request.headers().get(HOST) + request.getUri());
    } else {
      return uri;
    }
  }

  @Override
  protected Integer status(HttpResponse httpResponse) {
    return httpResponse.getStatus().getCode();
  }

  @Override
  protected String requestHeader(HttpRequest httpRequest, String name) {
    return httpRequest.headers().get(name);
  }

  @Override
  protected String responseHeader(HttpResponse httpResponse, String name) {
    return httpResponse.headers().get(name);
  }

  @Override
  protected Setter<HttpRequest> getSetter() {
    return null;
  }

  @Override
  public Scope startScope(Span span, HttpRequest request) {
    Context context = withSpan(span, Context.current());
    OpenTelemetry.getPropagators().getHttpTextFormat().inject(context, request.headers(), SETTER);
    return withScopedContext(context);
  }

  @Override
  protected String getInstrumentationName() {
    return "io.opentelemetry.auto.netty-3.8";
  }
}
