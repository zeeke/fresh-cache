package org.laborra.freshcache;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;

import static org.mockito.Mockito.*;

/**
 * Created by zeeke on 1/25/14.
 */
public class FreshManagerTest {

    @Test
    public void testCacheMiss () {

        Cache cache = mock(Cache.class);
        when(cache.get("fooKey")).thenReturn(null);

        FreshManager freshManager = new FreshManager(cache);

        String value = freshManager.runWithCache(
                "fooKey",
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return "fooValue";
                    }
                },
                new FreshManager.IFreshEvaluator() {
                    @Override
                    public Integer evaluate() {
                        return 42;
                    }
                }
        );

        Assert.assertEquals("fooValue", value);
    }

    @Test
    public void cacheHit () throws Exception {
        Cache cache = mock(Cache.class);
        when(cache.get("fooKey"))
                .thenReturn(new SimpleValueWrapper(new Object[]{42, "fooValue"}));

        FreshManager freshManager = new FreshManager(cache);

        Callable<String> callable = mock(Callable.class);

        String value = freshManager.runWithCache(
                "fooKey",
                callable,
                new FreshManager.IFreshEvaluator() {
                    @Override
                    public Integer evaluate() {
                        return 42;
                    }
                }
        );

        verify(callable, never()).call();
        Assert.assertEquals("fooValue", value);
    }

    @Test
    public void badCachedValue () {
        Cache cache = mock(Cache.class);
        when(cache.get("fooKey")).thenReturn(new SimpleValueWrapper("badValue"));

        FreshManager freshManager = new FreshManager(cache);

        String value = freshManager.runWithCache(
                "fooKey",
                new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return "fooValue";
                    }
                },
                new FreshManager.IFreshEvaluator() {
                    @Override
                    public Integer evaluate() {
                        return 42;
                    }
                }
        );

        Assert.assertEquals("fooValue", value);
    }
}
