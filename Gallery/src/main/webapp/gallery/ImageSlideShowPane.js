require(
		[ 
		 	"dojo/_base/array",
		  	"dojo/_base/declare", 
		    "dojo/io-query",
		    "gallery/GalleryCarousel",
		    "dijit/layout/ContentPane",
		    "gallery/GalleryItemStore"
		],
		function(
				array,
				declare, 
				ioQuery,
				GalleryCarousel, 
				ContentPane, 
				GalleryItemStore) {

			declare(
					"gallery.ImageSlideShowPane",
					[ ContentPane ],
					{
						currentGalleryItem:null,
						
						setGalleryItem : function(galleryItem, storeUri, query) {		
							array.forEach(this.getChildren(), function(child){
								if(child instanceof gallery.GalleryCarousel){
									child.destroyRecursive();
								}
							});
							
							this.currentGalleryItem=galleryItem;
							var sampleStore = 
								new gallery.GalleryItemStore({
									target: storeUri,
									idProperty: "path",
									request:{
										query:{
											path:""
										} 
									}
								});
								
							// The 'flatten' paramater is critical, without it the returned structure does not work.
							// query: "?subdirectory=true&flatten=true&fields=content_type:image/*"
							query["flatten"]=true;
							query["fields"]="content_type:image/*";
							var theQuery = "?" + ioQuery.objectToQuery(query);
							
							var carousel = new gallery.GalleryCarousel({
								numVisible: 1,
								title: "Image View",
								//pageIndicator: true,
								pageIndicator: false,
								navButton: true,
								height: 'inherit',
								//selectable: true,
								selectable: false,
								store: sampleStore,
								query: theQuery,
								initialGalleryItem: galleryItem
							});
							
							this.addChild(carousel);
						}
					}
			);
		}
);
							
