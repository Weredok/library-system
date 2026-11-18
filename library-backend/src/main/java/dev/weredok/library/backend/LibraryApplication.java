package dev.weredok.library.backend;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.Clock;
import java.time.ZoneId;

@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }

    /**
     * Годинник з часовим поясом бібліотеки. Усі «сьогодні» в коді беруться звідси —
     * так у тестах можна підставити фіксовану дату.
     */
    @Bean
    public Clock clock(@Value("${library.zone}") String zone) {
        return Clock.system(ZoneId.of(zone));
    }
}
