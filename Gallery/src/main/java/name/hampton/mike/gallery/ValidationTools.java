package name.hampton.mike.gallery;

import java.io.File;

import name.hampton.mike.gallery.exception.InvalidPathException;

public class ValidationTools {

	public static File getValidDirectory(String directoryString)
			throws InvalidPathException // <- maybe this should not be this type
										// of exception...
	{
		if(null == directoryString)
		{
			throw new InvalidPathException("Variable 'directoryString' is null.");
		}
		File baseDir = new File(directoryString);
		return validateDirectory(baseDir);
	}

	public static File validateDirectory(File directoryFile)
			throws InvalidPathException // <- maybe this should not be this type
										// of exception...
	{
		validateItem(directoryFile);
		if (!directoryFile.isDirectory()) {
			throw new InvalidPathException(directoryFile.getAbsolutePath(),
					"Specified directory is not a directory type!");
		}
		return directoryFile;
	}

	public static File validateItem(File aFile)
			throws InvalidPathException // <- maybe this should not be this type
										// of exception...
	{
		if (!aFile.canRead()) {
			throw new InvalidPathException(aFile.getAbsolutePath(),
					"Cannot read item.");
		} else if (!aFile.exists()) {
			throw new InvalidPathException(aFile.getAbsolutePath(),
					"Item does not exist.");
		}
		return aFile;
	}

}
