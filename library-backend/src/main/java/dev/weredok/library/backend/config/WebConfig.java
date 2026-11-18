package dev.weredok.library.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.TimeUnit;

/**
 * Віддача зібраного GWT. Файли *.cache.* мають хеш у назві — їх можна кешувати назавжди;
 * library.nocache.js завантажувач — не кешуємо, інакше браузер не побачить нову версію.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/library/*.nocache.js")
                .addResourceLocations("classpath:/static/library/")
                .setCacheControl(CacheControl.noStore());
        registry.addResourceHandler("/library/**")
                .addResourceLocations("classpath:/static/library/")
                .setCacheControl(CacheControl.maxAge(365, TimeUnit.DAYS).cachePublic());
    }
}
