package name.hampton.mike.gallery.servlet;

import java.io.File;
import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.hampton.mike.gallery.DirectoryDisplay;
import name.hampton.mike.gallery.DisplayData;
import name.hampton.mike.gallery.SearchCriteria;
import name.hampton.mike.gallery.exception.InvalidPathException;

import com.google.gson.Gson;

public class DirectoryDisplayServlet extends AbstractGalleryServlet {
	
	class DirectoryDisplayHelper extends DirectoryDisplay {
		private Principal principal;  

		public Principal getPrincipal() {
			return principal;
		}

		public void setPrincipal(Principal principal) {
			this.principal = principal;
		}

		@Override
		protected File getBaseDir() throws InvalidPathException {
			return DirectoryDisplayServlet.this.getBaseDir(principal);
		}
	}; 
	
	DirectoryDisplayHelper directoryDisplayHelper = new DirectoryDisplayHelper();

	/**
	 * 
	 */
	private static final long serialVersionUID = 147641395094130965L;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.debug("doGet !!!!!!!");
	}

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");
		Gson gson = new Gson();

		DisplayData displayData = null;
		try {
			directoryDisplayHelper.setPrincipal(request.getUserPrincipal());
			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = request.getReader().readLine()) != null) {
				sb.append(s);
			}
			SearchCriteria searchCriteria = (SearchCriteria) gson.fromJson(
					sb.toString(), SearchCriteria.class);
			
			displayData = directoryDisplayHelper.search(searchCriteria);

		} 
		catch (Exception ex) {
			ex.printStackTrace();
			if(null == displayData)
			{
				displayData = new DisplayData(); 
			}
			displayData.getStatus().setSuccess(false);
			displayData.getStatus().setDescription(ex.getMessage());
		}
		response.getOutputStream().print(gson.toJson(displayData));
		response.getOutputStream().flush();
	};

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		System.out.println("doPost !!!!!!!");
		processRequest(req, resp);
	}

}
