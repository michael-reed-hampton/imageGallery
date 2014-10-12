package name.hampton.mike.gallery.servlet;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Observable;
import java.util.Observer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import name.hampton.mike.gallery.ApplicationConfiguration;
import name.hampton.mike.gallery.DisplayData;
import name.hampton.mike.gallery.Status;
import name.hampton.mike.gallery.solr.BuildSOLRCore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ConfigureServlet extends AbstractGalleryServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3239322674296930065L;
	private static final String CONFIGURATION_KEYS = "configurationKeys";
	// GalleryApplication.DEFAULT_KEY
	public static final String DEFAULT_KEY = "defaultKey";
	
	protected String defaultKey;

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.debug("doGet !!!!!!!");
	}

	protected void processRequest(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("application/json");

		// I want the null values in the HashMap to be passed, so there is a different Gson
		//Gson gson = new Gson();
		Gson gson = new GsonBuilder().serializeNulls().create();
		
		DisplayData displayData = new DisplayData();
		displayData.setStatus(new Status());

		try {
			String configurationKeys = 
				getServletConfig().getInitParameter(CONFIGURATION_KEYS);
			
			logger.debug("configurationKeys="+configurationKeys);
			
			String[] configurationKeyArray = configurationKeys.split(",",-1);
			logger.debug("configurationKeyArray="+Arrays.asList(configurationKeyArray));
			
			HashMap<String, String> configuration = new HashMap<String, String>(configurationKeyArray.length);

			StringBuilder sb = new StringBuilder();
			String s;
			while ((s = request.getReader().readLine()) != null) {
				sb.append(s);			
			}
			logger.debug("Input JSON="+sb.toString());			

			@SuppressWarnings("unchecked")
			HashMap<String, String> inputConfiguration = (HashMap<String, String>) gson.fromJson(
					sb.toString(), configuration.getClass());
			if(null != inputConfiguration){
				logger.debug("inputConfiguration="+inputConfiguration);			
				// If there is an input configuration, save it
				for(String configurationKey : inputConfiguration.keySet()){
					logger.debug("setting " + configurationKey + "=" + inputConfiguration.get(configurationKey));			
					setConfigurationValue(request.getUserPrincipal(), configurationKey, inputConfiguration.get(configurationKey));
				}
			}
			
			for(String configurationKey : configurationKeyArray){
				configuration.put(configurationKey.trim(), lookupConfigurationValue(request.getUserPrincipal(), configurationKey.trim()));
			}
			logger.debug("configuration="+configuration);

			displayData.getStatus().setSuccess(true);
			displayData.setData(configuration);
			
			//TODO: Post a message that says this persons config changed.
			
			// ****************************************************************
			//TODO: Make this more abstract.  This Should be elsewhere!!!!
			// ****************************************************************
			String solrURL = lookupConfigurationValue(request.getUserPrincipal(), name.hampton.mike.gallery.GalleryApplication.SOLR_URL);
			BuildSOLRCore solrCoreBuilder = new BuildSOLRCore(solrURL);
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					logger.debug("Observable update: " + arg);
				}
			};
			solrCoreBuilder.addObserver(observer );
			String myBaseDir = lookupConfigurationValue(request.getUserPrincipal(), name.hampton.mike.gallery.GalleryApplication.BASE_DIR);
			File myBaseDirFile = new File(myBaseDir);
			myBaseDir = myBaseDirFile.getCanonicalPath();
			solrCoreBuilder.ensureCore(myBaseDir, true);
			// ****************************************************************
			// ****************************************************************
		} 
		catch (Exception ex) {
			ex.printStackTrace();
			displayData.getStatus().setSuccess(false);
			displayData.getStatus().setDescription(ex.getMessage());
		}
		response.getOutputStream().print(gson.toJson(displayData));
		response.getOutputStream().flush();
	};

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		logger.debug("doPost !!!!!!!");
		defaultKey =
				getServletContext().getInitParameter(DEFAULT_KEY);
		ApplicationConfiguration.getSingleton().setDefaultConfigurationKey(defaultKey);
		processRequest(req, resp);
	}

}
