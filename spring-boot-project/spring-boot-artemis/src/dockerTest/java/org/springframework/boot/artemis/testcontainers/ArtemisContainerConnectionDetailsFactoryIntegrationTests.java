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

package org.springframework.boot.artemis.testcontainers;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.artemis.autoconfigure.ArtemisAutoConfiguration;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.jms.autoconfigure.JmsAutoConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.testsupport.container.TestImage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link ArtemisContainerConnectionDetailsFactory}.
 *
 * @author Eddú Meléndez
 */
@SpringJUnitConfig
@Testcontainers(disabledWithoutDocker = true)
class ArtemisContainerConnectionDetailsFactoryIntegrationTests {

	@Container
	@ServiceConnection
	static final ArtemisContainer artemis = TestImage.container(ArtemisContainer.class);

	@Autowired
	private JmsMessagingTemplate jmsTemplate;

	@Autowired
	private TestListener listener;

	@Test
	void connectionCanBeMadeToActiveMQContainer() {
		this.jmsTemplate.convertAndSend("sample.queue", "message");
		Awaitility.waitAtMost(Duration.ofMinutes(1))
			.untilAsserted(() -> assertThat(this.listener.messages).containsExactly("message"));
	}

	@Configuration(proxyBeanMethods = false)
	@ImportAutoConfiguration({ ArtemisAutoConfiguration.class, JmsAutoConfiguration.class })
	static class TestConfiguration {

		@Bean
		TestListener testListener() {
			return new TestListener();
		}

	}

	static class TestListener {

		private final List<String> messages = new ArrayList<>();

		@JmsListener(destination = "sample.queue")
		void processMessage(String message) {
			this.messages.add(message);
		}

	}

}
