package io.mawhebty;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.TimeZone;

@SpringBootApplication
public class MawhebtyCoreServiceApplication {

    @PostConstruct
    void started() {
        TimeZone current = TimeZone.getDefault();

        if (!"UTC".equals(current.getID())) {
            System.out.println(">>> [STATIC] Current timezone: " + current.getID() + " — changing to UTC");
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        } else {
            System.out.println(">>> [STATIC] Timezone already UTC — no change needed");
        }
    }
	public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
		SpringApplication.run(MawhebtyCoreServiceApplication.class, args);
	}

}
