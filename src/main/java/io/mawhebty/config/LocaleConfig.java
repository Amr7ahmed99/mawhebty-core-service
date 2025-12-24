package io.mawhebty.config;

import io.mawhebty.resolver.CustomLocaleResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;

@Configuration
@RequiredArgsConstructor
public class LocaleConfig {

    @Value("${DEFAULT_LOCALIZATION_LANGUAGE}")
    private String defaultLocalLang;

    @Bean
    public LocaleResolver localeResolver() {
        final var localeResolver = new CustomLocaleResolver(this.defaultLocalLang);
        localeResolver.setDefaultLocale(Locale.forLanguageTag(this.defaultLocalLang));
        return localeResolver;
    }
}
