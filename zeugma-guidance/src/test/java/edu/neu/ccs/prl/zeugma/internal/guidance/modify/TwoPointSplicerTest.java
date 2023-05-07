package edu.neu.ccs.prl.zeugma.internal.guidance.modify;

import edu.neu.ccs.prl.zeugma.internal.util.ByteArrayList;
import edu.neu.ccs.prl.zeugma.internal.util.ByteList;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertThrows;

class TwoPointSplicerTest {
    private final TwoPointSplicer splicer = new TwoPointSplicer(new Random(4380));

    public static ByteList makeRange(int start, int length) {
        ByteList p1 = new ByteArrayList();
        for (int i = 0; i < length; i++) {
            p1.add((byte) (start + i));
        }
        return p1;
    }

    @Test
    void recombineMiddle() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList child = splicer.recombine(p1, 5, 7, p2, 6, 8);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 16, 17, 18, 8, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineSingleton() {
        ByteList p1 = new ByteArrayList(new byte[]{0});
        ByteList p2 = new ByteArrayList(new byte[]{1});
        ByteList child = splicer.recombine(p1, 0, 1, p2, 0, 1);
        ByteList expected = new ByteArrayList(new byte[]{1});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineDoesNotModifyFirstParent() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList p1Copy = new ByteArrayList(p1);
        splicer.splice(p1, p2);
        Assertions.assertEquals(p1Copy, p1);
    }

    @Test
    void recombineDoesNotModifySecondParent() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList p2Copy = new ByteArrayList(p2);
        splicer.splice(p1, p2);
        Assertions.assertEquals(p2Copy, p2);
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
        ByteList p2 = makeRange(0, 10);
        ByteList child = splicer.recombine(p1, 0, 0, p2, 2, 3);
        ByteList expected = new ByteArrayList(new byte[]{2, 3});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineSecondParentEmpty() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = new ByteArrayList();
        ByteList child = splicer.recombine(p1, 2, 3, p2, 0, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 4, 5, 6, 7, 8, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineBothParentsEmpty() {
        ByteList p1 = new ByteArrayList();
        ByteList p2 = new ByteArrayList();
        ByteList child = splicer.splice(p1, p2);
        Assertions.assertEquals(new ByteArrayList(), child);
    }

    @Test
    void recombineMostOfFirstParent() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList child = splicer.recombine(p1, 6, 6, p2, 6, 8);
        ByteList expected = new ByteArrayList(new byte[]{0, 1, 2, 3, 4, 5, 16, 17, 18, 7, 8, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineNoneOfFirstParent() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList child = splicer.recombine(p1, 0, p1.size() - 1, p2, 6, 7);
        ByteList expected = new ByteArrayList(new byte[]{16, 17});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineAllOfSecondParent() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList child = splicer.recombine(p1, 1, 8, p2, 0, p2.size() - 1);
        ByteList expected = new ByteArrayList(new byte[]{0, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 9});
        Assertions.assertEquals(expected, child);
    }

    @Test
    void recombineLittleOfSecondParent() {
        ByteList p1 = makeRange(0, 10);
        ByteList p2 = makeRange(10, 10);
        ByteList child = splicer.recombine(p1, 1, 8, p2, 0, 0);
        ByteList expected = new ByteArrayList(new byte[]{0, 10, 9});
        Assertions.assertEquals(expected, child);
    }
}