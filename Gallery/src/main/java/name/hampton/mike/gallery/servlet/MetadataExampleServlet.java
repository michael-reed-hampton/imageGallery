package name.hampton.mike.gallery.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.hampton.mike.gallery.MetadataExample;

import org.apache.commons.imaging.ImageReadException;

/* example of usage
 * 
 * http://server:8080/gallery/metaDataExample?imageUrl=http%3A%2F%2Fptforum.photoolsweb.com%2Fubbthreads.php%3Fubb%3Ddownload%26Number%3D1024%26filename%3D1024-2006_1011_093752.jpg
 * 
 * Note that this only works with unsecured urls.
 */

public class MetadataExampleServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException ,IOException 
	{
		System.out.println("doGet !!!!!!!");
		
		
		String imageUrlString = req.getParameter("imageUrl");
		resp.getWriter().println("imageUrlString="+String.valueOf(imageUrlString));
		if(null!=imageUrlString)
		{
			URL url = new URL(imageUrlString);
			if(null!=url)
			{
				InputStream inputStream = url.openStream();
				try {
					MetadataExample.metadataExample(inputStream, resp.getWriter());
				} catch (ImageReadException e) {
					e.printStackTrace(resp.getWriter());
				}
			}
		}	
	};

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		System.out.println("doPost !!!!!!!");
	}
	
}
