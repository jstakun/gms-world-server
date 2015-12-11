var
	marker = {
		icon: {
			url: '/images/layers/0stars_blue.png',
			size: { x: 48, y: 48 }
		},
		mobile: {
			url: '/images/layers/0stars_blue.png',
			size: { x: 72, y: 72 }
		}
	};
	
var Marker = function(options){

	google.maps.Marker.apply(this, arguments);

	if (options.price && options.cc) {
		this.MarkerLabel = new MarkerLabel({
			map: this.map,
			marker: this,
			text: options.price + ' ' + options.cc,
			mobile: options.mobile
		});
		this.MarkerLabel.bindTo('position', this, 'position');
	}
};

Marker.prototype = $.extend(new google.maps.Marker(), {
	// If we're adding/removing the marker from the map, we need to do the same for the marker label overlay
	setMap: function(){
		google.maps.Marker.prototype.setMap.apply(this, arguments);
		(this.MarkerLabel) && this.MarkerLabel.setMap.apply(this.MarkerLabel, arguments);
	}
});

//marker label overlay
var MarkerLabel = function(options) {

	var self = this;

	this.setValues(options);
	
	this.span = document.createElement('span');
	
	var size = 'top: -73px;font-size: 16px;'
	if (options.mobile == true) {
		size = 'top: -81px;font-size: 24px;'
	}
	
	this.span.style.cssText = 'position: relative; left: -50%; white-space: nowrap; border: 2px solid black;' +
	                     'padding: 2px; background-color: white; color:green; font-weight: bold; font-family:Roboto,Arial,sans-serif;' + size;

	// Create the label container
	this.div = document.createElement('div');
	this.div.appendChild(this.span);
	this.div.style.cssText = 'position: absolute; display: none';

	// Trigger the marker click handler if clicking on the label
	google.maps.event.addDomListener(this.div, 'click', function(e){
		(e.stopPropagation) && e.stopPropagation();
		google.maps.event.trigger(self.marker, 'click');
	});
};

MarkerLabel.prototype = $.extend(new google.maps.OverlayView(), {
	onAdd: function() {
		this.getPanes().overlayImage.appendChild(this.div);

		// Ensures the label is redrawn if the text or position is changed.
		var self = this;
		this.listeners = [
			google.maps.event.addListener(this, 'position_changed', function() { self.draw(); }),
			google.maps.event.addListener(this, 'text_changed', function() { self.draw(); }),
			google.maps.event.addListener(this, 'zindex_changed', function() { self.draw(); })
		];
	},
	onRemove: function() {
		this.div.parentNode.removeChild(this.div);
		// Label is removed from the map, stop updating its position/text
		for (var i = 0, l = this.listeners.length; i < l; ++i) {
			google.maps.event.removeListener(this.listeners[i]);
		}
	},
	draw: function() {
		var
			text = String(this.get('text')),
			markerSize = marker.icon.size,
			projection = this.getProjection(),
		    position = projection.fromLatLngToDivPixel(this.get('position'));
			
		
		if (this.get('mobile') == true) {
			markerSize = marker.mobile.size;
		} 
		
		this.div.style.left = position.x + 'px';
		this.div.style.top = position.y + 'px';
		this.div.style.display = 'block';
			
		this.span.innerHTML = text;
	}
});