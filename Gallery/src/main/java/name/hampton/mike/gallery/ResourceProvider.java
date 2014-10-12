package name.hampton.mike.gallery;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Utility interface to bridge the gap between class.getResource() Aand servletContext.getReosurce()
 * 
 * @author mike.hampton
 *
 */
public interface ResourceProvider {
	public URL getResource(String path) throws MalformedURLException;
}
