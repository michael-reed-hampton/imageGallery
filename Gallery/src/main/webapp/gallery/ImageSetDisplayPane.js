require(
		[ 
		  "dojo/_base/declare", 
		  "dojo/ready", 
		  "dojo/parser", 
		  "dijit/layout/ContentPane", 
   		  "gallery/GalleryItemImage",
		  "gallery/ImageTypes"
		],
		function(declare, ready, parser, ContentPane, GalleryItemImage, ImageTypes) {

			declare(
					"gallery.ImageSetDisplayPane",
					[ ContentPane ],
					{
						imageTypes : null,

						displayChildren : function(imageSet) {
							var me = this;
							if(null == this.imageTypes)this.imageTypes = new gallery.ImageTypes();
							this.imageTypes.imageTypes(
								function(data) {
									var imageTypes = data;
									var itemIndex = 0;
									// Get the items currently inside this
									var oldChildArray = me.getChildren();

									for (var idx = 0; idx < imageSet.length; idx++) {
										var child = imageSet[idx];
										// The problem with this is that all imageSet have their own url to their thumbnail,
										// and this prevents the browser from cacheing effectively...
										// html += '<img src="./thumbnail'+child.path+'"></img>'
										// So we need to be intelligent based on the 'type'.
										//
										// Where should this intelligence lie? What should determine what the item icon is?
										// If it lies on the server, then the browser is crippled, and we have to transfer more
										// data. Do it here, but abstract it out to a common area. This should be in a single
										// script.
										//
										// Check the type - lowercase it to match the type array

										// var thumbnailUrl = './thumbnail';

										// For now only displaying Images as thumbnails
										if (-1 != imageTypes
												.indexOf(child.type
														.toLowerCase())) {
											var widget = oldChildArray[itemIndex];
											if(widget){
												// If the widget exists, just set the data on it
												me.setGalleryItemOnWidget(widget, child, idx);
											} else {
												me.addGalleryItem(child, idx);
											}
											itemIndex++;
										}
									}
									// We did not set all the imageSet.
									// Either hide or remove those remaining...
									var numChildren = oldChildArray.length;
									if (itemIndex < numChildren) {
										// Need to make sure the array length does not change during the loop.
										for (var idx = itemIndex; itemIndex < numChildren; itemIndex++) {
											var thumbnailToDestroy = oldChildArray[itemIndex];
											// need to make sure this is the ThumbnailSheet
											me.removeChild(thumbnailToDestroy);
											thumbnailToDestroy.destroy();
										}
									}
								}, 
								function(error) {
									var dialog = new dijit.Dialog({
										title : 'Error',
										content : error
									});
									dialog.show();
								}
							);
						}, // end displayChildren
						
						// By default, it is just the item passed.
						// It is important to note that childItem could be null!							
						// Returning null is fine, a new item should be created is null is returned.
						//
						// The idea here is that this might return a different widget that we key to 
						// using the gallery item.  This needs to work in concert with the 'addGalleryItem'
						// function.  EX: if the 'addGalleryItem' adds a galleryItem by creating a FooPanel that contains
						// a GalleryItemImage, then a FooPanel will be passed to this function, and this function should
						// extract he GalleryItemImage from it and return it.
						getGalleryItemImage: function( childItem ){
							return childItem;
						},
						
						setGalleryItemOnWidget: function(widget, galleryItem, idx){
							var oldImage = this.getGalleryItemImage( widget );											
							if (oldImage) {
								oldImage.setGalleryItemAttr(galleryItem);
							}
						},
						
						// The idx is passed for usage in subclasses, it might be useful.
						addGalleryItem: function(galleryItem, idx){
							// By default, just add the item passed.
							var galleryItemImage = new gallery.GalleryItemImage({galleryItem : galleryItem});												
							this.addChild(galleryItemImage);
						}
					}
			);

			ready(function() {
				parser.parse();
			});
		});