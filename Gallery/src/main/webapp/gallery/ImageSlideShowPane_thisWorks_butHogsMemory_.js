require(
		[ 
		  "dojo/_base/declare", 
		  "dojo/ready", 
		  "dojo/parser",
		  "dojo/aspect", 
		  "dijit/registry",
		  "dijit/layout/ContentPane",
		  "dojo/store/Memory",
		  "dojox/mobile/SwapView",
		  "gallery/ImageSetDisplayPane"
		],
		function(declare, ready, parser, aspect, registry, ContentPane, Memory, SwapView, ImageSetDisplayPane) {

			declare(
					"gallery.ImageSlideShowPane",
					[ gallery.ImageSetDisplayPane ],
					{
						currentGalleryItem: null,
						
						setGalleryItem : function(galleryItem) {							
							this.currentGalleryItem=galleryItem;
							this.displayChildren(galleryItem.collectionReference);
						},
						
						// @Override
						getGalleryItemImage: function( swapView ){
							// It is important to note that childItem could be null!							
							// Returning null is fine, a new item should be created is null is returned.
							//
							var galleryItemImage = null;
							if(swapView){
								var swapViewChildArray = swapView.getChildren();
								galleryItemImage = swapViewChildArray[0];
							}
							return galleryItemImage;
						},
						
						setGalleryItemOnWidget: function(swapView, galleryItem, idx){
							swapView.selected = (this.currentGalleryItem===galleryItem);
							var oldImage = this.getGalleryItemImage( swapView );											
							if (oldImage) {
								oldImage.setGalleryItemAttr(galleryItem);
							}
							if(swapView.selected)swapView.show();				
						},
						
						// @Override
						addGalleryItem: function(galleryItem, idx){
							var isSelected = (this.currentGalleryItem===galleryItem);
							var swapView=new dojox.mobile.SwapView({        
	                            id:"swapViewId_"+idx,
	                            selected: isSelected
	                        });
							var galleryItemImage = new gallery.GalleryItemImagePane({
							    thumbnailClass:"fullImage",
							    baseThumbnailUrl:"./image",
							    galleryItem : galleryItem
							});
							swapView.addChild(galleryItemImage);
							this.addChild(swapView);
							if(isSelected)swapView.show();
						}					
					}
			);
		}
);
							
