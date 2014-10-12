define([
    // The gallery/SearchCriteria module is required by this module, so it goes
    // in this list of dependencies.
    'dojo/_base/lang',
    'dojo/_base/declare',
    'dojo/io-query',
    'dojo/store/JsonRest',
    'dojo/request',
    'dojo/json'
], function(
		lang, 
		declare, 
		ioQuery, 
		JsonRest, 
		request, 
		json){	
	return declare("gallery.GalleryItemStore", [JsonRest], {

		constructor: function(options){
			// options:
			//		The configuration information to pass into the data store.
			//
			lang.mixin(this, options);
		},
		mayHaveChildren: function(object){
			// see if it has a children property
			return ("allowsChildren" in object) && (object.allowsChildren);
		},
		getChildren: function(object, onComplete, onError ){
			// retrieve the full copy of the object
			// http://localhost:8080/gallery/rest/search?directory=/xmen
			// deferred = this.get(object.path);
			var theQuery = this.request?(this.request.query?"?" + ioQuery.objectToQuery(this.request.query):""):"";
			deferred = this.query("/" + object.path + theQuery);
			
			// This is what happens if the request is successful.
			var getChildrenFunction = function(fullObject){
				// copy to the original object so it has the children array as well.
				var numberOfChildren=fullObject.children.length;
				for(var idx=0;idx<numberOfChildren;idx++){
					fullObject.children[idx].parent=fullObject; // Try to always have the parent in here, it should be possible for all but the root
					fullObject.children[idx].collectionReference=fullObject.children; // Try to have the reference to the collection this is in in her.  
						// this may not be its parents children - this could be the result of a search where the members of the collection have
						// different parents
				}
				object.children = fullObject.children;
				onComplete(fullObject.children);
			};
			var getChildrenErrorFunction = buildOnError(deferred, getChildrenFunction, onError);							
			deferred.then(getChildrenFunction, getChildrenErrorFunction);
		},
		getRoot: function(onItem, onError){
			// get the root object, we will do a get() and callback the result
			// this.get(this.request.query.path).then(onItem, onError);
			var theQuery = this.request?(this.request.query?"?" + ioQuery.objectToQuery(this.request.query):""):"";
			this.query(theQuery).then(onItem, onError);
		},
		getLabel: function(object){
			// just get the name
			return object.name;
		},
		put: function(object, options){
			//this.onChange(object);
			this.onChildrenChange(object, object.children);
			this.onChange(object);
			return dojo.store.JsonRest.prototype.put.apply(this, arguments);
		}
    });
});

//------------------------------------------------------------
// 	This is the beginning of a more generic way to deal with session timeouts
//	in an application that uses form-based authentication.  This currently makes the 
//	assumption that the data returned will need to be handles as 'json'.
//
//	'deferred' - the Promise object returned by the dojo.store.JsonRest.get command, and possibly others...
//	'dataCallbackFunction' - the function that will be called after authentication succeeds
//
// 	It also makes the following assumptions about the login page:
// 	It contains the string '3ff0f1a0-3f31-11e4-916c-0800200c9a66'
//	The login form is named 'myform'
function buildOnError(deferred, dataCallbackFunction, onError)
{
	return function(error){
		var myDeferred = deferred;
		// If the session timed out, detect it.
		// 200 is an "OK" response, but it is what comes back for the
		// form based authentication.
		if(200 == myDeferred.ioArgs.xhr.status)
		{
			// Might be a session timeout.  Will check the response text for
			// a substring that should only occur in the login challenge.
			// the value '3ff0f1a0-3f31-11e4-916c-0800200c9a66' is in the login page.
			if(myDeferred.ioArgs.xhr.responseText.indexOf('3ff0f1a0-3f31-11e4-916c-0800200c9a66') > -1)
			{
				// Create a Dialog to hold the login page.  This will make the dom element
				//	'myform' available to be modified.
        		var dialog = new dijit.Dialog({title: 'Session Timed Out', content: myDeferred.ioArgs.xhr.responseText});
        		
				// Rewire the 'onSubmit' to execute asynch, and take the output back to passed function
    			function sendForm(dataCallback){
					// This is the name of the form in the login page.
  				  	var form = dojo.byId("myform");
  				  	dojo.connect(form, "onsubmit", 
  				  		function(event){
  				    		// Stop the submit event since we want to control form submission.
  				    		dojo.stopEvent(event);

  				    		// The parameters to pass to xhrPost, the form, how to handle it, and the callbacks.
  				    		// Note that there isn't a url passed.  xhrPost will extract the url to call from the form's
  				    		//'action' attribute.  
  				    		var xhrArgs = {
  				      			form: dojo.byId("myform"),
  				      			handleAs: "json",
  				      			load: function(data){
  				      				dataCallback(data);
  				        			dojo.byId("response").innerHTML = "Login success.";
  				    				dialog.destroy();
  				      			},
  				      			error: onError
  							}
  							// Call the asynchronous xhrPost
  							dojo.byId("response").innerHTML = "Sending login request..."
  				    		var deferred = dojo.xhrPost(xhrArgs);
  				  		}
  				  	);
  				}
				// Run the function above.  why is it doing things this way?  Because this will 
				// almost definately be needed elsewhere, so trying to make it generic for later
				// refactoring.
    			sendForm(dataCallbackFunction);
        		
        		dialog.show();
			}
		}		
		else onError(error);
		/*
		{										
			var dialog = new dijit.Dialog({title: 'Error', content: myDeferred.ioArgs.xhr.responseText});
			dialog.show();
			// an error occurred, log it, and indicate no children
			console.error(error);
			// onComplete([]);
		}
		*/	        		
	};
}
