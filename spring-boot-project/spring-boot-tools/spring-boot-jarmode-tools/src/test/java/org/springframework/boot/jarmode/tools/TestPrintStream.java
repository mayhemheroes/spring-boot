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

package org.springframework.boot.jarmode.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AssertProvider;
import org.assertj.core.api.Assertions;

import org.springframework.boot.jarmode.tools.TestPrintStream.PrintStreamAssert;
import org.springframework.util.FileCopyUtils;

/**
 * {@link PrintStream} that can be used for testing.
 *
 * @author Phillip Webb
 */
class TestPrintStream extends PrintStream implements AssertProvider<PrintStreamAssert> {

	private final Class<?> testClass;

	TestPrintStream(Object testInstance) {
		super(new ByteArrayOutputStream());
		this.testClass = testInstance.getClass();
	}

	@Override
	public PrintStreamAssert assertThat() {
		return new PrintStreamAssert(this);
	}

	@Override
	public String toString() {
		return this.out.toString();
	}

	static final class PrintStreamAssert extends AbstractAssert<PrintStreamAssert, TestPrintStream> {

		private PrintStreamAssert(TestPrintStream actual) {
			super(actual, PrintStreamAssert.class);
		}

		void hasSameContentAsResource(String resource) {
			try {
				try (InputStream stream = this.actual.testClass.getResourceAsStream(resource)) {
					Assertions.assertThat(stream).as("Resource '%s'", resource).isNotNull();
					String content = FileCopyUtils.copyToString(new InputStreamReader(stream, StandardCharsets.UTF_8));
					hasSameContent(content);
				}
			}
			catch (IOException ex) {
				throw new IllegalStateException(ex);
			}
		}

		void hasSameContent(String content) {
			Assertions.assertThat(this.actual).hasToString(content);
		}

		void contains(String text) {
			Assertions.assertThat(this.actual.toString()).contains(text);
		}

	}

}
