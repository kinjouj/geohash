jest.autoMockOff();

var GeoHash = require("../geohash");

var lat = 35.7101389;
var lng = 139.8108333;

describe('GeoHash', function() {
  it('constructor', function() {
    var geohash = new GeoHash(8);
    expect(geohash).not.toBeNull();
    expect(function() { new GeoHash(); }).toThrow('invalid "precision"');
    expect(function() { new GeoHash("8"); }).toThrow('invalid "precision"');
    expect(function() { new GeoHash(0); }).toThrow('invalid "precision"');
  });

  it('encode', function() {
    var geohash = new GeoHash(8);
    expect(geohash.encode(lat, lng)).toBe("xn77jkz4");
    expect(geohash.encode(lat, lng, 0)).toBe("xn77jkz4");
    expect(geohash.encode()).toBeNull();
  });

  it('decode', function() {
    var geohash = new GeoHash(8);
    var latlng = geohash.decode('xn77jkz4');
    expect(latlng).not.toBeNull();
    expect(parseInt(latlng.lat)).toBe(35);
    expect(parseInt(latlng.lng)).toBe(139);
    expect(geohash.decode()).toBeNull();
  });

  it('adjacent', function() {
    var geohash = new GeoHash(8);
    var adjacent = geohash.adjacent('xn77jkz4', 'top');
    expect(adjacent).toBe('xn77jkz5');
    expect(geohash.adjacent()).toBeNull();
    expect(function() { geohash.adjacent('xn77jkz4'); }).toThrow();
  });

  it('neighbors', function() {
    var geohash = new GeoHash(8);
    var neighbors = geohash.neighbors('xn77jkz4');
    expect(neighbors.length).toBe(8);
    expect(geohash.neighbors()).toBeNull();
  });

  it('decode_interval', function() {
    var geohash = new GeoHash(8);
    expect(geohash.decode_interval('xn77jkz4')).not.toBeNull();
    expect(geohash.decode_interval()).toBeNull();
  });

  it('middle', function() {
    var geohash = new GeoHash(8);
    expect(geohash.middle([2, 2])).toBe(2);
    expect(geohash.middle()).toBeNull();
  });
});
