import nose
from nose.tools import assert_equal
from geohash import GeoHash


"""
geohash = GeoHash(12)

enc = geohash.encode(35.658515,139.70134)
dec = geohash.decode(enc)
adj = geohash.adjacent(enc,'top')
neighbors = geohash.neighbors(enc)

print adj
print neighbors
"""

class TestGeoHash(object):

    def setup(self):
        self.geohash = GeoHash(8)

    def test_geohash(self):
        assert_equal(8, self.geohash.precision)
        assert_equal(10, GeoHash(10).precision)

    def test_geohash_encode(self):
        geohash1 = self.geohash.encode(35.7101389, 139.8108333)
        assert_equal(8, len(geohash1))
        assert_equal('xn77jkz4', geohash1)

        geohash2 = GeoHash(10).encode(35.7101389, 139.8108333)
        assert_equal(10, len(geohash2))
        assert_equal('xn77jkz4ss', geohash2)

    def test_geohash_decode(self):
        geohash = self.geohash.encode(35.7101389, 139.8108333)
        assert_equal(8, len(geohash))
        assert_equal('xn77jkz4', geohash)

        latlng = self.geohash.decode(geohash)
        assert_equal(35, int(latlng['lat']))
        assert_equal(139, int(latlng['lng']))

    def test_geohash_adjacent(self):
        geohash = self.geohash.encode(35.7101389, 139.8108333)
        assert_equal(8, len(geohash))
        assert_equal('xn77jkz4', geohash)

        assert_equal('xn77jkz5', self.geohash.adjacent(geohash, 'top'))
        assert_equal('xn77jkz1', self.geohash.adjacent(geohash, 'bottom'))
        assert_equal('xn77jkyf', self.geohash.adjacent(geohash, 'left'))
        assert_equal('xn77jkz6', self.geohash.adjacent(geohash, 'right'))
        assert_equal(None, self.geohash.adjacent(geohash, 'A'))

    def test_geohash_neighbor(self):
        geohash = self.geohash.encode(35.7101389, 139.8108333)
        assert_equal(8, len(geohash))
        assert_equal('xn77jkz4', geohash)

        neighbors = self.geohash.neighbors(geohash)
        assert_equal(8, len(neighbors))
