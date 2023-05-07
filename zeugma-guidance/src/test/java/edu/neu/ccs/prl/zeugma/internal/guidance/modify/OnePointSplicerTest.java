package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class OnePointSplicerTest {
    private final OnePointSplicer splicer = new OnePointSplicer(new Random(4380));

    @Test
    void recombineMiddle() {
        ByteList p1 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList p2 = TwoPointSplicerTest.makeRange(10, 10);
        ByteList child = OnePointSplicer.recombine(p1, 5, p2, 6);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 16, 17, 18, 19});
        assertEquals(expected, child);
    }

    @Test
    void recombineSingleton() {
        ByteList p1 = new ByteArrayList(new byte[]{0});
        ByteList p2 = new ByteArrayList(new byte[]{1});
        ByteList child = OnePointSplicer.recombine(p1, 0, p2, 0);
        ByteList expected = new ByteArrayList(new byte[]{1});
        assertEquals(expected, child);
    }

    @Test
    void recombineDoesNotModifyFirstParent() {
        ByteList p1 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList p2 = TwoPointSplicerTest.makeRange(10, 10);
        ByteList p1Copy = new ByteArrayList(p1);
        splicer.splice(p1, p2);
        assertEquals(p1Copy, p1);
    }

    @Test
    void recombineDoesNotModifySecondParent() {
        ByteList p1 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList p2 = TwoPointSplicerTest.makeRange(10, 10);
        ByteList p2Copy = new ByteArrayList(p2);
        splicer.splice(p1, p2);
        assertEquals(p2Copy, p2);
    }

    @Test
    void recombineFirstParentNull() {
        assertThrows(NullPointerException.class, () -> splicer.splice(null, new ByteArrayList()));
    }

    @Test
    void recombineSecondParentNull() {
        assertThrows(NullPointerException.class, () -> splicer.splice(new ByteArrayList(), null));
    }

    @Test
    void recombineFirstParentEmpty() {
        ByteList p1 = new ByteArrayList();
        ByteList p2 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList child = OnePointSplicer.recombine(p1, 0, p2, 2);
        ByteList expected = new ByteArrayList(new byte[]{2, 3, 4, 5, 6, 7, 8, 9});
        assertEquals(expected, child);
    }

    @Test
    void recombineSecondParentEmpty() {
        ByteList p1 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList p2 = new ByteArrayList();
        ByteList child = OnePointSplicer.recombine(p1, 2, p2, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 1});
        assertEquals(expected, child);
    }

    @Test
    void recombineBothParentsEmpty() {
        ByteList p1 = new ByteArrayList();
        ByteList p2 = new ByteArrayList();
        ByteList child = splicer.splice(p1, p2);
        assertEquals(new ByteArrayList(), child);
    }

    @Test
    void recombineNoneOfFirstParent() {
        ByteList p1 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList p2 = TwoPointSplicerTest.makeRange(10, 10);
        ByteList child = OnePointSplicer.recombine(p1, 0, p2, 6);
        ByteList expected = new ByteArrayList(new byte[]{16, 17, 18, 19});
        assertEquals(expected, child);
    }

    @Test
    void recombineAllOfSecondParent() {
        ByteList p1 = TwoPointSplicerTest.makeRange(0, 10);
        ByteList p2 = TwoPointSplicerTest.makeRange(10, 10);
        ByteList child = OnePointSplicer.recombine(p1, 7, p2, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5, 6, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19});
        assertEquals(expected, child);
    }
}