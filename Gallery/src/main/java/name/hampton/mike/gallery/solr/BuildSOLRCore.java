package name.hampton.mike.gallery.solr;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import name.hampton.mike.UnzipUtility;
import name.hampton.mike.gallery.exception.InvalidConfigurationException;
import name.hampton.mike.search.SearchException;
import name.hampton.mike.search.SearchIndexIntf;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.request.CoreAdminRequest;
import org.apache.solr.client.solrj.response.CoreAdminResponse;
import org.apache.solr.common.params.CoreAdminParams.CoreAdminAction;
import org.apache.solr.common.util.NamedList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Used to build a SOLR core for a given file path.  A SOLR core is similar to a table.
 * 
 * 
 * @author mike.hampton
 *
 */
public class BuildSOLRCore extends Observable{

	Logger logger = LoggerFactory.getLogger(this.getClass().getName());

	/**
	 * The base url for the SOLR server. Example: http://localhost:8983/solr
	 * 
	 */
	private String urlString = "";

	/**
	 * Creates the instance, and sets the url for the SOLR server.
	 * 
	 * @param solrServerURL
	 */
	public BuildSOLRCore(String solrServerURL) {
		urlString = solrServerURL;
		logger.debug("solrServerURL='" + solrServerURL + "'");
	}
	
	/**
	 * Used for observable notifications
	 * @param newCore
	 */
	private void notifyCoreCreation(String newCore){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, newCore, SOLRIndexingEvent.EventType.CORE_CREATED); 
		notifyObservers(event);
	}

	/**
	 * Used for observable notifications
	 * @param newCore
	 */
	private void notifyCoreCreationFailure(String newCore, int status, Object error){
		setChanged();
		SOLRIndexingEvent event = new SOLRIndexingEvent(this, newCore, SOLRIndexingEvent.EventType.CORE_CREATE_FAILED);
		event.setStatus(status);
		event.setError(error);
		
		notifyObservers(event);
	}

	/**
	 * Makes sure that there is a core JUST FOR THIS PATH!
	 * 
	 * If there is another core that also includes items from this path, it will
	 * not be found. This makes a core - for this specific path.
	 * 
	 * In other words, if this path is a sub directory of another path that has
	 * already been indexed, this will create a new core.
	 * 
	 * @param pathString
	 *            a path on the machine running this process.
	 * @throws SolrServerException
	 * @throws IOException
	 * @throws InvalidConfigurationException 
	 * @throws SearchException 
	 */
	public SearchIndexIntf ensureCore(String pathString, boolean reindexIfExists) throws SolrServerException,
			IOException, InvalidConfigurationException, SearchException {
		
		SearchIndexIntf indexer = null;
		
		// get an ID for the core based on this path.
		String coreID = SOLRUtilities.getCoreIDForPath(pathString);

		// Create the interface to the solr server
		SolrServer solrServer = new HttpSolrServer(urlString);

		// Request core list
		CoreAdminRequest request = new CoreAdminRequest();
		request.setAction(CoreAdminAction.STATUS);
		CoreAdminResponse cores = request.process(solrServer);

		boolean coreAlreadyExists = false;

		// We will remember what the default core is for possible later use.
		SolrCore defaultCore = null;

		// Not really using this right now, but useful to hold on to it for
		// debugging.
		List<SolrCore> coreList = new ArrayList<SolrCore>();
		// Iterate across the list of cores returned. If we find the core for
		// the path passed in,
		// we can quit, because we do not need to create it - it is already
		// there.
		for (int i = 0; i < cores.getCoreStatus().size() && !coreAlreadyExists; i++) {
			SolrCore existingCore = new SolrCore();
			existingCore.setName(cores.getCoreStatus().getName(i));

			if (coreID.equals(existingCore.getName())) {
				coreAlreadyExists = true;
				logger.debug("coreAlreadyExists for coreID='" + coreID + "'");
			} else {
				NamedList<Object> existingCoreValue = cores.getCoreStatus()
						.getVal(i);

				// Remember the default if we find it. If we need to create the
				// core, we will need it.
				if (null != existingCoreValue.get("isDefaultCore")) {
					Object coreIsDefault = existingCoreValue
							.get("isDefaultCore");
					if (coreIsDefault == Boolean.TRUE) {
						existingCore.setDefaultCore(true);
					}
				}
				if (existingCore.isDefaultCore())
					defaultCore = existingCore;

				existingCore.setInstanceDir(new File((String) existingCoreValue
						.get("instanceDir")));
				coreList.add(existingCore);
				logger.debug("Adding core='" + existingCore + "'");
			}
		}

		
		// If the core is not there, we need to create it and index the path
		if (!coreAlreadyExists) {
			logger.debug("core does not already exist");
			// we create the directory for the core in the configured solr
			// directory.
			// We will use the default core 'instancedir' parent directory as
			// the 'SOLR core root'
			if (null != defaultCore) {
				// <disk access>
				/*
				 * Use the parent directory of the 'instancedir' of the default
				 * core as the 'SOLR core root' <lst name="collection1"> <str
				 * name="name">collection1</str> <bool
				 * name="isDefaultCore">true</bool> <str
				 * name="instanceDir">K:\research
				 * \solr-image-index\solr\collection1\</str>
				 * 
				 * This means that the SOLR deployment must be on the same
				 * machine. This is bad. 
				 * 
				 * TODO: Put the process to create the
				 * core onto the SOLR deployment. That would be the process down
				 * to the tag below </disk access>. I see this as a failing in
				 * the SOLR deployment. It seems like there should be a simple
				 * way to set this up (like this!) in the deployment itself.
				 */
				File solrCoreRoot = defaultCore.getInstanceDir()
						.getParentFile();
				SolrCore newCore = new SolrCore();
				newCore.setName(coreID);
				// Use the 'SOLR core root' and the coreID we want to create
				// EX: 'SOLR core root' is K:\\research\\solr-image-index\\solr,
				// coreID is c__some_kind_of_path
				// this is then
				// K:\\research\\solr-image-index\\solr\\c__some_kind_of_path
				newCore.setInstanceDir(new File(solrCoreRoot, coreID));
				if (newCore.getInstanceDir().exists()) {
					throw new IOException(
							"Directory for the new core: '"
									+ newCore.getInstanceDir()
											.getAbsolutePath()
									+ "' already exists! Aborting creation of new SOLR core.  This can only happen if there is an old"
									+ " SOLR core that was removed but not deleted from the file system, or there happens to be a"
									+ " directory in the 'SOLR core root' that is named the same as the hash for the path passed in.");
				}
				// create the directory
				if (!newCore.getInstanceDir().mkdirs()) {
					throw new IOException(
							"Could not create directories for the new core: '"
									+ newCore.getInstanceDir()
											.getAbsolutePath()
									+ "', aborting creation of new SOLR core.  Search for this path will not be available");
				}
				// Get the temp file and unzip the template file into it
				File defaultCoreTemplateFile = getDefaultCoreTemplateFile(solrCoreRoot);
				new UnzipUtility().unzip(defaultCoreTemplateFile
						.getAbsolutePath(), newCore.getInstanceDir()
						.getAbsolutePath());
				// </disk access>

				// make the call to solr to create the core
				// If you do it via a browser, it looks like this:
				// http://localhost:8983/solr/admin/cores?action=CREATE&name=C__Users_mike.hampton_Pictures&instanceDir=K:\research\solr-image-index\solr\C__Users_mike.hampton_Pictures
				CoreAdminResponse createCoreResponse = CoreAdminRequest
						.createCore(newCore.getName(), newCore.getInstanceDir()
								.getAbsolutePath(), solrServer);
				logger.debug("Create core response = " + createCoreResponse);
				// TODO: Detect success/failure!!!!
				// Success:
				// <response>
				// <lst name="responseHeader">
				// <int name="status">0</int>
				// <int name="QTime">561</int>
				// </lst>
				// <str name="core">C__Users_mike.hampton_Pictures</str>
				// </response>
				//
				// Failure:
				// 	<response>
				// 		<lst name="responseHeader">
				// 			<int name="status">500</int>
				// 			<int name="QTime">2</int>
				// 		</lst>
				// 		<lst name="error">
				// 			<str name="msg">
				// 				Core with name 'C__Users_mike.hampton_Pictures' already
				// 				exists.
				// 			</str>
				// 			<str name="trace">
				// org.apache.solr.common.SolrException: Core with name
				// 'C__Users_mike.hampton_Pictures' already exists. at
				// org.apache.solr.handler.admin.CoreAdminHandler.handleCreateAction(CoreAdminHandler.java:555)
				// at
				// org.apache.solr.handler.admin.CoreAdminHandler.handleRequestInternal(CoreAdminHandler.java:199)
				// at
				// org.apache.solr.handler.admin.CoreAdminHandler.handleRequestBody(CoreAdminHandler.java:188)
				// at
				// org.apache.solr.handler.RequestHandlerBase.handleRequest(RequestHandlerBase.java:135)
				// at
				// org.apache.solr.servlet.SolrDispatchFilter.handleAdminRequest(SolrDispatchFilter.java:729)
				// at
				// org.apache.solr.servlet.SolrDispatchFilter.doFilter(SolrDispatchFilter.java:258)
				// at
				// org.apache.solr.servlet.SolrDispatchFilter.doFilter(SolrDispatchFilter.java:207)
				// at
				// org.eclipse.jetty.servlet.ServletHandler$CachedChain.doFilter(ServletHandler.java:1419)
				// at
				// org.eclipse.jetty.servlet.ServletHandler.doHandle(ServletHandler.java:455)
				// at
				// org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:137)
				// at
				// org.eclipse.jetty.security.SecurityHandler.handle(SecurityHandler.java:557)
				// at
				// org.eclipse.jetty.server.session.SessionHandler.doHandle(SessionHandler.java:231)
				// at
				// org.eclipse.jetty.server.handler.ContextHandler.doHandle(ContextHandler.java:1075)
				// at
				// org.eclipse.jetty.servlet.ServletHandler.doScope(ServletHandler.java:384)
				// at
				// org.eclipse.jetty.server.session.SessionHandler.doScope(SessionHandler.java:193)
				// at
				// org.eclipse.jetty.server.handler.ContextHandler.doScope(ContextHandler.java:1009)
				// at
				// org.eclipse.jetty.server.handler.ScopedHandler.handle(ScopedHandler.java:135)
				// at
				// org.eclipse.jetty.server.handler.ContextHandlerCollection.handle(ContextHandlerCollection.java:255)
				// at
				// org.eclipse.jetty.server.handler.HandlerCollection.handle(HandlerCollection.java:154)
				// at
				// org.eclipse.jetty.server.handler.HandlerWrapper.handle(HandlerWrapper.java:116)
				// at org.eclipse.jetty.server.Server.handle(Server.java:368) at
				// org.eclipse.jetty.server.AbstractHttpConnection.handleRequest(AbstractHttpConnection.java:489)
				// at
				// org.eclipse.jetty.server.BlockingHttpConnection.handleRequest(BlockingHttpConnection.java:53)
				// at
				// org.eclipse.jetty.server.AbstractHttpConnection.headerComplete(AbstractHttpConnection.java:942)
				// at
				// org.eclipse.jetty.server.AbstractHttpConnection$RequestHandler.headerComplete(AbstractHttpConnection.java:1004)
				// at
				// org.eclipse.jetty.http.HttpParser.parseNext(HttpParser.java:640)
				// at
				// org.eclipse.jetty.http.HttpParser.parseAvailable(HttpParser.java:235)
				// at
				// org.eclipse.jetty.server.BlockingHttpConnection.handle(BlockingHttpConnection.java:72)
				// at
				// org.eclipse.jetty.server.bio.SocketConnector$ConnectorEndPoint.run(SocketConnector.java:264)
				// at
				// org.eclipse.jetty.util.thread.QueuedThreadPool.runJob(QueuedThreadPool.java:608)
				// at
				// org.eclipse.jetty.util.thread.QueuedThreadPool$3.run(QueuedThreadPool.java:543)
				// at java.lang.Thread.run(Thread.java:745)
				// </str>
				// <int name="code">500</int>
				// </lst>
				// </response>
				if (createCoreResponse.getStatus() == 0) {
					notifyCoreCreation(pathString);
					
					// indexer = SearchProvider.getSingleton().getSearchIndexIntf(principal);
					
					indexer = new SOLRIndexer(urlString, pathString);
					// Link the observers
					Observer observer = new Observer(){
						@Override
						public void update(Observable o, Object arg) {
							setChanged();
							BuildSOLRCore.this.notifyObservers(arg);
						}
					};
					indexer.addObserver(observer);
					
					// int successCount = indexer.indexDir(pathString);
					int successCount = indexer.indexItem(pathString);
					logger.debug("SOLRIndexer indexDir returned a success count of " + successCount);
				} else {
					// bad stuff...
					Object error = createCoreResponse.getResponse().get("error");
					notifyCoreCreationFailure(pathString, createCoreResponse.getStatus(), error);
					logger.error("Create core response = "
							+ createCoreResponse.getResponse());
					throw new IOException("Error creating core.  Code="
							+ createCoreResponse.getStatus());
				}
			}
		} else if(reindexIfExists){
			// Effectively reindex the contents.
			indexer = new SOLRIndexer(urlString,pathString);
			// indexer = SearchProvider.getSingleton().getSearchIndexIntf(principal);
			// Link the observers
			Observer observer = new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					setChanged();
					BuildSOLRCore.this.notifyObservers(arg);
				}
			};
			indexer.addObserver(observer);

			int successCount = indexer.reIndexItem(pathString);
			logger.debug("SOLRIndexer reIndexDir returned a success count of " + successCount);
		}
		return indexer;
	}

	/**
	 * SOLR does not allow you to create a core from a simple REST call.  Files must exist in the
	 * correct directories on the file system, so this finds a zip file that has them in there, or else
	 * this whole thing will fail.
	 * 
	 * @param solrCoreRoot
	 * @return
	 * @throws IOException
	 */
	private File getDefaultCoreTemplateFile(File solrCoreRoot)
			throws IOException {
		// The template file needs to be somewhere. Rather than configure
		// everything, this will assume the following:
		// If there is no default core template file configured, then this will
		// look in the 'SOLR core root' for a file named
		// 'core_template_conf.zip'. If not found then it will throw an error
		// stating that this file is not present.
		File defaultCoreTemplateFile = new File(solrCoreRoot,
				"core_template_conf.zip");
		if (!defaultCoreTemplateFile.exists()
				|| !defaultCoreTemplateFile.canRead()) {
			throw new IOException(
					"Cannot find default core template file: '"
							+ defaultCoreTemplateFile
							+ "', aborting creation of new SOLR core.  Search for this path will not be available");
		}
		return defaultCoreTemplateFile;
	}
}
