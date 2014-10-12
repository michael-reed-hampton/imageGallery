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
					"gallery.ThumbnailSheet",
					[ ContentPane ],
					{
						imageTypes : null,

						displayChildren : function(children) {
							var me = this;
							if(null == this.imageTypes)this.imageTypes = new gallery.ImageTypes();
							this.imageTypes.imageTypes(
								function(data) {
									var imageTypes = data;
									var thumbNailIndex = 0;
									// need to make sure this is the ThumbnailSheet
									var oldChildArray = me.getChildren();

									for (var idx = 0; idx < children.length; idx++) {
										var child = children[idx];
										if ('folder' != child.type) {
											// The problem with this is that all children have their own url to their thumbnail,
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
												var thumbnail = oldChildArray[thumbNailIndex];
												if (thumbnail) {
													thumbnail.setGalleryItemAttr(child);
												} else {
													// need to make sure this is the ThumbnailSheet
													var galleryItemThumbnail = new gallery.GalleryItemImage({galleryItem : child});
													me.addChild(galleryItemThumbnail);
												}
												thumbNailIndex++;
											}
										}
									}
									// We did not set all the children.
									// Either hide or remove those remaining...
									var numChildren = oldChildArray.length;
									if (thumbNailIndex < numChildren) {
										// Need to make sure the array length does not change during the loop.
										for (var idx = thumbNailIndex; thumbNailIndex < numChildren; thumbNailIndex++) {
											var thumbnailToDestroy = oldChildArray[thumbNailIndex];
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
						} // end displayChildren
					}
			);

			ready(function() {
				parser.parse();
			});
		});