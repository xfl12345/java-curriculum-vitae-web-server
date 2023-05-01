package cc.xfl12345.person.cv.log;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConsoleLogFilter extends Filter<ILoggingEvent> {
    protected final static String druidSql = "druid.sql";
    protected final static String druid = "com.alibaba.druid";

    protected HashMap<String, Level> prefixName2Threshold;

    protected HashMap<String, Level> canonicalName2Threshold;

    protected ConcurrentHashMap<String, String> secondLevelCache;

    protected ConcurrentHashMap<String, Object> whiteListCache;

    public ConsoleLogFilter() {
        whiteListCache = new ConcurrentHashMap<>();

        prefixName2Threshold = new HashMap<>(16);
        prefixName2Threshold.put("org.apache.coyote.http11", Level.INFO);
        prefixName2Threshold.put(druidSql, Level.OFF);
        prefixName2Threshold.put(druid, Level.INFO);
        prefixName2Threshold.put("java", Level.INFO);
        prefixName2Threshold.put("javax", Level.INFO);
        prefixName2Threshold.put("sun.rmi", Level.INFO);
        prefixName2Threshold.put("org.apache.tomcat", Level.INFO);
        prefixName2Threshold.put("org.apache.catalina", Level.INFO);
        prefixName2Threshold.put("org.aspectj", Level.INFO);
        prefixName2Threshold.put("com.networknt.schema", Level.INFO);
        prefixName2Threshold.put("org.springframework.validation.beanvalidation", Level.INFO);

        canonicalName2Threshold = new HashMap<>(4);
        canonicalName2Threshold.put("org.apache.catalina.util.LifecycleBase", Level.INFO);
        canonicalName2Threshold.put("org.apache.catalina.mapper.MapperListener", Level.DEBUG);
        canonicalName2Threshold.put("com.zaxxer.hikari.pool.PoolBase", Level.INFO);

        secondLevelCache = new ConcurrentHashMap<>(prefixName2Threshold.size()  << 2);
    }


    @Override
    public FilterReply decide(ILoggingEvent event) {
        String loggerName = event.getLoggerName();
        Level level = event.getLevel();

        // 先看看有没有白名单缓存
        if (whiteListCache.containsKey(loggerName)) {
            return FilterReply.NEUTRAL;
        }

        // 完全限定名称匹配
        Level canonicalNameMatchLevel = canonicalName2Threshold.get(loggerName);
        if (canonicalNameMatchLevel != null) {
            return level.isGreaterOrEqual(canonicalNameMatchLevel) ?
                FilterReply.ACCEPT : FilterReply.DENY;
        }

        // 前缀匹配
        // 看看有没有缓存
        String prefix = secondLevelCache.get(loggerName);
        if (prefix == null) {
            for (String loggerPrefix : prefixName2Threshold.keySet()) {
                if (isMatch(loggerPrefix, loggerName)) {
                    // 缓存起来
                    secondLevelCache.put(loggerName, loggerPrefix);
                    return level.isGreaterOrEqual(prefixName2Threshold.get(loggerPrefix)) ?
                        FilterReply.ACCEPT : FilterReply.DENY;
                }
            }

            // 如果被匹配了，就不会到这一行了
            // 加入白名单缓存
            whiteListCache.put(loggerName, "");
        } else {
            return level.isGreaterOrEqual(prefixName2Threshold.get(prefix)) ?
                FilterReply.ACCEPT : FilterReply.DENY;
        }

        return FilterReply.NEUTRAL;
    }

    protected boolean isMatch(String prefix, String name) {
        // 被检字符串 都没有 前缀 长，直接淘汰
        if (name == null || name.length() < prefix.length()) {
            return false;
        }

        int prefixLength = prefix.length();
        if (prefixLength <= 6) {
            for (int i = 0; i < prefixLength; i++) {
                if (prefix.charAt(i) != name.charAt(i)) {
                    return false;
                }
            }

            return true;
        }

        boolean result = true;
        // 判断总长度是否是奇数，以减少一次比较。
        boolean odd = (prefixLength & 0x1) == 0x1;
        int end = prefixLength - 1;
        int mid = prefixLength / 2;
        // 两头向中间靠拢，一定程度上提高碰撞概率
        for (int i = 0, currentEnd = end; i < mid; i++, currentEnd--) {
            if (prefix.charAt(i) != name.charAt(i)) {
                result = false;
                break;
            }

            if (prefix.charAt(currentEnd) != name.charAt(currentEnd)) {
                result = false;
                break;
            }
        }

        if (result && odd) {
            result = prefix.charAt(mid) == name.charAt(mid);
        }

        return result;
    }
}
