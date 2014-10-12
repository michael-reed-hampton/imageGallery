Web App.

What It Does
------------------------ 
    You configure it with a directory.
    It uses the directory structure, walking it for pictures it understands.
        "pictures it understands" - should allow for pluggable image formats (including movies, etc).  Currently using the imageio for this.
    Initial display is the root directory configured.
    Each display
    	
        Links at the top for sub directories.  Each link leads to a display based on that directory.
        Thumbnails of images in the directory, with expandable div below that will display exif if requested.
            this is the display for the directory, and for a search.
            
        The number of links, and thumbnails (both types will be referred to as elements here) displayed per row will be dependent 
        	on the size of the element, and	the size of the requesting screen.  If the element is 50 wide, and the screen is 200 wide,
        	then at most we could put 4 elements on a row.  There would likely be less depending on the padding of the elements. The
        	display should wrap the results dynamically - if the screen size is changed (the window is resized).  This precludes the use
        	of a grid widget, as they have static numbers of columns.  It is more of a flowlayout.     
            
            
        searchtool at the top 
            Will search the exif data, keys as well as values, as well as the filename/directoryname.
            Checkbox for "search subdirectories".
            Have advanced search div that can be expanded, that will allow user to specify 
                exif fields to search (Microsoft.XP.Keywords for example).  The list of keywords will be compiled from the keywords available on each image (not 
                	a hard coded list, built from image database).
                just keys
                just values
                just filenames
            Display results as found, allow user to cancel ongoing search.
        Allows user to select 1 or many images for editing of tag (exif) information.
        	User creates or edits exif tags.
        Sort images Based on: 
        	Date Created - from the file
        	Date Taken (might be null)
        	Type of File
        	Name of File
        	Rating (might be null)
        	
        	
Current State
-----------------------
Put in a filter that checks to see if the application is configured properly, if not, it redirects to the application configuration page, or the users
	personal configuration page depending on the configuration of the 'ConfigurationCheckFilter'.  Did some cool stuff here based on roles, and also
	started a pretty cool namespaced replacement utility (NamespaceVariableResolver).
Got the dojo tree working, and even managed to handle session expiration errors.  This is done by "rewiring" the 'onSubmit' of the form in the login page to
	be asynchronous, and then hooking the response to the originally calling function.  See index.html, specifically the 'buildOnError' javascript function.
	Had to do some weird magic here, and it makes some assumptions, but it is pretty flexible.  It will need to be abstracted further though for use in
	other parts of the application.
Need to have a way for it to kick off a process that reads and indexes the entire image tree it is configured to point at.  This would index the metadata from the images,
	and build up a list of terms from the metadata.  This list would be useful to help a user select relevant terms.
	This needs to get kicked off from the configuration initiation.
	At startup:
		If the path is indexed, we need to be able to find out WHEN it was last updated, and feed any changed files in the path hierarchy back to the index again.
			date=beginningoftime
			if alreadyindexed date=lasttimeindexed
			files = files changed since date
			index files
	When a path is added, we need to add the index first.  I have this in name.hampton.mike.gallery.solr.BuildSOLRCore.ensureCore(String)
	We need a way to let the user know what the status of the index is.
		
	
        	
        	
How I have been testing
------------------------ 
Build from eclipse (K:\research\scratchEclipseWorkspace - this is the 'Gallery' project) using maven (mvn clean install)

       	
I have been running this on jetty and Tomcat.  

	Jetty
	-----
	On my machine, I have been using the following
	In a command prompt.
	
	cd /D K:\research\jetty\jetty-distribution-7.4.2.v20110526\bin
	jdk1.7.path.cmd	
	copy /Y K:\research\imageGallery\Gallery\target\Gallery.war K:\research\jetty\jetty-distribution-7.4.2.v20110526\webapps\Gallery.war	
	jetty.cmd
	------	
	Tomcat
	------	
	In a command prompt.
	cd /D K:\apache-tomcat-8.0.12\bin
	start startupRemoteDebug.bat	
	
	Tomcat has a context (K:\apache-tomcat-8.0.12\conf\Catalina\localhost\gallery.xml) that points to the target directory of the deployment, so
	just right-click on the "Gallery" project and select "Run As > Maven Install".  Tomcat will redeploy it in a second or two.  I have also deployed
	the DOJO library in Tomcat.  The context is at K:\apache-tomcat-8.0.12\conf\Catalina\localhost\dojo1.10.0src.xml and dojo is available at 
	http://localhost:8080/dojo1.10.0src.
	
	I just added the user "user" to the tomcat users (K:\apache-tomcat-8.0.12\conf\tomcat-users.xml), and a user named 'galleryAdmin'.	
	------	
	
	*In browser go to http://localhost:8080/gallery, user/password or galleryAdmin/password to log in.
	If the configuration is not set, it should try to take you to http://localhost:8080/gallery/admin/configure.html,
	but you will get a 403 if you are not logged in as a user with the 'galleryAdmin' role.
	click on configure.html or go to http://localhost:8080/gallery/configure.html
	Enter the path to a picture directory for baseDir, and a separate directory for thumbnailDir.
		This will read the basedir, and will write thumbnails to the thumbnaildir
	Go to http://localhost:8080/gallery/index.html to see it work.  The UI is very basic right now.
	

Search. 
	Working on Using SOLR and Apache Tika with the ImageParser/JpegParser class.
	Looks like I can just use the default install, with a specific configuration.
	I set it up on the windows box pretty easily.
	Download solr, http://apache.mirror.anlx.net/lucene/solr/4.10.0/solr-4.10.0.tgz
	I Unzipped/untarred it to K:\solr-4.10.0
	I copied K:\solr-4.10.0\example\solr to K:\research\solr-image-index\solr
	I made the command script to start solr: K:\research\solr-image-index\runSolr.cmd 
		"C:\Progra~1\Java\JDK18~1.0_2\bin\java" -server -Xss256k -Xms512m -Xmx512m -XX:MaxPermSize=256m -XX:PermSize=256m -Duser.timezone=UTC  -Djava.net.preferIPv4Stack=true -Dsolr.autoSoftCommit.maxTime=3000 -XX:-UseSuperWord  -XX:NewRatio=3  -XX:SurvivorRatio=4  -XX:TargetSurvivorRatio=90  -XX:MaxTenuringThreshold=8  -XX:+UseConcMarkSweepGC  -XX:+CMSScavengeBeforeRemark  -XX:PretenureSizeThreshold=64m  -XX:CMSFullGCsBeforeCompaction=1  -XX:+UseCMSInitiatingOccupancyOnly  -XX:CMSInitiatingOccupancyFraction=70  -XX:CMSTriggerPermRatio=80  -XX:CMSMaxAbortablePrecleanTime=6000  -XX:+CMSParallelRemarkEnabled  -XX:+ParallelRefProcEnabled  -XX:+AggressiveOpts -verbose:gc -XX:+PrintHeapAtGC -XX:+PrintGCDetails -XX:+PrintGCDateStamps -XX:+PrintGCTimeStamps -XX:+PrintTenuringDistribution -Xloggc:"K:\research\solr-image-index\logs/solr_gc.log" -Dcom.sun.management.jmxremote -Dcom.sun.management.jmxremote.local.only=false -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.port=1083 -Dcom.sun.management.jmxremote.rmi.port=1083 -DSTOP.PORT=7983 -DSTOP.KEY=solrrocks -Djetty.port=8983 -Dsolr.appDir="K:/solr-4.10.0" -Dsolr.solr.home="K:\research\solr-image-index\solr" -Djetty.home="K:\solr-4.10.0\example" -jar "K:\solr-4.10.0\example\start.jar"  1>"K:\solr-4.10.0\bin\solr-8983-console.log"
		This has the correct paths to use this directory, plus one additional property '-Dsolr.appDir="K:/solr-4.10.0"'.  This is used below.
	I made the command script to stop solr: K:\research\solr-image-index\stopSolr.cmd 
		"C:\Progra~1\Java\JDK18~1.0_2\bin\java" -jar "K:\solr-4.10.0\example\start.jar" STOP.PORT=7983 STOP.KEY=solrrocks --stop
	In K:\research\solr-image-index\solr\collection1\conf\solrconfig.xml I changed directories that referred to the solr installation:
		<lib dir="$../../../contrib/extraction/lib" regex=".*\.jar" />
	  	to refer to the new environment variable I put in the start script
		<lib dir="${solr.appDir}/contrib/extraction/lib" regex=".*\.jar" />
	Also in K:\research\solr-image-index\solr\collection1\conf\solrconfig.xml, I changed the ExtractingRequestHandler slightly.  Still working on this.
      <lst name="defaults">
        <str name="fmap.Last-Modified">last_modified</str>
        <str name="uprefix">ignored_</str>
      </lst>    
	
	At this point I started the server by running "K:\research\solr-image-index\runSolr.cmd"
	In a browser, go to http://localhost:8983/solr for the admin page
	
	I then added a jpeg image with some metadata.	
		curl "http://localhost:8983/solr/update/extract?literal.id=C:\Users\mike.hampton\Pictures\000.jpg&uprefix=attr_&fmap.content=attr_content&commit=true" -F "myfile=@C:\Users\mike.hampton\Pictures\000.jpg"
		This is basically the following:
			curl "<url protocol, host, path to solr extract>?literal.id=<file system path to the file>&&uprefix=attr_&fmap.content=attr_content&commit=true" -F "myfile=@<file system path to the file>"
		
			Then I could search.  For example
			
			Then we can query for a word in the metadata.  In this case, 'John' authored the file:
				http://localhost:8983/solr/select?q=John
			Change the type of the return to JSon
				http://localhost:8983/solr/select?q=John&wt=json
			Search by id
				http://localhost:8983/solr/get?id=C:\Users\mike.hampton\Pictures\000.jpg
				http://localhost:8983/solr/collection1/get?id=C:\Users\mike.hampton\Pictures\000.jpg
			Get the last time any item was updated by sorting by that field, selting only the top row, and only returning the updated field
				I added a field (updated) to the index.  This query will return the last item updated
				http://localhost:8983/solr/C__Users_mike.hampton_Pictures_temp/select?q=*:*&sort=updated%20asc&rows=1&fl=updated
			
			Return only specific fields ( fl={field1,...,fieldN} )
				http://localhost:8983/solr/query?q=author:John Harmon&fl=author,title,id
				http://localhost:8983/solr/collection1/query?q=author:John%20Harmon&fl=author,title,id
			
			Good examples at: http://heliosearch.org/solr/getting-started/	
				also look at http://lucidworks.com/blog/indexing-with-solrj/

	The indexing needs to watch a directory.  It needs to add to the index when files are added, it needs to reindex a file if it is changed, and it needs to delete it from the index if it is removed.
		
		** Add or update a document is the same
		curl "http://localhost:8983/solr/update/extract?literal.id=C:\Users\mike.hampton\Pictures\000.jpg&uprefix=attr_&fmap.content=attr_content&commit=true" -F "myfile=@C:\Users\mike.hampton\Pictures\000.jpg"
		
		** Delete a document by id		
		curl http://localhost:8983/solr/update --data "<delete><query>id:C\:\\Users\\mike.hampton\\Pictures\\000.jpg</query></delete>" -H "Content-type:text/xml; charset=utf-8"
		curl http://localhost:8983/solr/update --data "<commit/>" -H "Content-type:text/xml; charset=utf-8"		    

		**Delete all documents from index
		curl http://localhost:8983/solr/update --data "<delete><query>*:*</query></delete>" -H "Content-type:text/xml; charset=utf-8"
		curl http://localhost:8983/solr/update --data "<commit/>" -H "Content-type:text/xml; charset=utf-8"
		
		So what we need is something that detects changes in the directory.  This would include adding files, changing files, deleting files, adding directories, changing directories, deleting directories.  Looks like the "WatchService API"		    
    
    Initial Indexing.
    	The user configures a directory in the web application, this should fire a new indexing job based on the directory the user selects (the subject of interest)
    	
    	NOTE:  look at http://solr.pl/en/2012/02/20/simple-photo-search/ to see how the solrconfig.xml and schema.xml file must change!
    	I did a lot of work on these two files here.	    
    	 
    	We will need to create a new SOLR 'core' for every unique configuration.  Need a way to 'templatize' this.
    	Here is a try:
    		in K:\research\solr-image-index\solr, I put a zip file - core_template.zip.
    			When we want a new core, unzip this into a new directory - named after the subject/directory somehow at 
    				K:\research\solr-image-index\solr\<name based on FQ directory name>
    				Then add the core using 
    					http://localhost:8983/solr/admin/cores?action=CREATE&name=<name based on FQ directory name>&instanceDir=K:\research\solr-image-index\solr\<name based on FQ directory name>
    					Example:
    						for C:\Users\mike.hampton\Pictures    						
    							http://localhost:8983/solr/admin/cores?action=CREATE&name=C__Users_mike.hampton_Pictures&instanceDir=K:\research\solr-image-index\solr\C__Users_mike.hampton_Pictures
    						or for 'crap'
    							http://localhost:8983/solr/admin/cores?action=CREATE&name=crap&instanceDir=K:\research\solr-image-index\solr\crap
    				This seems to work!
    					Note that this SOLR core does not need to know what the directory that it is indexing information from is, or even what it is exactly indexing.
    	Now that we have a new core, the documents from 'mike's configured directory will be indexed using his path:
		curl "http://localhost:8983/solr/crap/update/extract?literal.id=C:\Users\mike.hampton\Pictures\Mike_SlipperyRockSuperMan.jpg&uprefix=attr_&fmap.content=attr_content&commit=true" -F "myfile=@C:\Users\mike.hampton\Pictures\Mike_SlipperyRockSuperMan.jpg"
		http://localhost:8983/solr/crap/get?id=C:\Users\mike.hampton\Pictures\Mike_SlipperyRockSuperMan.jpg
		curl http://localhost:8983/solr/crap/update --data "<delete><query>id:C\:\\Users\\mike.hampton\\Pictures\\Mike_SlipperyRockSuperMan.jpg</query></delete>" -H "Content-type:text/xml; charset=utf-8"
		curl http://localhost:8983/crap/mike/update --data "<commit/>" -H "Content-type:text/xml; charset=utf-8"
		
		Look at https://wiki.apache.org/solr/CoreAdmin for info on the core admin stuff
	
		So in code...
			When the user saves their configuration, the name.hampton.mike.gallery.servlet.ConfigureServlet gets called.
			This servlet needs to 
				1.	Find out if the passed directory already has a SOLR core.  If it does, we are done.
					Hash the directory to a name.  This needs to be an algorithm that deterministically returns a string from a directory that is unique.
					Can make a REST call to 
						http://localhost:8983/solr/admin/cores?wt=json,  This will give us a list of the existing cores.
						http://localhost:8983/solr/admin/cores?action=status&core=mike, will give the status of a core with the name of 'mike'.
							If the named core does not exist, it will return 
							{"responseHeader":{"status":0,"QTime":1},"initFailures":{},"status":{"mikexxx":{}}}
				2.	If it does not exist, then
					we create the directory for the core in the configured solr directory.  This needs to be set up somehow...
						We will get the configured solr directory by doing
							http://localhost:8983/solr/admin/cores?wt=json
							In the json response, we will get json.status[json.defaultCoreName].instanceDir.  We will use the parent directory of this, calling this the
								'SOLR core root' from this point forward 
						We will call a REST service on the server to 
							create the directory
							unzip the template file into it
								The template file needs to be somewhere.  Rather than configure everything, this will assume the following:
									If there is no default core template file configured, then this will look in the 'SOLR core root' for a file named
									'core_template_conf.zip'.  If not found then it will throw an error stating that this file is not present.
								Once found, the file will be unzipped into the new directory.
							make the call to solr to create the core 
								http://localhost:8983/solr/admin/cores?action=CREATE&name=C__Users_mike.hampton_Pictures&instanceDir=K:\research\solr-image-index\solr\C__Users_mike.hampton_Pictures
							run indexing on the subject directory selected
								This amounts to doing a listing of the files in the directory and calling the equivalent of 
								curl "http://localhost:8983/solr/update/extract?literal.id=C:\Users\mike.hampton\Pictures\000.jpg&uprefix=attr_&fmap.content=attr_content&commit=true" -F "myfile=@C:\Users\mike.hampton\Pictures\000.jpg"
								in Java. 
								Looks to be SolrJ
			Testing this using name.hampton.mike.gallery.BuildSOLRCoreTest.  I am worried that the http interface will take too long.
			
SOLR
	Startup indexing and directory monitoring for reindexing/deletions - done!	


Currently working on:
-----------------------





ToDo
-----------------------
Need to do pagination for the carousel and the thumbnails
Need to build lists of frequent keys, and frequent values for the solr index.  This is probably something to do in tandem with the solr schema.xml

Also try to make use of HTML5 capabilities:
    Drag N Drop
    Web Storage
    App Cache
    Web Workers
    SSE - Server Sent Events!
	