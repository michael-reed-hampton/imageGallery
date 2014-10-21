package name.hampton.mike.gallery.servlet;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.search.SearchException;
import name.hampton.mike.search.SearchIndexIntf;
import name.hampton.mike.search.SearchProvider;

/**
 * It is possible for the monitor that watches the files being indexed to miss deletions.  This is
 * one way we handle this situation.  It will not solve all the problems with a non-transactional
 * multi threaded/process solution, but it will make it better.  It may still be necessary to do a 
 * reindex check on occasion.
 * 
 * @author mike.hampton
 *
 */
public class StatusNotifierFileServlet extends FileServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6587506590247659816L;

	/**
	 * Notifies that a file does not exist.
	 * 
	 */
	protected boolean shouldAbortOnNonExistantFile(HttpServletRequest request, HttpServletResponse response, File fileNoLongerExists)
			throws IOException {
		
		if(null != fileNoLongerExists){
			// notify someone
			SearchIndexIntf searchIndexIntf;
			try {
				searchIndexIntf = SearchProvider.getSingleton().getSearchIndexIntf(request.getUserPrincipal());
				searchIndexIntf.deleteItem(fileNoLongerExists);
			} catch (InvalidConfigurationException e) {
				logger.error("",e);
			} catch (SearchException e) {
				logger.error("",e);
			}
		}
		// else we do not know why this was asked for, just ignore the request and let the super class decide how to react.
		
		return super.shouldAbortOnNonExistantFile(request, response, fileNoLongerExists);		
	}

}
