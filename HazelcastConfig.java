package com.lowes.storeelasticsearch.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.Config;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MapConfig;
import com.hazelcast.config.MaxSizeConfig;

/**
 * Hazelcast in-memory configuration class
 * 
 * @author ndevara
 *
 */
@Configuration
public class HazelcastConfig {

	@Value("${hazelcast.instance}")
	private String hazelcastInstanceName;

	@Value("${hazelcast.map}")
	private String mapName;

	/**
	 * In-Memory Config
	 * 
	 * @return
	 */
	@Bean
	public Config hazelCastConfig() {
		Config config = new Config();
		config.setInstanceName(hazelcastInstanceName)
				.addMapConfig(new MapConfig().setName(mapName)
						.setMaxSizeConfig(new MaxSizeConfig(300, MaxSizeConfig.MaxSizePolicy.FREE_HEAP_SIZE))
						.setEvictionPolicy(EvictionPolicy.NONE).setAsyncBackupCount(2).setReadBackupData(true)
						.setTimeToLiveSeconds(0));
		return config;
	}

	/**
	 * TCP/IP Config NOTE: Please don't remove
	 */

//	@Value("${cache.hostnames}")
//	private String hosts;
//
//	@SuppressWarnings("deprecation")
//	@Bean
//	public HazelcastCacheManager hazelcastCacheManager() {
//		ClientConfig config = new ClientConfig();
//		config.setInstanceName("instance");
//		config.setProperty("hazelcast.client.statistics.enabled", "true");
//		GroupConfig groupConfig = config.getGroupConfig();
//		if (groupConfig == null) {
//			groupConfig = new GroupConfig();
//		}
//		groupConfig.setName("");
//		groupConfig.setPassword("");
//		config.setGroupConfig(groupConfig);
//
//		// Configuring Near Cache
////		NearCacheConfig nearCacheConfig = new NearCacheConfig().setName("storeMap")
////				.setInMemoryFormat(InMemoryFormat.BINARY).setSerializeKeys(true).setInvalidateOnChange(false)
////				.setTimeToLiveSeconds(0).setMaxIdleSeconds(0)
////				.setLocalUpdatePolicy(NearCacheConfig.LocalUpdatePolicy.INVALIDATE);
////
////		config.addNearCacheConfig(nearCacheConfig);
//
//		// Split the address by #
//		String addresses[] = hosts.split("#");
//		config.getNetworkConfig().addAddress(addresses);
//		HazelcastInstance instance = HazelcastClient.newHazelcastClient(config);
//		return new HazelcastCacheManager(instance);
//	}

}
