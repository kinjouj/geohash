module.exports = {
  process: function (src, filename) {
    if (filename.match(/geohash\.js$/)) {
      src += 'module.exports = GeoHash;';
    }

    return src;
  }
};
