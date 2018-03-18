package de.oderkerk.tools.boot.fileuploader.storage;

/**
 * 
 * @author Odin
 * @since 18.03.2018
 *
 */
public class StorageFileNotFoundException extends StorageException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8860672406626761671L;

	public StorageFileNotFoundException(String message) {
		super(message);
	}

	public StorageFileNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}