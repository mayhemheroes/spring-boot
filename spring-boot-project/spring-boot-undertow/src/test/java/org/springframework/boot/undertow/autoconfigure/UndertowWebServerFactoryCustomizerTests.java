/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.undertow.autoconfigure;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.xnio.Option;
import org.xnio.OptionMap;
import org.xnio.Options;

import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.undertow.ConfigurableUndertowWebServerFactory;
import org.springframework.boot.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.server.autoconfigure.ServerProperties;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willAnswer;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link UndertowWebServerFactoryCustomizer}.
 *
 * @author Brian Clozel
 * @author Phillip Webb
 * @author Artsiom Yudovin
 * @author Rafiullah Hamedy
 * @author HaiTao Zhang
 */
class UndertowWebServerFactoryCustomizerTests {

	private final MockEnvironment environment = new MockEnvironment();

	private final ServerProperties serverProperties = new ServerProperties();

	private final UndertowServerProperties undertowProperties = new UndertowServerProperties();

	private UndertowWebServerFactoryCustomizer customizer;

	@BeforeEach
	void setup() {
		ConfigurationPropertySources.attach(this.environment);
		this.customizer = new UndertowWebServerFactoryCustomizer(this.environment, this.serverProperties,
				this.undertowProperties);
	}

	@Test
	void customizeUndertowAccessLog() {
		bind("server.undertow.accesslog.enabled=true", "server.undertow.accesslog.pattern=foo",
				"server.undertow.accesslog.prefix=test_log", "server.undertow.accesslog.suffix=txt",
				"server.undertow.accesslog.dir=test-logs", "server.undertow.accesslog.rotate=false");
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setAccessLogEnabled(true);
		then(factory).should().setAccessLogPattern("foo");
		then(factory).should().setAccessLogPrefix("test_log");
		then(factory).should().setAccessLogSuffix("txt");
		then(factory).should().setAccessLogDirectory(new File("test-logs"));
		then(factory).should().setAccessLogRotate(false);
	}

	@Test
	void customMaxHttpRequestHeaderSize() {
		bind("server.max-http-request-header-size=2048");
		assertThat(boundServerOption(UndertowOptions.MAX_HEADER_SIZE)).isEqualTo(2048);
	}

	@Test
	void customMaxHttpRequestHeaderSizeIgnoredIfNegative() {
		bind("server.max-http-request-header-size=-1");
		assertThat(boundServerOption(UndertowOptions.MAX_HEADER_SIZE)).isNull();
	}

	@Test
	void customMaxHttpRequestHeaderSizeIgnoredIfZero() {
		bind("server.max-http-request-header-size=0");
		assertThat(boundServerOption(UndertowOptions.MAX_HEADER_SIZE)).isNull();
	}

	@Test
	void customMaxHttpPostSize() {
		bind("server.undertow.max-http-post-size=256");
		assertThat(boundServerOption(UndertowOptions.MAX_ENTITY_SIZE)).isEqualTo(256);
	}

	@Test
	void customConnectionTimeout() {
		bind("server.undertow.no-request-timeout=1m");
		assertThat(boundServerOption(UndertowOptions.NO_REQUEST_TIMEOUT)).isEqualTo(60000);
	}

	@Test
	void customMaxParameters() {
		bind("server.undertow.max-parameters=4");
		assertThat(boundServerOption(UndertowOptions.MAX_PARAMETERS)).isEqualTo(4);
	}

	@Test
	void customMaxHeaders() {
		bind("server.undertow.max-headers=4");
		assertThat(boundServerOption(UndertowOptions.MAX_HEADERS)).isEqualTo(4);
	}

	@Test
	void customMaxCookies() {
		bind("server.undertow.max-cookies=4");
		assertThat(boundServerOption(UndertowOptions.MAX_COOKIES)).isEqualTo(4);
	}

	@Test
	void customizeIoThreads() {
		bind("server.undertow.threads.io=4");
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setIoThreads(4);
	}

	@Test
	void customizeWorkerThreads() {
		bind("server.undertow.threads.worker=10");
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setWorkerThreads(10);
	}

	@Test
	void enableSlashDecoding() {
		bind("server.undertow.decode-slash=true");
		assertThat(boundServerOption(UndertowOptions.DECODE_SLASH)).isTrue();
	}

	@Test
	void disableUrlDecoding() {
		bind("server.undertow.decode-url=false");
		assertThat(boundServerOption(UndertowOptions.DECODE_URL)).isFalse();
	}

	@Test
	void customUrlCharset() {
		bind("server.undertow.url-charset=UTF-16");
		assertThat(boundServerOption(UndertowOptions.URL_CHARSET)).isEqualTo(StandardCharsets.UTF_16.name());
	}

	@Test
	void disableAlwaysSetKeepAlive() {
		bind("server.undertow.always-set-keep-alive=false");
		assertThat(boundServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE)).isFalse();
	}

	@Test
	void customServerOption() {
		bind("server.undertow.options.server.ALWAYS_SET_KEEP_ALIVE=false");
		assertThat(boundServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE)).isFalse();
	}

	@Test
	void customServerOptionShouldBeRelaxed() {
		bind("server.undertow.options.server.always-set-keep-alive=false");
		assertThat(boundServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE)).isFalse();
	}

	@Test
	void customSocketOption() {
		bind("server.undertow.options.socket.CONNECTION_LOW_WATER=8");
		assertThat(boundSocketOption(Options.CONNECTION_LOW_WATER)).isEqualTo(8);
	}

	@Test
	void customSocketOptionShouldBeRelaxed() {
		bind("server.undertow.options.socket.connection-low-water=8");
		assertThat(boundSocketOption(Options.CONNECTION_LOW_WATER)).isEqualTo(8);
	}

	@Test
	void deduceUseForwardHeaders() {
		this.environment.setProperty("DYNO", "-");
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setUseForwardHeaders(true);
	}

	@Test
	void defaultUseForwardHeaders() {
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setUseForwardHeaders(false);
	}

	@Test
	void forwardHeadersWhenStrategyIsNativeShouldConfigureValve() {
		this.serverProperties.setForwardHeadersStrategy(ServerProperties.ForwardHeadersStrategy.NATIVE);
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setUseForwardHeaders(true);
	}

	@Test
	void forwardHeadersWhenStrategyIsNoneShouldNotConfigureValve() {
		this.environment.setProperty("DYNO", "-");
		this.serverProperties.setForwardHeadersStrategy(ServerProperties.ForwardHeadersStrategy.NONE);
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		this.customizer.customize(factory);
		then(factory).should().setUseForwardHeaders(false);
	}

	private <T> T boundServerOption(Option<T> option) {
		Builder builder = Undertow.builder();
		ConfigurableUndertowWebServerFactory factory = mockFactory(builder);
		this.customizer.customize(factory);
		OptionMap map = ((OptionMap.Builder) ReflectionTestUtils.getField(builder, "serverOptions")).getMap();
		return map.get(option);
	}

	private <T> T boundSocketOption(Option<T> option) {
		Builder builder = Undertow.builder();
		ConfigurableUndertowWebServerFactory factory = mockFactory(builder);
		this.customizer.customize(factory);
		OptionMap map = ((OptionMap.Builder) ReflectionTestUtils.getField(builder, "socketOptions")).getMap();
		return map.get(option);
	}

	private ConfigurableUndertowWebServerFactory mockFactory(Builder builder) {
		ConfigurableUndertowWebServerFactory factory = mock(ConfigurableUndertowWebServerFactory.class);
		willAnswer((invocation) -> {
			Object argument = invocation.getArgument(0);
			Arrays.stream((argument instanceof UndertowBuilderCustomizer undertowCustomizer)
					? new UndertowBuilderCustomizer[] { undertowCustomizer } : (UndertowBuilderCustomizer[]) argument)
				.forEach((customizer) -> customizer.customize(builder));
			return null;
		}).given(factory).addBuilderCustomizers(any());
		return factory;
	}

	private void bind(String... inlinedProperties) {
		TestPropertySourceUtils.addInlinedPropertiesToEnvironment(this.environment, inlinedProperties);
		Binder binder = new Binder(ConfigurationPropertySources.get(this.environment));
		binder.bind("server", Bindable.ofInstance(this.serverProperties));
		binder.bind("server.undertow", Bindable.ofInstance(this.undertowProperties));
	}

}
