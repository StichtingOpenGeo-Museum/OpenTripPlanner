/* This program is free software: you can redistribute it and/or
   modify it under the terms of the GNU Lesser General Public License
   as published by the Free Software Foundation, either version 3 of
   the License, or (at your option) any later version.
   
   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
   
   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>. 
*/

otp.namespace("otp.core");

otp.core.Map = otp.Class({

    webapp          : null,

    lmap            : null,
    layerControl    : null,
    
    contextMenu             : null,
    contextMenuModuleItems  : null,
    contextMenuLatLng       : null,
    
    initialize : function(webapp) {
        var this_ = this;
        this.webapp = webapp;
        
        var tileLayer = new L.TileLayer(otp.config.tileUrl, {attribution: otp.config.tileAttrib});
	    
	    if(typeof otp.config.getTileUrl != 'undefined') {
    	    tileLayer.getTileUrl = otp.config.getTileUrl;
        }
        
        var mapProps = { 
            layers  : [ tileLayer ],
            center : (otp.config.initLatLng || new L.LatLng(0,0)),
            zoom : (otp.config.initZoom || 2),
            zoomControl : false
        }
        if(otp.config.minZoom) _.extend(mapProps, { minZoom : otp.config.minZoom });
        if(otp.config.maxZoom) _.extend(mapProps, { maxZoom : otp.config.maxZoom });

        this.lmap = new L.Map('map', mapProps);

        this.lmap.addControl(new L.Control.Zoom({ position : 'topright' }));
        
        if(!otp.config.initLatLng) {
            console.log("no initLL, reading metadata");
            var url = otp.config.hostname + '/opentripplanner-api-webapp/ws/metadata';
            $.ajax(url, {
                data: { routerId : otp.config.routerId },            
                dataType:   'jsonp',
                
                success: function(data) {
                    console.log(data);
                    this_.lmap.fitBounds([
                        [data.lowerLeftLatitude, data.lowerLeftLongitude],
                        [data.upperRightLatitude, data.upperRightLongitude]
                    ]);
                }
            });
        }
       

        /*var baseMaps = {
            'Base Layer' : tileLayer 
        };*/
        
        var overlays = { };
        
        if(typeof otp.config.overlayTileUrl != 'undefined') {
	    	var overlayTileLayer = new L.TileLayer(otp.config.overlayTileUrl);
	    	//this.lmap.addLayer(overlayTileLayer);
	    	//overlays['Overlay'] = overlayTileLayer;
        }
        
        //this.layerControl = new L.Control.Layers(baseMaps, overlays);
        //this.layerControl.addTo(this.lmap);
        
        this.lmap.on('click', function(event) {
            webapp.mapClicked(event);        
        });
        
        // setup context menu
        var this_ = this;
        this.contextMenu = $('<div id="otp-map-contextMenu"></div>');
        
        this.contextMenuModuleItems = $('<div style="border-bottom: 1px solid #ccc;"></div>').appendTo(this.contextMenu);
        
        $('<div class="otp-map-contextMenu-item">Recenter Map Here</div>')
        .appendTo(this.contextMenu).click(function(evt) {
            this_.lmap.panTo(this_.contextMenuLatLng);
        });
        $('<div class="otp-map-contextMenu-item">Zoom In</div>')
        .appendTo(this.contextMenu).click(function(evt) {
            this_.lmap.zoomIn();
        });
        $('<div class="otp-map-contextMenu-item">Zoom Out</div>')
        .appendTo(this.contextMenu).click(function(evt) {
            this_.lmap.zoomOut();
        });
                        
        this.lmap.on('contextmenu', function(event) {
            this_.contextMenu.show();
            this_.contextMenu.offset({ 
                top: event.containerPoint.y + $('#map').offset().top,
                left: event.containerPoint.x
            }).appendTo("body");//$('#map'));
            this_.contextMenuLatLng = event.latlng;
        });
        
        $(document).bind("click", function(event) {
            this_.contextMenu.hide();
        });                 
        this.activated = true;        
    },
    
    addContextMenuItem : function(text, clickHandler) {
        var this_ = this;
        console.log("adding cm: "+text);
        $('<div class="otp-map-contextMenu-item">'+text+'</div>')
        .appendTo($(this_.contextMenuModuleItems))
        .click(function(evt) {
            clickHandler.call(this, this_.contextMenuLatLng);
        });
    },
    
    activeModuleChanged : function(oldModule, newModule) {
        
        //console.log("actModChanged: "+oldModule+", "+newModule);
        if(oldModule != null) {
            for(var layerName in oldModule.mapLayers) {
                
                var layer = oldModule.mapLayers[layerName];
                this.lmap.removeLayer(layer);                
                //this.layerControl.removeLayer(layer);
            }
        }

        for(var layerName in newModule.mapLayers) {
        
            var layer = newModule.mapLayers[layerName];
            this.lmap.addLayer(layer);
            var this_ = this;
        }
    },
    
    setBounds : function(bounds)
    {
    	this.lmap.fitBounds(bounds);
    },
    
    CLASS_NAME : "otp.core.Map"
});

