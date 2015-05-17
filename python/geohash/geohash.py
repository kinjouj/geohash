import re
import math

class GeoHash(object):
    def __init__(self, precision=8):
        self.precision = precision
        self.border = {
            'top':    { 'even': 'prxz',     'odd': 'bcfguvyz' },
            'right':  { 'even': 'bcfguvyz', 'odd': 'prxz' },
            'bottom': { 'even': '028b',     'odd': '0145hjnp' },
            'left':   { 'even': '0145hjnp', 'odd': '028b' }
        }
        self.neighbor = {
            'top':    { 'even': 'p0r21436x8zb9dcf5h7kjnmqesgutwvy', 'odd': 'bc01fg45238967deuvhjyznpkmstqrwx' },
            'right':  { 'even': 'bc01fg45238967deuvhjyznpkmstqrwx', 'odd': 'p0r21436x8zb9dcf5h7kjnmqesgutwvy' },
            'bottom': { 'even': '14365h7k9dcfesgujnmqp0r2twvyx8zb', 'odd': '238967debc01fg45kmstqrwxuvhjyznp' },
            'left':   { 'even': '238967debc01fg45kmstqrwxuvhjyznp', 'odd': '14365h7k9dcfesgujnmqp0r2twvyx8zb' }
        }

        self.base32 = {}

        base32 = re.findall(r'(\w)', '0123456789bcdefghjkmnpqrstuvwxyz')

        for i in range(0, len(base32)):
            c = base32[i]
            self.base32[i] = c
            self.base32[c] = i

    def encode(self,latitude,longitude):
        latitude = float(latitude)
        longitude = float(longitude)

        lat_interval = [-90.0, 90.0]
        lng_interval = [-180.0, 180.0]

        bits = []
        length = (self.precision * 5) / 2

        for i in range(0,length):
            lng_middle = self.middle(lng_interval)
            lat_middle = self.middle(lat_interval)

            if longitude > lng_middle:
                bits.append(1)
                lng_interval = [lng_middle, lng_interval[1]]
            else:
                bits.append(0)
                lng_interval = [lng_interval[0], lng_middle]

            if latitude > lat_middle:
                bits.append(1)
                lat_interval = [lat_middle, lat_interval[1]]
            else:
                bits.append(0)
                lat_interval = [lat_interval[0], lat_middle]

        hash = []

        for j in range(0, self.precision):
            bit = bits[:5]
            bits = bits[5:]

            bit = int(''.join(map(str, bit)), 2)

            hash.append(self.base32[bit])

        return ''.join(hash)

    def decode(self,geohash):
        intervals = self.decode_interval(geohash)
        latitude  = self.middle(intervals[0])
        longitude = self.middle(intervals[1])

        return {'lat': latitude, 'lng': longitude}

    def decode_interval(self, geohash):
        hash = re.findall(r'(\w)', geohash)

        bits = []

        for c in hash:
            bits.append(''.join(map(str, self.oct2bin(self.base32[c]))))

        bits = re.findall(r'([\d])', ''.join(map(str, bits)))

        lats = []
        lngs = []

        for i in range(0,len(bits)):
            bit = bits[i]

            if i % 2 != 0:
                lats.append(int(bit))
            else:
                lngs.append(int(bit))

        lat_interval = [-90.0, 90.0]
        lng_interval = [-180.0, 180.0]

        for lat in lats:
            lat_middle = self.middle(lat_interval)

            if lat == 1:
                lat_interval = [lat_middle, lat_interval[1]]
            else:
                lat_interval = [lat_interval[0], lat_middle]

        for lng in lngs:
            lng_middle = self.middle(lng_interval)

            if lng == 1:
                lng_interval = [lng_middle, lng_interval[1]]
            else:
                lng_interval = [lng_interval[0], lng_middle]


        return [lat_interval, lng_interval]

    def adjacent(self, geohash, direction):
        if direction not in ['top', 'right', 'bottom', 'left']:
            return None

        last   = geohash[-1]
        type   = 'even' if len(geohash) % 2 == 0 else 'odd'
        base   = geohash[0:-1]
        border = self.border[direction][type]

        if border.find(last) != -1:
            base = self.adjacent(base, direction)

        neighbor = self.neighbor[direction][type]

        return base + self.base32[neighbor.find(last)]

    def neighbors(self, geohash):
        directions = [
            ['top', 'right'],
            ['right', 'bottom'],
            ['bottom', 'left'],
            ['left', 'top']
        ]
        neighbors = []

        for direction in directions:
            point1 = self.adjacent(geohash, direction[0])
            point2 = self.adjacent(point1, direction[1])

            neighbors.append(point1)
            neighbors.append(point2)

        return neighbors

    def middle(self,ary):
        return (ary[0] + ary[1]) / 2

    def oct2bin(self, octal):
        binary = []

        for i in range(0, 5):
            binary.append(int(octal % 2))
            octal = math.floor(octal / 2)

        binary.reverse()

        return binary
