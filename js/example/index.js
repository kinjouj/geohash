"use strict";

navigator.geolocation.getCurrentPosition(function(position) {
  var geohash = new GeoHash(6);
  var lat = position.coords.latitude;
  var lng = position.coords.longitude;
  var enc = geohash.encode(lat,lng);
  var dec = geohash.decode(enc);

  console.debug('navigator.geolocation.getCurrentPosition', lat, lng);
  console.debug('Encode GeoHash', enc);
  console.debug('Decode GeoHash', geohash.decode(enc));
  console.debug('Neighbors', geohash.neighbors(enc));

  var center = new google.maps.LatLng(
    position.coords.latitude,
    position.coords.longitude
  );

  var map = new google.maps.Map(
    document.querySelector('#map'),
    {
      zoom: 12,
      center
    }
  );

  new google.maps.Marker({ position: center, map: map, title: enc });

  var topLeftLatLng = geohash.decode(
    geohash.adjacent(geohash.adjacent(enc, 'top'), 'left')
  );

  var bottomRightLatLng = geohash.decode(
    geohash.adjacent(geohash.adjacent(enc, 'bottom'), 'right')
  );

  var bounds = new google.maps.LatLngBounds(
    new google.maps.LatLng(topLeftLatLng.lat, topLeftLatLng.lng),
    new google.maps.LatLng(bottomRightLatLng.lat, bottomRightLatLng.lng)
  );

  var rectangle = new google.maps.Rectangle();
  rectangle.setOptions({
    strokeColor: "0000FF",
    strokeOpacity: 0.5,
    strokeWeight: 1,
    fillColor: "#0000FF",
    fillOpacity: 0.5,
    bounds: bounds,
    map: map
  });
});
