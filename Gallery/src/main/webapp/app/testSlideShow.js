require(
		[
      	    "gallery/ImageSlideShowPane"
        ], 
        function ( ImageSlideShowPane ) 
        {
			dojo.ready(function(){
			    var itemSlideShow = new gallery.ImageSlideShowPane({
			        //region: "center",
			    	id: "slideShowID",
			        title: "Slide Show"
			    });
			    itemSlideShow.placeAt(document.body);
			    itemSlideShow.startup();
			});
        }
);
