package de.oderkerk.tools.boot.fileuploader.storage;

import java.nio.file.Path;
import java.util.stream.Stream;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
public interface StorageService {

	void init();

	void store(MultipartFile file, String stage, String mandant);

	Stream<Path> loadAll(String stage, String mandant);

	Path load(String filename);

	Resource loadAsResource(String filename);

	void deleteAll();

	void deleteFile(String filename);

	void deflateFile(String filename);

}
