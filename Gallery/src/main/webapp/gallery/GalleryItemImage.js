require(
		[
		  "dojo/_base/declare",
		  "dojo/dom-construct",
		  "dojo/parser", 
		  "dojo/ready",
		  "dijit/_WidgetBase", 
		  "dojo/dom-geometry",
		  "dojo/topic"
		],
		function(declare, domConstruct, parser, ready, _WidgetBase, domGeometry,topic) {

			declare(
					"gallery.GalleryItemImage",
					[ _WidgetBase ],
					{
						_defaultlClass: 'thumbnailImage',
						_baseImageUrl: './thumbnail/',
						// user data
						galleryItem: null,

						constructor: function(params, srcNodeRef){							
						    if(!params.title)params.title="thumbnail";
						    if(!params.alt)params.alt="thumbnail";
						    if(!params.src)params.src=require.toUrl("dojo/resources/blank.gif");						    
						    if(params.defaultlClass)this._defaultlClass= params.defaultlClass;
						    if(params.baseImageUrl)this._baseImageUrl= params.baseImageUrl;
						},

						buildRendering : function() {
							this.domNode = domConstruct.create("img", {
								class: this._defaultlClass
							});
						},
						
						setGalleryItemAttr: function(value){
							this.galleryItem = value;
							this.domNode.title=this.galleryItem.name;
							this.domNode.alt=this.galleryItem.name;
							this.domNode.src=this._baseImageUrl + this.galleryItem.path;
						},

						postCreate : function() {
							// Is this in the viewport?
							// var positionInViewport = domGeometry.position(this.domNode);
							// every time the user clicks the button, increment the counter
							this.connect(this.domNode, "onclick", "imageClicked");
							if(null!=this.galleryItem)this.setGalleryItemAttr(this.galleryItem);
						},
						
						imageClicked: function(){
							topic.publish("galleryItemSelected", this.galleryItem);
							// console.log(this.galleryItem);
				        }						
			});

			ready(function() {
				parser.parse();
			});
		});