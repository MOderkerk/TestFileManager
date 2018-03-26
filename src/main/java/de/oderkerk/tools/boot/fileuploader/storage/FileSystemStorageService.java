package de.oderkerk.tools.boot.fileuploader.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
@Service
public class FileSystemStorageService implements StorageService {

	Logger logger = LoggerFactory.getLogger(FileSystemStorageService.class);
	@Value("${app.uploadfolder}")
	private String storage;
	private final Path rootLocation;

	@Autowired
	public FileSystemStorageService(StorageProperties properties) {
		this.rootLocation = Paths.get(properties.getLocation());
		if (logger.isDebugEnabled())
			logger.debug("Root location ={}", this.rootLocation.toString());
	}

	@Override
	public void store(MultipartFile file, String stage, String mandant) {
		String filename = StringUtils.cleanPath(file.getOriginalFilename());
		try {
			if (file.isEmpty()) {
				throw new StorageException("Failed to store empty file " + filename);
			}
			if (filename.contains("..")) {
				// This is a security check
				throw new StorageException(
						"Cannot store file with relative path outside current directory " + filename);
			}
			Files.copy(file.getInputStream(), this.rootLocation.resolve(stage + "/" + mandant + "/imp/" + filename),
					StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.toString());
			throw new StorageException("Failed to store file " + filename, e);
		}
	}

	@Override
	public Stream<Path> loadAll(String stage, String mandant) {
		try {
			return Files.walk(this.rootLocation.resolve(stage + "/" + mandant + "/imp/"), 10)
					.filter(path -> !path.equals(this.rootLocation)).map(path -> this.rootLocation.relativize(path));
		} catch (IOException e) {
			throw new StorageException("Failed to read stored files", e);
		}

	}

	@Override
	public Path load(String filename) {
		return rootLocation.resolve(filename);
	}

	@Override
	public Resource loadAsResource(String filename) {
		try {
			Path file = load(filename);
			Resource resource = new UrlResource(file.toUri());
			if (resource.exists() || resource.isReadable()) {
				return resource;
			} else {
				throw new StorageFileNotFoundException("Could not read file: " + filename);

			}
		} catch (MalformedURLException e) {
			throw new StorageFileNotFoundException("Could not read file: " + filename, e);
		}
	}

	@Override
	public void deleteAll() {
		// FileSystemUtils.deleteRecursively(rootLocation.toFile());
	}

	@Override
	public void init() {

	}

	@Override
	public void deleteFile(String filename) {
		if (logger.isDebugEnabled())
			logger.debug("Start Delete");
		try {
			if (logger.isDebugEnabled())
				logger.debug(storage + "/" + filename);
			Files.deleteIfExists(Paths.get(storage + "/" + filename));
		} catch (IOException e) {
			throw new StorageException("Could not delete File ", e);
		}

	}

	@Override
	public void deflateFile(String filename) {
		if (logger.isDebugEnabled())
			logger.debug("Deflate File {} ", filename);
		ZipInputStream zis = null;
		try {

			String[] parts = filename.split(Pattern.quote("."));
			String ending = parts[parts.length - 1];
			File inputFile = new File(storage + "/" + filename);
			if (logger.isDebugEnabled())
				logger.debug("Input File inkl Path {}", inputFile.toString());
			switch (ending.toLowerCase()) {
			case "zip":
				if (logger.isDebugEnabled())
					logger.debug("Zip File found");
				byte[] buffer = new byte[1024];
				zis = new ZipInputStream(new FileInputStream(inputFile));
				ZipEntry zipEntry = zis.getNextEntry();
				FileOutputStream fos = null;
				if (logger.isDebugEnabled())
					logger.debug("Zip Entry : {}", zipEntry.getName());
				while (zipEntry != null) {
					String outputFileName = zipEntry.getName();
					File newFile = new File(inputFile.getParentFile() + "/" + outputFileName);
					if (logger.isDebugEnabled())
						logger.debug("New Filename {}", newFile);
					try {
						fos = new FileOutputStream(newFile);
						int len;
						while ((len = zis.read(buffer)) > 0) {
							fos.write(buffer, 0, len);
						}

						zipEntry = zis.getNextEntry();
					} catch (IOException e) {

						throw e;
					} finally {
						if (fos != null)
							fos.close();

					}
					zis.closeEntry();
					zis.close();
				}

				break;
			case "7z":
				break;
			case "tar":
				break;
			default:
				throw new RuntimeException("File Format not supported");
			}
		} catch (IOException e) {
			logger.error(e.toString());
			throw new StorageException("Could not deflate File ", e);
		} finally {
			if (zis != null)
				try {
					zis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
	}
}
