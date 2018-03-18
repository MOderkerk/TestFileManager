package de.oderkerk.tools.boot.fileuploader;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.file.Paths;
import java.util.stream.Stream;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.Base64Utils;

import de.oderkerk.tools.boot.fileuploader.storage.StorageFileNotFoundException;
import de.oderkerk.tools.boot.fileuploader.storage.StorageService;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc
@SpringBootTest
public class FileUploadTests {
	@Value("${app.user}")
	private String user;

	@Value("${app.pw}")
	private String password;
	@Autowired
	private MockMvc mvc;

	@MockBean
	private StorageService storageService;

	@Test
	public void shouldListAllFiles() throws Exception {
		given(this.storageService.loadAll("aete", "M00"))
				.willReturn(Stream.of(Paths.get("first.txt"), Paths.get("second.txt")));

		this.mvc.perform(get("/filter?stage=aete&mandant=M00").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString((user + ":" + password).getBytes()))).andExpect(status().isOk())
				.andExpect(model().attribute("files",
						Matchers.contains("http://localhost/files/first.txt", "http://localhost/files/second.txt")));
	}

	@Test
	public void shouldSaveUploadedFile() throws Exception {
		MockMultipartFile multipartFile = new MockMultipartFile("file", "test.txt", "text/plain",
				"Spring Framework".getBytes());
		this.mvc.perform(multipart("/").file(multipartFile).param("stage", "aete").param("mandant", "M00"))
				.andExpect(status().isFound()).andExpect(header().string("Location", "/"));

		then(this.storageService).should().store(multipartFile, "aete", "M00");
	}

	@Test
	public void should404WhenMissingFile() throws Exception {
		given(this.storageService.loadAsResource("test.txt")).willThrow(StorageFileNotFoundException.class);

		this.mvc.perform(get("/files/test.txt").header(HttpHeaders.AUTHORIZATION,
				"Basic " + Base64Utils.encodeToString((user + ":" + password).getBytes())))
				.andExpect(status().isNotFound());
	}

}
