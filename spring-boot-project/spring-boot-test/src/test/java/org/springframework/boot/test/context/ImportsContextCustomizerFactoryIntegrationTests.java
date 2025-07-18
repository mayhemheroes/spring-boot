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

package org.springframework.boot.test.context;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ImportsContextCustomizerFactoryIntegrationTests.ImportedBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Integration tests for {@link ImportsContextCustomizerFactory} and
 * {@link ImportsContextCustomizer}.
 *
 * @author Phillip Webb
 */
@ExtendWith(SpringExtension.class)
@Import(ImportedBean.class)
class ImportsContextCustomizerFactoryIntegrationTests {

	@Autowired
	private ApplicationContext context;

	@Autowired
	private ImportedBean bean;

	@Test
	void beanWasImported() {
		assertThat(this.bean).isNotNull();
	}

	@Test
	void testItselfIsNotABean() {
		assertThatExceptionOfType(NoSuchBeanDefinitionException.class)
			.isThrownBy(() -> this.context.getBean(getClass()));
	}

	@Component
	static class ImportedBean {

	}

}
