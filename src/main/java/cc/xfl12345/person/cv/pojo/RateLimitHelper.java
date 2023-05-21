package cc.xfl12345.person.cv.pojo;

import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.Nonnull;

import javax.cache.Cache;
import java.util.Map;
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

    public JsonApiResponseData tryConsume(String key) {
        Bucket bucket = justGetCache(key, cache, () -> generateBucket(bucketConfig));
        JsonApiResponseData responseData = new JsonApiResponseData(JsonApiConst.VERSION);
        if (bucket.tryConsume(1)) {
            responseData.setApiResult(JsonApiResult.SUCCEED);
        } else {
            responseData.setApiResult(JsonApiResult.FAILED_FREQUENCY_MAX);
            long nanosTime2Fefill = bucket.estimateAbilityToConsume(1).getNanosToWaitForRefill();
            responseData.setData(Map.of("coolDownRemainder", nanosTime2Fefill / 1000000));
        }

        return responseData;
    }
}
