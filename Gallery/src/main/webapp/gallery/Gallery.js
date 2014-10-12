require
(
		[
		 	"dojo/_base/declare",
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
			"dijit/form/CheckBox"
        ], 
        function
        (
        		declare,
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
        		CheckBox
        ) 
        {
			declare
			( 
					"gallery.Gallery", 
					[BorderContainer], 
					{						
						postCreate: function()
						{
							var treeGalleryItemStore = new gallery.GalleryItemStore
							(
								{
									target:"./rest/search",
									request:
									{
										query:
										{
											fields: "dir:true"
										} 
									}
								}
							);
							
							var imageGalleryItemStore =
								new gallery.GalleryItemStore
								(
										{
											target:"./rest/search"
										}
								)
							;
						
							
							tree = new dijit.Tree({ model: treeGalleryItemStore });
							
							var topLevelConatiner = this;/* new dijit.layout.BorderContainer({
						        style: "height: 100%; width: 100%;",
						        liveSplitters:true
						    }); */
							
							topLevelConatiner.addChild(new dijit.layout.ContentPane({
								region:'left',
						        style: "height: 100%; width: 25%;",
								content:tree,
								splitter:true
							}));
							
							var detailedInfoAndSearch = new dijit.layout.BorderContainer({				
						        region: "center",
						        style: "height: 100%; width: 100%;",
								splitter:true
						    });
						    topLevelConatiner.addChild(detailedInfoAndSearch);

						    var searchPanel = new dijit.TitlePane({			    	
						        region: "top",
						        title: "Filter Items",
						        content: "This is where the search widget should go"
						    });
						    detailedInfoAndSearch.addChild(searchPanel);
						    
						    var includeSubDirectories = new CheckBox({
						        name: "includeSubDirectories",
						        value: "includeSubDirectories",
						        checked: false,
						        onChange: function(b)
						        { 
						        	alert('onChange called with parameter = ' + b + ', and widget value = ' + this.get('value') ); 
						        }
						    });
						    searchPanel.addChild(includeSubDirectories);
						    
						    
							// In the detailedInfo panel, there might be a single image with
						    // very detailed information on it, or a collection of images,
						    // which is really just detailed information about a folder.
						    // Only one will show at a time
						    var detailedInfo = new StackContainer({
						        region: "center",
						        style: "height: 100%; width: 100%;",
						        id: "myProgStackContainer",
						        doLayout: false
						    });
						    detailedInfoAndSearch.addChild(detailedInfo);
	
						    var itemSlideShow = new gallery.ImageSlideShowPane({
						        // region: "center",
						    	id: "slideShowID",
						        title: "Slide Show",
						        style: "height: 100%; width: 100%;"
						    });
						    detailedInfo.addChild(itemSlideShow);
	
						    var containerDetailedInfo = new gallery.ImageSetDisplayPane({
						        // region: "center",
						    	id: "containerDetailsID",
						        title: "Folder Details"
						    });
						    detailedInfo.addChild(containerDetailedInfo);
	
							dojo.connect(tree, "onClick", function(object){
								topic.publish("galleryItemSelected", object);					
					        });				
							
									
							var galleryItemSelected = function(object)
							{
								if(object.allowsChildren)
								{
									imageGalleryItemStore.getChildren
									(
										{path:object.path},
										function(children){containerDetailedInfo.displayChildren(children)}, 
										function(error)
										{
					        				var dialog = new dijit.Dialog({title: 'Error', content: myDeferred.ioArgs.xhr.responseText});
					        				dialog.show();
										}
									);
									detailedInfo.selectChild(containerDetailedInfo);
								}
								else
								{
									var storeUri = treeGalleryItemStore.target+"/" + object.parent.path;
									var query = [];
									itemSlideShow.setGalleryItem(object, storeUri, query);
								    detailedInfo.selectChild(itemSlideShow);
									
								}
							}
			
							topic.subscribe("galleryItemSelected", galleryItemSelected ); 
						}
					}
			);
        }
);
