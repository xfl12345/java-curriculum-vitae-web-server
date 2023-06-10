package cc.xfl12345.person.cv.pojo;

import lombok.*;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebResourceMapping {
    @Getter
    @Setter
    protected String pathPattern;

    @Getter
    @Setter
    protected String resourceLocation;
}
