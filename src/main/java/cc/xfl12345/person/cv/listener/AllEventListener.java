package cc.xfl12345.person.cv.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationContextEvent;
import org.springframework.web.context.support.ServletRequestHandledEvent;

@Slf4j
public class AllEventListener implements ApplicationListener<ApplicationEvent> {
    @Override
    public void onApplicationEvent(final ApplicationEvent event) {
        // 忽略请求事件
        if (event instanceof ServletRequestHandledEvent)
            return;
        ApplicationContext applicationContext = null;
        if (event instanceof ApplicationContextEvent) {
            applicationContext = ((ApplicationContextEvent) event).getApplicationContext();
            log.info("当前Spring事件 {} 已触发，我是:{}，我的父容器为：{}",
                event.getClass().getSimpleName(),
                applicationContext,
                applicationContext.getParent()
            );
        } else {
            log.info("当前Spring事件 {} 已触发", event.getClass().getSimpleName());
        }
    }
}
