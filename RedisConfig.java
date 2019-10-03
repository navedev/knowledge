package com.lowes.storeelasticsearch.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
public class RedisConfig {

	@Value("${spring.redis.host}")
	private String redisHost;

	@Value("${spring.redis.port}")
	private String redisPort;

	@Bean
	public JedisConnectionFactory jedisConnectionFactory() {

		JedisConnectionFactory factory = new JedisConnectionFactory();
		factory.getStandaloneConfiguration().setHostName(redisHost);
		factory.getStandaloneConfiguration().setPort(Integer.valueOf(redisPort));
		return factory;
	}

	@Bean
	public RedisCacheManager redisCacheManager() {
		RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration.defaultCacheConfig()
				.disableCachingNullValues().entryTtl(Duration.ofHours(1)).serializeValuesWith(
						RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));
		redisCacheConfiguration.usePrefix();

		return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(jedisConnectionFactory())
				.cacheDefaults(redisCacheConfiguration).build();
	}

}
