package name.hampton.mike.gallery.rest;

import javax.imageio.ImageIO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;

@Path("imageUtil")
public class ImageUtilREST {

	@Context
    SecurityContext securityContext;
	
	/**
	 * Method handling HTTP GET requests. The returned object will be sent to
	 * the client as "text/plain" media type.
	 *
	 * @return String that will be returned as a text/plain response.
	 */
	@GET
	@Path("imageTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public String[] ping() {
		return ImageIO.getReaderFileSuffixes();
	}
	
	/*
{ items: [
	{
	  "thumb":"images/extraWide.jpg",
	  "large":"images/extraWide.jpg",
	  "title":"I'm wide, me",
	  "link":"http://www.flickr.com/photos/44153025@N00/748348847"
	},
	{
	 "thumb":"images/imageHoriz.jpg",
	  "large":"images/imageHoriz.jpg",
	  "title":"I'm a horizontal picture",
	  "link":"http://www.flickr.com/photos/44153025@N00/735656038"
	},
	{
	  "thumb":"images/imageHoriz2.jpg",
	  "large":"images/imageHoriz2.jpg",
	  "title":"I'm another horizontal picture",
	  "link":"http://www.flickr.com/photos/44153025@N00/714540483"
	},
	{
	 "thumb":"images/imageVert.jpg",
	  "large":"images/imageVert.jpg",
	  "title":"I'm a vertical picture",
	  "link":"http://www.flickr.com/photos/44153025@N00/715392758"
	},
	{
	 "large":"images/square.jpg",
	 "thumb":"images/square.jpg",
	 "link" :"images/square.jpg",
	 "title":"1:1 aspect ratio"
	}
]}	 */
	
	
	
	
}