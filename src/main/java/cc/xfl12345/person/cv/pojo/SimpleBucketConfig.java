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

    protected long refillToken = 1;

    protected Duration refillFrequency = Duration.ofMinutes(1);

    protected long bucketCapacity = 10;

}
