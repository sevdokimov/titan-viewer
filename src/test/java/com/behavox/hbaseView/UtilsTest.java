package com.behavox.hbaseView;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 *
 */
public class UtilsTest {

    @Test
    public void testGson() {
        A a = new A();
        a.a = new byte[]{1, 2};
        a.byteListA = Arrays.asList(new byte[]{1, 2}, new byte[]{1});

        String str = Utils.BHEX_GSON.toJson(a);

        System.out.println(str);

        A a2 = Utils.BHEX_GSON.fromJson(str, A.class);

        Assert.assertNull(a2.b);
        Assert.assertEquals(2, a2.a.length);
        Assert.assertNull(a2.byteListB);
        Assert.assertEquals(2, a2.byteListA.size());
    }

    private static class A {
        private byte[] a;
        private byte[] b;

        private List<byte[]> byteListA;
        private List<byte[]> byteListB;
    }
}
