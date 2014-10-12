package name.hampton.mike.gallery.servlet;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import name.hampton.mike.NamespaceVariableResolver;
import name.hampton.mike.gallery.ApplicationConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This checks to make sure the application has been configured.
 * 
 * If a user does not have access to the configuration, or skips it.  Then it wont work.
 * 
 * @author mike.hampton
 *
 */
public class ConfigurationCheckFilter implements Filter {

	private FilterConfig filterConfig;
	
	// Value used as a flag to prevent infinate recursion
	private static final String FIRED = ConfigurationCheckFilter.class.getName() + ".FIRED";
	// GalleryApplication.DEFAULT_KEY
	private static final String DEFAULT_KEY = "defaultKey";
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		this.filterConfig = filterConfig;
	}


	@Override
	public void destroy() {
	}

	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain filterChain) throws IOException, ServletException {

		boolean redirected = false;
		String defaultKey = //filterConfig.getInitParameter("defaultKey");
				filterConfig.getServletContext().getInitParameter(DEFAULT_KEY);
		ApplicationConfiguration.getSingleton().setDefaultConfigurationKey(defaultKey);

		if(request instanceof HttpServletRequest)
		{
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			HttpSession session = httpRequest.getSession(false);
			Object fired = null;
			if(null != session)
			{
				fired = session.getAttribute(FIRED);
			}
			
			// If the session has not yet checked the configuration, do it now.
			if(null == fired)
			{
				// Set this value immediately to prevent an infinate loop.
				session.setAttribute(FIRED, Boolean.TRUE);				
				
				// This is used to resolve variable references.  I needed this so that I could use
				// user specific values in this process.
				HashMap<String, Object> keyObjects = new HashMap<String, Object>();
				keyObjects.put("request", request);
				keyObjects.put("response", response);
				NamespaceVariableResolver resolver = new NamespaceVariableResolver(keyObjects); 
				
				String configurationPath = null;
				String role = null;
				String checkMajorKey = null;
				String checkConfigurationKey = null;
				int index=0;
				do
				{
					configurationPath = (String)resolver.resolveValue(filterConfig.getInitParameter("configurationPath"+index),keyObjects);
					if(null!=configurationPath)
					{
						role = (String)resolver.resolveValue(filterConfig.getInitParameter("role"+index),keyObjects);
						if(((HttpServletRequest) request).isUserInRole(role))
						{
							checkMajorKey = (String)resolver.resolveValue(filterConfig.getInitParameter("checkMajorKey"+index),keyObjects);
							checkConfigurationKey = (String)resolver.resolveValue(filterConfig.getInitParameter("checkConfigurationKey"+index),keyObjects);
							if(null!=checkMajorKey && null!=checkConfigurationKey)
							{
								// This is the value we need to check for this user.  If it is null, then it indicates the app is not yet configured.
								String value = ApplicationConfiguration.getSingleton().lookupConfigurationValue(checkMajorKey, checkConfigurationKey);
								if(null == value)
								{
									// We need to redirect if possible
									HttpServletResponse httpResponse = (HttpServletResponse) response;
									httpResponse.sendRedirect(configurationPath);
									redirected = true;
								}
							}
						}
					}
					index++;					
				}while(!redirected && null!=configurationPath); // Once we are redirected, we are done, or else the user does not have a configuration page
			}
		}
		if(!redirected)filterChain.doFilter(request, response);
	}

}
