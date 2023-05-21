package cc.xfl12345.person.cv.pojo;

import cc.xfl12345.person.cv.appconst.JsonApiConst;
import cc.xfl12345.person.cv.appconst.JsonApiResult;
import cc.xfl12345.person.cv.pojo.response.JsonApiResponseData;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.annotation.Nonnull;

import javax.cache.Cache;
import java.time.Duration;
import java.util.Map;
import java.util.function.Supplier;

public class RateLimitHelper {

    protected Cache<String, Bucket> cache;

    protected long frequencyInMinute;

    public RateLimitHelper(@Nonnull Cache<String, Bucket> cache, long frequencyInMinute) {
        this.cache = cache;
        this.frequencyInMinute = frequencyInMinute;
    }


    protected long gcd(long a, long b) {
        long k;
        do {
            k = a % b;
            a = b;
            b = k;
        } while (k != 0);

        return a;
    }

    protected Bucket generateBucket(long frequenceInMinute) {
        long k = gcd(frequenceInMinute, 60);

        Refill refill = Refill.intervally(frequenceInMinute / k, Duration.ofSeconds(60 / k));
        Bandwidth limit = Bandwidth.classic(frequenceInMinute, refill);

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
        Bucket bucket = justGetCache(key, cache, () -> generateBucket(frequencyInMinute));
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