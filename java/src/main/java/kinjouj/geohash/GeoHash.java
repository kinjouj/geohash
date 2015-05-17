package kinjouj.geohash;

import java.util.LinkedList;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import static kinjouj.geohash.Direction.*;

/**
 * GeoHash
 * @version 0.1
 * @author kinjouj
 */
public class GeoHash {

    private static final char[] base32 = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n',
        'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
    };

    private static final String[][] _border = new String[][] {
        { "prxz", "bcfguvyz" },
        { "bcfguvyz", "prxz" },
        { "028b", "0145hjnp" },
        { "0145hjnp", "028b" }
    };

    private static final String[][] _neighbor = new String[][] {
        { "p0r21436x8zb9dcf5h7kjnmqesgutwvy", "bc01fg45238967deuvhjyznpkmstqrwx" },
        { "bc01fg45238967deuvhjyznpkmstqrwx", "p0r21436x8zb9dcf5h7kjnmqesgutwvy" },
        { "14365h7k9dcfesgujnmqp0r2twvyx8zb", "238967debc01fg45kmstqrwxuvhjyznp" },
        { "238967debc01fg45kmstqrwxuvhjyznp", "14365h7k9dcfesgujnmqp0r2twvyx8zb" }
    }; 

    /**
     * GeoHashのデフォルト文字長
     */
    public static int DEFAULT_PRECISION_SIZE = 8;

    private GeoHash() {}

    /**
     * 緯度経度からGeoHashにエンコード
     * @param lat 緯度
     * @param lon 経度
     * @return GeoHashコード (デフォルト文字長は8)
     */
    public static String encode(double lat, double lon) {
        return encode(lat, lon, DEFAULT_PRECISION_SIZE);
    }

    /**
     * 緯度経度からGeoHashにエンコード
     * @param lat 緯度
     * @param lon 経度
     * @param precision GeoHashの文字長
     * @return GeoHashコード
     */
    public static String encode(double lat, double lon, int precision) {
        if (precision <= 0) {
            throw new IllegalArgumentException("length is greater than zero");
        }

        int intervalLength = precision * 5;

        double[] latInterval = new double[] { -90.0, 90.0 };
        double[] lonInterval = new double[] { -180.0, 180.0 };

        StringBuilder bits = new StringBuilder();

        for (int i = 0; i < intervalLength; i++) {
            double latMiddle = (latInterval[0] + latInterval[1]) / 2;
            double lonMiddle = (lonInterval[0] + lonInterval[1]) / 2;

            if (lonMiddle > lon) {
                bits.append("0");
                lonInterval = new double[] { lonInterval[0], lonMiddle };
            } else {
                bits.append("1");
                lonInterval = new double[] { lonMiddle, lonInterval[1] };
            }

            if (latMiddle > lat) {
                bits.append("0");
                latInterval = new double[] { latInterval[0], latMiddle };
            } else {
                bits.append("1");
                latInterval = new double[] { latMiddle, latInterval[1] };
            }
        }

        String bit = bits.toString();
        StringBuilder sb = new StringBuilder();

        for (int j = 0; j < precision; j++) {
            String s = StringUtils.substring(bit, j * 5, j * 5 + 5);

            sb.append(base32[Integer.parseInt(s, 2)]);
        }

        return sb.toString();
    }

    /**
     * GeoHashコードから緯度経度にデコード
     * @param code GeoHashコード
     * @return デコードされた緯度経度
     */
    public static double[] decode(String code) {
        if (code == null) {
            throw new IllegalArgumentException("code is null");
        }

        if (code.isEmpty()) {
            throw new IllegalArgumentException("code is empty");
        }

        Map<Character,Integer> base32Characters = new HashMap<Character, Integer>();
        int base32Length = base32.length;

        for (int i = 0; i < base32Length; i++) {
            base32Characters.put(base32[i], i);
        }

        char[] codes = code.toCharArray();
        List<String> bits = new LinkedList<String>();

        for (char c : codes) {
            String bit = Integer.toBinaryString(base32Characters.get(c));
            bit = String.format("%05d", Integer.parseInt(bit));

            for (char x : bit.toCharArray()) {
                bits.add(String.valueOf(x));
            }
        }

        List<String> lats = new LinkedList<String>();
        List<String> lons = new LinkedList<String>();

        for (int i = 0; i < bits.size(); i++) {
            if(i % 2 != 0) {
                lats.add(bits.get(i));
            } else {
                lons.add(bits.get(i));
            }
        }

        double[] lat_interval = new double[] { -90.0, 90.0 };
        double[] lon_interval = new double[] { -180.0, 180.0 };

        for (int i = 0; i < lats.size(); i++) {
            double intervalMiddle = (lat_interval[0] + lat_interval[1]) / 2;

            if ("1".equals(lats.get(i))) {
                lat_interval = new double[] { intervalMiddle, lat_interval[1] };
            } else {
                lat_interval = new double[] { lat_interval[0], intervalMiddle };
            }
        }

        for (int i = 0; i < lons.size(); i++) {
            double intervalMiddle = (lon_interval[0] + lon_interval[1]) / 2;

            if ("1".equals(lons.get(i))) {
                lon_interval = new double[] { intervalMiddle, lon_interval[1] };
            } else {
                lon_interval = new double[] { lon_interval[0], intervalMiddle };
            }
        }

        double lat = (lat_interval[0] + lat_interval[1]) / 2;
        double lon = (lon_interval[0] + lon_interval[1]) / 2;

        return new double[] { lat, lon };
    }

    /**
     * 隣接角のGeoHashコードを取得
     * @param code 中心となるGeoHashコード
     * @param direction 方向ディレクション(kinjouj.geohash.Direction)
     * @return 隣接角のGeoHashコード
     */
    public static String adjacent(String code, Direction direction) {
        if (code == null) {
            throw new IllegalArgumentException("code is null");
        }

        if (code.isEmpty()) {
            throw new IllegalArgumentException("code is empty");
        }

        if (direction == null) {
            throw new IllegalArgumentException("direction is null");
        }

        int codeLength = code.length();
        int type = codeLength % 2 == 0 ? 0 : 1;

        char c = code.charAt(codeLength - 1);
        int directionValue = direction.get();

        String base = code.substring(0, codeLength - 1);
        String border = _border[directionValue][type];

        if(border.indexOf(c) != -1) {
            base = adjacent(base, direction);
        }

        String neighbor = _neighbor[directionValue][type];

        return base + base32[neighbor.indexOf(c)];
    }

    /**
     * 近傍探索
     * @param code 取得したGeoHash
     * @return 中心となるGeoHashコードから近傍エリアのGeoHashを取得
     */
    public static List<String> neighbor(String code) {
        if (code == null) {
            throw new IllegalArgumentException("code is null");
        }

        if (code.isEmpty()) {
            throw new IllegalArgumentException("code is empty");
        }

        Direction[][] directions = new Direction[][] {
            { TOP, RIGHT },
            { RIGHT, BOTTOM },
            { BOTTOM, LEFT },
            { LEFT, TOP }
        };

        List<String> neighbors = new LinkedList<String>();

        for (Direction[] direction : directions) {
            String point1 = adjacent(code, direction[0]);
            String point2 = adjacent(point1, direction[1]);

            neighbors.add(point1);
            neighbors.add(point2);
        }

        return neighbors;
    }

    /**
     * 近傍探索
     * @param lat 緯度
     * @param lng 経度
     * @return 中心となる緯度経度から近傍エリアを取得 (デフォルト文字長は8)
     */
    public static List<String> neighbor(double lat, double lng) {
        return neighbor(lat, lng, DEFAULT_PRECISION_SIZE);
    }

    /**
     * 近傍探索
     * @param lat 緯度
     * @param lng 経度
     * @param precision GeoHashの文字長
     * @return 中心となる緯度経度から近傍エリアを取得
     */
    public static List<String> neighbor(double lat, double lng, int precision) {
        return neighbor(encode(lat, lng, precision));
    }
}