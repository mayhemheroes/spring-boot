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

package org.springframework.boot.context.config;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import org.springframework.boot.env.PropertiesPropertySourceLoader;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.boot.testsupport.classpath.resources.WithResource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * Tests for {@link StandardConfigDataLoader}.
 *
 * @author Madhura Bhave
 * @author Phillip Webb
 */
class StandardConfigDataLoaderTests {

	private final StandardConfigDataLoader loader = new StandardConfigDataLoader();

	private final ConfigDataLoaderContext loaderContext = mock(ConfigDataLoaderContext.class);

	@Test
	@WithResource(name = "application.yml", content = """
			foo: bar
			---
			hello: world
			""")
	void loadWhenLocationResultsInMultiplePropertySourcesAddsAllToConfigData() throws IOException {
		ClassPathResource resource = new ClassPathResource("application.yml");
		StandardConfigDataReference reference = new StandardConfigDataReference(
				ConfigDataLocation.of("classpath:application.yml"), null, "classpath:application", null, "yml",
				new YamlPropertySourceLoader());
		StandardConfigDataResource location = new StandardConfigDataResource(reference, resource);
		ConfigData configData = this.loader.load(this.loaderContext, location);
		assertThat(configData.getPropertySources()).hasSize(2);
		PropertySource<?> source1 = configData.getPropertySources().get(0);
		PropertySource<?> source2 = configData.getPropertySources().get(1);
		assertThat(source1.getName()).isEqualTo("Config resource 'class path resource [application.yml]' "
				+ "via location 'classpath:application.yml' (document #0)");
		assertThat(source1.getProperty("foo")).isEqualTo("bar");
		assertThat(source2.getName()).isEqualTo("Config resource 'class path resource [application.yml]' "
				+ "via location 'classpath:application.yml' (document #1)");
		assertThat(source2.getProperty("hello")).isEqualTo("world");
	}

	@Test
	@WithResource(name = "empty.properties")
	void loadWhenPropertySourceIsEmptyAddsNothingToConfigData() throws IOException {
		ClassPathResource resource = new ClassPathResource("empty.properties");
		StandardConfigDataReference reference = new StandardConfigDataReference(
				ConfigDataLocation.of("empty.properties"), null, "empty", null, "properties",
				new PropertiesPropertySourceLoader());
		StandardConfigDataResource location = new StandardConfigDataResource(reference, resource);
		ConfigData configData = this.loader.load(this.loaderContext, location);
		assertThat(configData.getPropertySources()).isEmpty();
	}

}
