package cc.xfl12345.person.cv.pojo;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.Nonnull;

import javax.cache.Cache;
import java.util.function.Supplier;

public class RateLimitHelper {

    protected Cache<String, Bucket> cache;

    protected SimpleBucketConfig bucketConfig;

    public RateLimitHelper(@Nonnull Cache<String, Bucket> cache, SimpleBucketConfig bucketConfig) {
        this.cache = cache;
        this.bucketConfig = bucketConfig;
    }



    protected Bucket generateBucket(SimpleBucketConfig config) {
        Refill refill = Refill.intervally(config.getRefillToken(), config.getRefillFrequency());
        Bandwidth limit = Bandwidth.classic(config.getBucketCapacity(), refill);

        return Bucket.builder().addLimit(limit).build();
    }

    protected <Key, Value> Value justGetCache(Key key, Cache<Key, Value> cache, Supplier<Value> factory) {
        Value obj = cache.get(key);
        if (obj == null) {
            cache.putIfAbsent(key, factory.get());
            obj = cache.get(key);
        }

        return obj;
    }

    public ConsumeResult tryConsume(String key) {
        Bucket bucket = justGetCache(key, cache, () -> generateBucket(bucketConfig));
        ConsumeResult consumeResult = new ConsumeResult();
        consumeResult.setSuccess(bucket.tryConsume(1));
        long nanosTime2Fefill = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
        // 剩余冷却时间（单位：毫秒）
        consumeResult.setCoolDownRemainder(nanosTime2Fefill / 1000000);

        return consumeResult;
    }

    public static class ConsumeResult {
        private boolean success = false;

        private long coolDownRemainder = 0;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public long getCoolDownRemainder() {
            return coolDownRemainder;
        }

        public void setCoolDownRemainder(long coolDownRemainder) {
            this.coolDownRemainder = coolDownRemainder;
        }
    }
}
