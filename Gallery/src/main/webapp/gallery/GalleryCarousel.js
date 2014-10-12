require(
		[ 
		 	"dojo/_base/array",
		  	"dojo/_base/declare", 
		    "dojox/mobile/StoreCarousel",
		  	"dojox/mobile/SwapView"
		],
		function(
				array,
				declare, 
				StoreCarousel,
				SwapView) {

			return declare(
					"gallery.GalleryCarousel",
					[ StoreCarousel ],
					{
						initialGalleryItem: null,
						
						// Probably want a different way to map items
						// objects forthis need the following:
						//	"alt", "src", "headerText", "footerText", and if it has a "type" it will be used to create a widget
						
						onComplete: function(/*Array*/items){
							var selectedIdx = 0; 
							var numberOfChildren = items.length;
							for(var idx=0;idx<numberOfChildren;idx++){
								
								// Obviously a very specific mapping of values.
								items[idx]["alt"] = items[idx]["name"]; 
								items[idx]["headerText"] = items[idx]["name"]; 
								items[idx]["footerText"] = items[idx]["name"]; 
								items[idx]["type"] = null; 
	
								items[idx]["src"] = "./image/" + items[idx]["path"];
								
								if(this.initialGalleryItem.path == items[idx]["path"]){
									// selectedIdx = idx;
									this.selectedItemIndex = idx;
								}
							}
//							// Cool but a little irritating.  With the block below, this will
							// make each view swap out until it gets to the correct one.  This only works if you 
							// do NOT set 'this.selectedItemIndex' above 
//							if(0<selectedIdx){
//								// This listens to ALL view changes.  put here to track SwapView changes...
//								var handle = dojo.subscribe("/dojox/mobile/viewChanged", function(view)
//										{ 
//											if(0<selectedIdx){
//												selectedIdx--
//												view.goTo(1);
//											}
//											else dojo.unsubscribe(handle);											
//										}
//								);
//							}
							this.inherited(arguments, [items]);
//							if(0<selectedIdx && this.currentView){
//							selectedIdx--;
//							this.currentView.goTo(1);
						}
					}
			);
		}
);
							
