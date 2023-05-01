package cc.xfl12345.person.cv;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;

@EnableConfigurationProperties
@SpringBootApplication
public class Main {
    private static ConfigurableApplicationContext context;

    public static int restartCount = 0;

    public static void main(String[] args) {
        context = SpringApplication.run(Main.class, args);
    }

    public static void restart() {
        ApplicationArguments args = context.getBean(ApplicationArguments.class);

        Thread thread = new Thread(() -> {
            context.close();
            context = SpringApplication.run(Main.class, args.getSourceArgs());
        });

        restartCount += 1;
        thread.setDaemon(false);
        thread.setName("restart-" + restartCount);
        thread.start();
    }

}
