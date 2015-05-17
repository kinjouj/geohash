package kinjouj.geohash;

import org.junit.Test;

import static kinjouj.geohash.Direction.*;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

public class DirectionTestCase {
    @Test
    public void test() {
        assertThat(TOP.get(), is(0));
        assertThat(RIGHT.get(), is(1));
        assertThat(BOTTOM.get(), is(2));
        assertThat(LEFT.get(), is(3));
    }
}