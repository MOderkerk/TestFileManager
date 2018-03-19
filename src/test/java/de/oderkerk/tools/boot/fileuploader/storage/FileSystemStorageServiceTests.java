/*
 * Copyright 2016-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.oderkerk.tools.boot.fileuploader.storage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
public class FileSystemStorageServiceTests {

	private StorageProperties properties = new StorageProperties();
	private FileSystemStorageService service;

	@Before
	public void init() {
		properties.setLocation("target/files/" + Math.abs(new Random().nextLong()));
		service = new FileSystemStorageService(properties);
		service.init();
	}

	@Test
	public void loadNonExistent() {
		assertThat(service.load("xxx.txt")).doesNotExist();
	}

	@Test(expected = StorageException.class)
	public void saveNotPermitted() {
		service.store(new MockMultipartFile("xxx", "../xxx.txt", MediaType.TEXT_PLAIN_VALUE, "Hello World".getBytes()),
				".", ".");
	}

	// @Test
	// public void savePermitted() {
	// service.store(
	// new MockMultipartFile("xxx", "bar/../xxx.txt", MediaType.TEXT_PLAIN_VALUE,
	// "Hello World".getBytes()),
	// ".", ".");
	// }

}
