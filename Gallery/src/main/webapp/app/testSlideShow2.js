require(
		[
      	    "gallery/ImageSlideShowPane2"
        ], 
        function ( ImageSlideShowPane2 ) 
        {
			dojo.ready(function(){
			    var itemSlideShow = new gallery.ImageSlideShowPane2({
			        //region: "center",
			    	id: "slideShowID",
			        title: "Slide Show"
			    });
			    itemSlideShow.placeAt(document.body);
			    itemSlideShow.startup();
			});
        }
);
