package com.fined.mentor.auth.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.function.Supplier;

@Configuration
public class RedisConfig {
        private RedisClient redisClient() {
                String host = System.getenv().getOrDefault("REDIS_HOST", "localhost");
                String portStr = System.getenv().getOrDefault("REDIS_PORT", "6379");
                int port = Integer.parseInt(portStr);
                boolean isSsl = Boolean.parseBoolean(System.getenv().getOrDefault("REDIS_SSL", "false"));

                return RedisClient.create(RedisURI.builder()
                                .withHost(host)
                                .withPort(port)
                                .withSsl(isSsl)
                                .build());
        }

        @Bean
        public ProxyManager<String> lettuceBasedProxyManager() {
                RedisClient redisClient = redisClient();
                StatefulRedisConnection<String, byte[]> redisConnection = redisClient
                                .connect(RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

                return LettuceBasedProxyManager.builderFor(redisConnection)
                                .withExpirationStrategy(
                                                ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(
                                                                Duration.ofMinutes(1L)))
                                .build();
        }

        @Bean
        public Supplier<BucketConfiguration> bucketConfiguration() {
                return () -> BucketConfiguration.builder()
                                .addLimit(Bandwidth.simple(100L, Duration.ofDays(1L)))
                                .build();
        }
}