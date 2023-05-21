package cc.xfl12345.person.cv.pojo;

import java.time.Duration;
import java.util.function.BiFunction;

public class SimpleBucketConfigUtils {
    public static BiFunction<Long, Long, Long> gcd = (a ,b) -> {
        long k;
        do {
            k = a % b;
            a = b;
            b = k;
        } while (k != 0);

        return a;
    };

    public static SimpleBucketConfig createConfigJustInMinutes(long frequency) {
        long k = gcd.apply(frequency, 60L);
        return SimpleBucketConfig.builder()
            .bucketCapacity(frequency)
            .refillToken(frequency / k)
            .refillFrequency(Duration.ofSeconds(60 / k))
            .build();
    }

}
