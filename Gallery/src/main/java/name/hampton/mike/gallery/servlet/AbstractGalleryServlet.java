package name.hampton.mike.gallery.servlet;

import java.io.File;
import java.security.Principal;

import javax.servlet.http.HttpServlet;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.GalleryApplication;
import name.hampton.mike.gallery.exception.InvalidPathException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractGalleryServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5036002428092386031L;

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	/**
	 * 
	 * @return
	 */
	protected File getBaseDir(Principal principal) throws 
		InvalidPathException // <- maybe this should not be this type of exception...
	{
		return getDir(principal, GalleryApplication.BASE_DIR);
	}

	/**
	 * 
	 * @return
	 */
	protected File getDir(Principal principal, String configurationKey) throws 
		InvalidPathException // <- maybe this should not be this type of exception...
	{
		String directoryString = lookupConfigurationValue(principal, configurationKey);
		
		if (null == directoryString) {
			directoryString = getServletConfig().getServletContext()
					.getRealPath("/");
			logger.warn("Directory for "+configurationKey+" not configured!  Will use the value of getRealPath(\"/\"), which is '"
					+ directoryString + "'");
		}
		return getValidDirectory(directoryString);
	}

	protected String lookupConfigurationValue(Principal principal, String configurationKey) {
		String userName = principal.getName();
		return ApplicationConfiguration.getSingleton().lookupConfigurationValue(userName, configurationKey);
	}
		
	protected String setConfigurationValue(Principal principal, String configurationKey, String configurationValue) {
		String userName = principal.getName();
		return ApplicationConfiguration.getSingleton().setConfigurationValue(userName, configurationKey, configurationValue);
	}
		
	protected File getValidDirectory(String directoryString) throws 
		InvalidPathException 
	{
		File baseDir = new File(directoryString);
		return validateDirectory(baseDir);
	}
	protected File validateDirectory(File directoryFile) throws 
		InvalidPathException 
	{
		if (!directoryFile.canRead()) {
			throw new InvalidPathException(directoryFile.getAbsolutePath(), "Cannot read directory.");
		} else if (!directoryFile.exists()) {
			throw new InvalidPathException(directoryFile.getAbsolutePath(), "Directory does not exist.");
		} else if (!directoryFile.isDirectory()) {
			throw new InvalidPathException(directoryFile.getAbsolutePath(), "Specified directory is not a directory type!");
		}		
		return directoryFile;
	}

}
