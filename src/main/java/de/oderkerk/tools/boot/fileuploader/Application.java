package de.oderkerk.tools.boot.fileuploader;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import de.oderkerk.tools.boot.fileuploader.storage.StorageProperties;
import de.oderkerk.tools.boot.fileuploader.storage.StorageService;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	CommandLineRunner init(StorageService storageService) {
		return (args) -> {
			// storageService.deleteAll();
			// storageService.init();
		};
	}
}
