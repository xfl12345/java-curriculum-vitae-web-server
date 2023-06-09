package cc.xfl12345.person.cv.pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SimpleBucketConfig {

    @Builder.Default
    protected long refillToken = 1;

    @Builder.Default
    protected Duration refillFrequency = Duration.ofMinutes(1);

    @Builder.Default
    protected long bucketCapacity = 10;

}
