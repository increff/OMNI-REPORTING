package com.increff.account.client;

import com.hazelcast.config.*;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.spring.cache.HazelcastCacheManager;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.increff.account.client.Params.INSTANCE_NAME;
import static com.increff.account.client.Params.USERS;

@Configuration
@EnableCaching
@Getter
@Setter
public class AuthConfig {

	@Value("${auth.appName}")
	private String authAppName;
	@Value("${cache.timeToLive:60}")
	private int timeToLive;
	@Value("${cache.size:300}")
	private int maxCacheSize;
	@Value("${auth.cookiePath:/}")
	private String authCookiePath;
	@Value("${auth.baseUrl}")
	private String authBaseUrl;
	@Value("${auth.appToken}")
	private String authAppToken;

	// GETTERS!

	public String getAuthBaseUrl() {
		return authBaseUrl;
	}

	public String getAuthAppToken() {
		return authAppToken;
	}

	public String getAuthAppName() {
		return authAppName;
	}

	@Bean
	public CacheManager cacheManager(HazelcastInstance hazelcastInstance) {
		return new HazelcastCacheManager(hazelcastInstance);
	}
	@Bean
	public HazelcastInstance hazelcastInstance(Config config) {
		return Hazelcast.newHazelcastInstance(config);
	}

	@Bean
	public Config config() {
		Config config =  new Config()
				.setInstanceName(INSTANCE_NAME)
				.addMapConfig(new MapConfig()
						.setName(USERS)
						.setTimeToLiveSeconds(getTimeToLive())
						.setEvictionConfig(new EvictionConfig()
								.setSize(getMaxCacheSize())
								.setMaxSizePolicy(MaxSizePolicy.FREE_HEAP_SIZE)
								.setEvictionPolicy(EvictionPolicy.LRU)
						)
				);
		return config;
	}
}
