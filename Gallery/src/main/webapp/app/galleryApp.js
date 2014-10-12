require(
		[
			"dojo/store/Cache",
			"dojo/store/Observable",
      		"gallery/GalleryItemStore",
      	    "gallery/ImageSetDisplayPane",
      	    "gallery/ImageSlideShowPane",
			"dijit/Tree",
			"dijit/Dialog",
			"dijit/layout/BorderContainer",
			"dijit/layout/AccordionContainer",
			"dijit/TitlePane",			
			"dijit/layout/ContentPane",
			"dijit/layout/StackContainer",
			"dojo/query",
			"dojo/_base/array",
			"dojo/topic",
			"dijit/form/CheckBox",
			"dijit/form/TextBox",
		    "dojo/io-query",
		    "gallery/SessionUtility",
		    "dijit/form/Button"
        ], 
        function (
        		Cache,
        		Observable,
        		GalleryItemStore, 
        		ImageSetDisplayPane, 
        		ImageSlideShowPane, 
        		Tree, 
        		Dialog, 
        		BorderContainer, 
        		AccordionContainer, 
        		TitlePane, 
        		ContentPane, 
        		StackContainer,
        		query,
        		array,
        		topic,
        		CheckBox,
        		TextBox,
        		ioQuery,
        		SessionUtility,
        		Button) 
        {
			dojo.ready(function(){

				var treeGalleryItemStore = new gallery.GalleryItemStore({
						target:"./rest/search",
						request:{
							query:{
								fields: "dir:true"
							} 
						}
					}
				);
				
				var imageGalleryItemStore =
					//new Observable
					//(
							//new Cache
							//( 
									new gallery.GalleryItemStore
									(
											{
												target:"./rest/search"
											}
									)
							//)
					//)
				;
			
				
				tree = new dijit.Tree({
					model: treeGalleryItemStore
				});
				var topLevelConatiner = new dijit.layout.BorderContainer({
			        style: "height: 100%; width: 100%;",
			        liveSplitters:true
			    });
				topLevelConatiner.addChild(new dijit.layout.ContentPane({
					region:'left',
			        style: "height: 100%; width: 25%;",
					content:tree,
					splitter:true
				}));
				
				//var detailedInfoAndSearch = new dijit.layout.ContentPane({
				var detailedInfoAndSearch = new dijit.layout.BorderContainer({				
			        region: "center",
			        style: "height: 100%; width: 100%;",
					splitter:true
			    });
			    topLevelConatiner.addChild(detailedInfoAndSearch);
			    var searchPanel = new dijit.TitlePane({			    	
			        region: "top",
			        title: "Filter Items"
			        	// ,content: "Include sub directories"
			    });
			    detailedInfoAndSearch.addChild(searchPanel);
			    
			    var seachElements = new dijit.layout.ContentPane({content:"Include sub-directories "});
			    var includeSubDirectoriesCB = new CheckBox({
			        id: "includeSubDirectoriesID",
			        name: "includeSubDirectories",
			        value: "includeSubDirectories",
			        checked: false,
			        onChange: function(b){ 
			        	if(tree.selectedItem)topic.publish("galleryItemSelected", tree.selectedItem);
			        	// alert('onChange called with parameter = ' + b + ', and widget value = ' + this.get('value') ); 
			        }
			    });
			    seachElements.addChild(includeSubDirectoriesCB);
			    // searchPanel.addChild(label);
			    //detailedInfoAndSearch.domNode.appendChild(dojo.create("label", {"for" : "includeSubDirectoriesID", innerHTML: " Include sub-directories"}));
			    var textSearch = new dijit.form.TextBox({
			        id: 		"freeTextID",
			        name: 		"freeText",
			        value: 		"" /* no or empty value! */,
			        placeHolder:"type words to look for",
			        style: 		"width: 50em;",
			        onKeyDown: function(event){
			        	if(event.keyCode == dojo.keys.ENTER){
			        		if(tree.selectedItem)topic.publish("galleryItemSelected", tree.selectedItem);
			        	}
			        }
			    });			    
			    seachElements.addChild(textSearch);
			    var runsearch = new Button({
			        label: "Search",
			        onClick: function(){
			        	if(tree.selectedItem)topic.publish("galleryItemSelected", tree.selectedItem);
			        }
			    });
			    seachElements.addChild(runsearch);
			    
			    searchPanel.addChild(seachElements);
			    
				// In the detailedInfo panel, there might be a single image with
			    //	very detailed information on it, or a collection of images, 
			    //	which is really just detailed information about a folder.
			    // Only one will show at a time
			    var detailedInfo = new StackContainer({
			        region: "center",
			        style: "height: 100%; width: 100%;",
			        id: "myProgStackContainer",
			        doLayout: false
			    });
			    detailedInfoAndSearch.addChild(detailedInfo);

			    var itemSlideShow = new gallery.ImageSlideShowPane({
			        //region: "center",
			    	id: "slideShowID",
			        title: "Slide Show",
			        style: "height: 100%; width: 100%;"
			    });
			    detailedInfo.addChild(itemSlideShow);

			    
			    var containerDetailedInfo = new gallery.ImageSetDisplayPane({
			        //region: "center",
			    	id: "containerDetailsID",
			        title: "Folder Details"
			    });
			    detailedInfo.addChild(containerDetailedInfo);

				dojo.connect(tree, "onClick", function(object){
					topic.publish("galleryItemSelected", object);					
		        });				

				var sessionUtility = gallery.SessionUtility();
				// This listens to ALL view changes.  put here to track SwapView changes...
				// dojo.subscribe("/dojox/mobile/viewChanged", function(view){ console.log(view) });
				var galleryItemSelected = function(object){
					if(object.allowsChildren)
					{
						var hasQueryParams=false;
						var queryParams = {};
						if(includeSubDirectoriesCB.checked){
							hasQueryParams=true;
							queryParams["subdirectory"]=true;
						}
						var freeText = textSearch.get('value');
						if(freeText!=""){
							hasQueryParams=true;
							queryParams["freeText"]=freeText;
						}
						
						// Build the rest of the queryParams here.

						var theQuery = "";
						if(hasQueryParams){
							theQuery = "?"; 
							theQuery += ioQuery.objectToQuery(queryParams);
						}
						var queryPath = (object.path=="")?"":"/" + object.path;
						deferred = imageGalleryItemStore.query(queryPath + theQuery);
						
						var onComplete = function(children){containerDetailedInfo.displayChildren(children)}
						var onError = function(error){
	        				var dialog = new dijit.Dialog({title: 'Error', content: myDeferred.ioArgs.xhr.responseText});
	        				dialog.show();
						}						
						// This is what happens if the request is successful.
						var getChildrenFunction = function(fullObject){
							// copy to the original object so it has the children array as well.
							// NOT doing this right now because the object passed is in the tree, and we do not want the children to be added in this way.
							// object.children = fullObject.children;
							var numberOfChildren=fullObject.children.length;
							for(var idx=0;idx<numberOfChildren;idx++){
								// All these are overkill, but I am using the kitchen sink here.
								fullObject.children[idx].sourceQueryPath=queryPath;  //The query that produced this object (and siblings in the collection) 
								fullObject.children[idx].sourceQueryParams=queryParams;  //The query that produced this object (and siblings in the collection) 
								fullObject.children[idx].parent=fullObject; // Try to always have the parent in here, it should be possible for all but the root
								fullObject.children[idx].collectionReference=fullObject.children; // Try to have the reference to the collection this is in in her.  
									// this may not be its parents children - this could be the result of a search where the members of the collection have
									// different parents
							}
							onComplete(fullObject.children);
							
						};
						var getChildrenErrorFunction = sessionUtility.buildOnError(deferred, getChildrenFunction, onError);							
						deferred.then(getChildrenFunction, getChildrenErrorFunction);
						
						detailedInfo.selectChild(containerDetailedInfo);
					}
					else
					{
						var storeUri = treeGalleryItemStore.target;
						var queryParams = {};
						if(null != object.sourceQueryPath)
						{
							storeUri += object.sourceQueryPath;
						}
						else
						{
							storeUri += (object.parent.path=="")?"":"/" + object.parent.path;
						}
						if(null != object.sourceQueryParams)
						{
							queryParams = object.sourceQueryParams;
						}
						itemSlideShow.setGalleryItem(object, storeUri, queryParams);
					    detailedInfo.selectChild(itemSlideShow);
						
					}
				}
				
				topic.subscribe("galleryItemSelected", galleryItemSelected);
			    
			    // put the top level widget into the document, and then call startup()
			    topLevelConatiner.placeAt(document.body);
			    topLevelConatiner.startup();
			});
        }
);
