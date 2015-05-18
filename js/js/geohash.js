function isArray(obj) {
  return Object.prototype.toString.call(obj) === '[object Array]';
}

// class: GeoHash
var GeoHash = function(precision) {
  if(precision == undefined || typeof(precision) !== 'number' || precision <= 0) {
    throw new InvalidPrecisionException();
  }

  this.precision = parseInt(precision);
  this.border = {
    'top':    { 'even': 'prxz',     'odd': 'bcfguvyz' },
    'right':  { 'even': 'bcfguvyz', 'odd': 'prxz' },
    'bottom': { 'even': '028b',     'odd': '0145hjnp' },
    'left':   { 'even': '0145hjnp', 'odd': '028b' }
  };
  this.neighbor = {
    'top': {
      'even': 'p0r21436x8zb9dcf5h7kjnmqesgutwvy',
      'odd':  'bc01fg45238967deuvhjyznpkmstqrwx'
    },
    'right': {
      'even': 'bc01fg45238967deuvhjyznpkmstqrwx',
      'odd':  'p0r21436x8zb9dcf5h7kjnmqesgutwvy'
    },
    'bottom': {
      'even': '14365h7k9dcfesgujnmqp0r2twvyx8zb',
      'odd':  '238967debc01fg45kmstqrwxuvhjyznp'
    },
    'left': {
      'even': '238967debc01fg45kmstqrwxuvhjyznp',
      'odd':  '14365h7k9dcfesgujnmqp0r2twvyx8zb'
    }
  };

  var chars = '0123456789bcdefghjkmnpqrstuvwxyz'.split('');
  var chars_len = chars.length;
  var ary = new Array;

  for(var i = 0; i < chars_len; i++) {
    var t = chars[i];
    ary[t] = i;
    ary[i] = t;
  }

  this.base32 = ary;
};

GeoHash.prototype.encode= function (lat, lng, precision) {
  if(lat === undefined || typeof(lat) !== 'number' || lng === undefined || typeof(lng) !== 'number') {
    return null;
  }

  if(precision === undefined || precision <= 0) {
    precision = this.precision;
  }

  var lng_interval = [-180.0, 180.0];
  var lat_interval = [-90.0, 90.0];
  var geohash_length = (precision * 5) / 2;
  var bits = new Array;

  for(var i = 0; i < geohash_length; i++) {
    var lng_middle = this.middle(lng_interval);
    var lat_middle = this.middle(lat_interval);

    if(lng > lng_middle) {
      bits.push(1);
      lng_interval = [lng_middle, lng_interval[1]];
    } else {
      bits.push(0);
      lng_interval = [lng_interval[0], lng_middle];
    }
    if(lat > lat_middle) {
      bits.push(1);
      lat_interval = [lat_middle, lat_interval[1]];
    } else {
      bits.push(0);
      lat_interval = [lat_interval[0], lat_middle];
    }
  }

  bits = bits.join('');

  var hash = new Array;

  for(var i = 0; i < precision; i++) {
    var bit = bits.substring(0, 5);
    var bit_idx = parseInt(bit, 2);

    bits = bits.substring(5);
    hash.push(this.base32[bit_idx]);
  }

  return hash.join('');
};

GeoHash.prototype.decode = function(geohash) {
  if(geohash === undefined || typeof(geohash) !== 'string' || geohash.length <= 0) {
    return null;
  }

  var intervals = this.decode_interval(geohash);
  var lat = this.middle(intervals[0]);
  var lng = this.middle(intervals[1]);

  return {'lat': lat,'lng': lng};
};

GeoHash.prototype.decode_interval = function(geohash) {
  if(geohash === undefined || geohash.length <= 0) {
    return null;
  }

  var oct2bin = function(octal) {
    var bin = new Array(5);

    for(var i = 0; i < 5; i++) {
      bin[i] = octal % 2;
      octal = Math.floor(octal / 2);
    }

    return bin.reverse().join('');
  };

  var hash = geohash.split('');
  var bits = new Array;

  for(var i = 0; i < hash.length; i++) {
    bits.push(oct2bin(this.base32[hash[i]]));
  }

  bits = bits.join('').split('');

  var lats = new Array;
  var lngs = new Array;

  for(var i = 0; i < bits.length; i++) {
    var bit = parseInt(bits[i], 10);

    if(i % 2 != 0) {
      lats.push(bit);
    } else {
      lngs.push(bit);
    }
  }

  var lat_interval = [-90.0, 90.0];
  var lng_interval = [-180.0, 180.0];

  for(var i = 0; i < lats.length; i++) {
    var lat_middle = this.middle(lat_interval);
    var lat = lats[i];

    if(lat == 1) {
      lat_interval = [lat_middle, lat_interval[1]];
    } else {
      lat_interval = [lat_interval[0], lat_middle];
    }
  }
  for(var i = 0; i < lngs.length; i++) {
    var lng_middle = this.middle(lng_interval);
    var lng = lngs[i];

    if(lng == 1) {
      lng_interval = [lng_middle, lng_interval[1]];
    } else {
      lng_interval = [lng_interval[0], lng_middle];
    }
  }

  return [lat_interval, lng_interval];
};

GeoHash.prototype.middle = function(ary) {
  if(ary === undefined || !isArray(ary) || ary[0] === undefined || typeof(ary[0]) !== 'number' || ary[1] === undefined || typeof(ary[1]) !== 'number') {
    return null;
  }

  return (ary[0] + ary[1]) / 2;
};

GeoHash.prototype.adjacent = function(geohash,direction) {
  if(geohash === undefined || typeof(geohash) !== 'string' || geohash.length <= 0) {
    return null;
  }

  if(direction === undefined || typeof(direction) !== 'string' || !(direction.match(/^(top|bottom|left|right)$/))) {
    throw new InvalidDirectionException();
  }

  var last = geohash.substring(geohash.length - 1);
  var type = geohash.length % 2 == 0 ? 'even' : 'odd';
  var base = geohash.substring(0, geohash.length - 1);
  var border = this.border[direction][type];

  if(border.indexOf(last) != -1) {
    base = this.adjacent(base,direction);
  }

  var neighbor = this.neighbor[direction][type];
  return base + this.base32[neighbor.indexOf(last)];
};

GeoHash.prototype.neighbors = function(geohash) {
  if(geohash === undefined || typeof(geohash) !== 'string' || geohash.length <= 0) {
    return null;
  }

  var directions = [
    ['top', 'right'],
    ['right', 'bottom'],
    ['bottom', 'left'],
    ['left', 'top']
  ];

  var neighbors = [];

  for(var i = 0; i < directions.length; i++) {
    var direction = directions[i];

    var point1 = this.adjacent(geohash, direction[0]);
    var point2 = this.adjacent(point1, direction[1]);

    if(point1 !== undefined) {
      neighbors.push(point1);
    }

    if(point2 !== undefined) {
      neighbors.push(point2);
    }
  }

  return neighbors;
};

var InvalidPrecisionException = function() {};
InvalidPrecisionException.prototype = { message: 'invalid "precision"' };

var InvalidDirectionException = function() {};
InvalidDirectionException.prototype = { message: 'invalid "direction"' };
