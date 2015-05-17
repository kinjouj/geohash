package kinjouj.geohash;

import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;
import static kinjouj.geohash.Direction.*;

public class GeoHashTestCase {

    // 東京スカイツリーの緯度
    private double lat = 35.7101389;

    // 東京スカイツリーの経度
    private double lng = 139.8108333;

    // デフォルトで生成したGeoHashコード
    private String code = "xn77jkz4";

    @Test
    public void encode() {
        String enc = GeoHash.encode(lat, lng);

        assertThat(enc, is("xn77jkz4"));
        assertThat(enc.length(), is(8));

        enc = GeoHash.encode(lat, lng, 10);

        assertThat(enc, is("xn77jkz4ss"));
        assertThat(enc.length(), is(10));
    }

    @Test(expected=IllegalArgumentException.class)
    public void encodeThrown() {
        GeoHash.encode(lat, lng, 0);
    }

    @Test
    public void decode() {
        double[] latlng = GeoHash.decode(code);
        assertThat(latlng.length, is(2));

        double lat = latlng[0];
        assertThat((int)lat, is(35));

        double lng = latlng[1];
        assertThat((int)lng, is(139));
    }

    @Test(expected=IllegalArgumentException.class)
    public void decodeThrown1() {
        GeoHash.decode(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void decodeThrown2() {
        GeoHash.decode("");
    }

    @Test
    public void adjacent() {
        assertThat(GeoHash.adjacent(code, TOP), is("xn77jkz5"));
        assertThat(GeoHash.adjacent(code, BOTTOM), is("xn77jkz1"));
        assertThat(GeoHash.adjacent(code, LEFT), is("xn77jkyf"));
        assertThat(GeoHash.adjacent(code, RIGHT), is("xn77jkz6"));
    }

    @Test(expected=IllegalArgumentException.class)
    public void adjacentThrown1() {
        GeoHash.adjacent(null, Direction.TOP);
    }

    @Test(expected=IllegalArgumentException.class)
    public void adjacentThrown2() {
        GeoHash.adjacent("", Direction.TOP);
    }

    @Test(expected=IllegalArgumentException.class)
    public void adjacentThrown3() {
        GeoHash.adjacent(code, null);
    }

    @Test
    public void neighbor() {
        List<String> codes1 = GeoHash.neighbor(code);
        assertThat(codes1.size(), is(8));

        List<String> codes2 = GeoHash.neighbor(lat, lng);
        assertThat(codes2.size(), is(8));

        List<String> codes3 = GeoHash.neighbor(lat, lng, 10);
        assertThat(codes3.size(), is(8));
        assertThat(codes3.get(0).length(), is(10));
    }

    @Test(expected=IllegalArgumentException.class)
    public void neighborThrown1() {
        GeoHash.neighbor(null);
    }

    @Test(expected=IllegalArgumentException.class)
    public void neighborThrown2() {
        GeoHash.neighbor("");
    }
}
