require(
		[
		    'dojo/_base/declare'
		],
		function(declare) {
			// Use a global memory space to hold onto the types.
			var global = this;
			global._imageTypes={};
			declare(
					/**
					 * Simple class used to load image types.  It uses a globel memory space to keep the
					 * types, because they are essentially static.  This prevents repeated calls in the case where this
					 * class is instantiated in different places.  It allows the app to not care where the instance is. 
					 * 
					 */
					"gallery.ImageTypes",
					null,
					{
						url: "./rest/imageUtil/imageTypes",
						imageTypes: function(dataLoadedCallback, errorCallback) {
							// Grab the list of types that have generated thumbnails.  Knowing what these are helps minimize
							// the case where the browser loads the same image under a different url, eating up memory.
							var me = this;
							if(null==global._imageTypes[this.url] || typeof global._imageTypes[this.url] == 'undefined'){
								var xhrArgs = {
								    url: this.url,
								    handleAs: "json",
								    load: function(data){
								    	global._imageTypes[me.url] = [];
								    	// convert all to lower case.  We need to be able to match them.
								    	for (var i = 0; i < data.length; i++) {
								    		global._imageTypes[this.url].push(data[i].toLowerCase())
								    	}
								    	if(dataLoadedCallback)dataLoadedCallback(global._imageTypes[me.url]);
								    },
								    error: function(error){
								    	if(errorCallback)errorCallback(error);
								    	console.error(error);
								    }
								}
								// Call the asynchronous xhrGet
								var deferred = dojo.xhrGet(xhrArgs);
							}
							else if(dataLoadedCallback)dataLoadedCallback(global._imageTypes[me.url]);
						}
					}
			);
		}
);