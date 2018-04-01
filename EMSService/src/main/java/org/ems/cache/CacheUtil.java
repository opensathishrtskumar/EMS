package org.ems.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheUtil {

	public static final String CACHENAME = "emscache";

	@Autowired
	private CacheManager cacheManager;

	public Cache getCache() {
		return cacheManager.getCache(CACHENAME);
	}

	public void putCacheEntry(String key, Object value) {
		getCache().put(key, value);
	}

	public <T> T getCacheEntry(String key, Class<T> className) {
		return getCache().get(key, className);
	}

}
