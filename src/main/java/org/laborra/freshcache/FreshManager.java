package org.laborra.freshcache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Created by zeeke on 1/25/14.
 */
public class FreshManager {

    private final Cache cache;

    public FreshManager(Cache cache) {
        this.cache = cache;
    }

    public <T> T runWithCache (String key, Callable<T> callable, IFreshEvaluator evaluator) {

        final Integer validityToken = evaluator.evaluate();
        final Cache.ValueWrapper valueWrapper = cache.get(key);

        if (valueWrapper == null) {
            return storeInCache(callable, key, cache, validityToken);
        }

        final Object cachedObj = valueWrapper.get();
        if (cachedObj == null) {
            return storeInCache(callable, key, cache, validityToken);
        }

        if (!(cachedObj instanceof Object[])) {
            return storeInCache(callable, key, cache, validityToken);
        }

        final Object[] composedValue = (Object[]) cachedObj;

        if (validityToken == composedValue[0]) {
            return (T) composedValue[1];
        }

        return storeInCache(callable, key, cache, validityToken);

    }

    private <T> T storeInCache(Callable<T> callable, String key, Cache fresh, Integer validityToken) {

        T retValue;
        try {
            retValue = callable.call();
        } catch (Exception e) {
            throw new FreshCacheException(e);
        }
        fresh.put(key, new Object[] { validityToken, retValue });
        return retValue;
    }

    interface IFreshEvaluator {
        public Integer evaluate ();
    }

    public static class FreshCacheException extends RuntimeException {

        public FreshCacheException(Exception e) {
            super(e);
        }
    }
}
