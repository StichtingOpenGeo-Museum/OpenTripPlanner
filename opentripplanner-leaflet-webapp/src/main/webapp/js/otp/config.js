// step 1: make sure we have some type of otp.config, and otp.config.locale defined
if(typeof(otp) == "undefined" || otp == null) otp = {};
if(typeof(otp.config) == "undefined" || otp.config == null) otp.config = {};
//if(typeof(otp.config.locale) == "undefined" || otp.config.locale == null) otp.config.locale = otp.locale.English;


// step 2: create an object of default otp.config default values (see step3 where we apply this to any existing config)
otp.config = {
/*
    // OTP server address and routerId (if applicable)
    hostname : "http://opentripplanner.eu",
    routerId : "benelux",

    // Base map tiles settings:
    tileUrl : 'http://{s}.tiles.mapbox.com/v3/mapbox.mapbox-streets/{z}/{x}/{y}.png',
    // overlayTileUrl : [link to tileset to overlay on base layer],
    tileAttrib : 'Routing powered by <a href="http://opentripplanner.org/">OpenTripPlanner</a>, Map tiles from MapBox (<a href="http://mapbox.com/about/maps/">terms</a>) and OpenStreetMap ',
    
    // map start location and zoom settings 
    initLatLng : new L.LatLng(52.07,5.2), // (NL)
    initZoom : 8,
    minZoom : 6,
    maxZoom : 17,*/
    // OTP server address and routerId (if applicable)
    hostname : "http://opentripplanner.eu",
    routerId : "benelux",

    // Base map tiles settings:
    tileUrl : 'http://{s}.tiles.mapbox.com/v3/mapbox.mapbox-streets/{z}/{x}/{y}.png',
    // overlayTileUrl : [link to tileset to overlay on base layer],
    tileAttrib : 'Routing powered by <a href="http://opentripplanner.org/">OpenTripPlanner</a>, Map tiles from MapBox (<a href="http://mapbox.com/about/maps/">terms</a>) and OpenStreetMap ',
    
    // map start location and zoom settings 
    /*initLatLng : new L.LatLng(52.07,5.2), // NL
    initZoom : 14,
    minZoom : 13,
    maxZoom : 17,*/

    showLogo:           true,
    showTitle:          true,
    showModuleSelector: true,

    logoGraphic :       'images/openplans-logo-40x40.png',

    siteName    : "My OTP Instance",
    siteURL     : "[link to site]",
    siteDescription  : "An OpenTripPlanner deployment.",
    
    // bikeshareName : "",

    loggerURL : 'http://localhost:9000',
    // dataStorageUrl : '[link]',
            
    timeOffset : -3,
                
    infoWidgets: [
        {
            title: 'About',
            styleId: 'otp-aboutWidget',
            content: '<p>About this site</p>',
        },
        {
            title: 'Contact',
            styleId: 'otp-contactWidget',
            content: '<p>Comments? Contact us at...</p>'
        },           
    ],
    
    showAddThis     : false,
    //addThisPubId    : 'your-addthis-id',
    //addThisTitle    : 'Your title for AddThis sharing messages',
    
};

