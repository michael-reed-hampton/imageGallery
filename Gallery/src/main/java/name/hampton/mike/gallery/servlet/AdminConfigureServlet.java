package name.hampton.mike.gallery.servlet;

import java.security.Principal;

import name.hampton.mike.gallery.ApplicationConfiguration;

public class AdminConfigureServlet extends ConfigureServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -412342498116652191L;

	protected String setConfigurationValue(Principal principal, String configurationKey, String configurationValue) {
		return ApplicationConfiguration.getSingleton().setConfigurationValue(defaultKey, configurationKey, configurationValue);
	}
	
	protected String lookupConfigurationValue(Principal principal, String configurationKey) {
		return ApplicationConfiguration.getSingleton().lookupConfigurationValue(defaultKey, configurationKey);
	}
}
