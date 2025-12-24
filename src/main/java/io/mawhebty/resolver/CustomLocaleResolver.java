package io.mawhebty.resolver;

import com.google.common.base.Strings;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;


public class CustomLocaleResolver extends AcceptHeaderLocaleResolver {
    private final String defaultLocale;
    private List<String> supportedLocales;
    private List<Locale> locales;

    public CustomLocaleResolver(final String locale) {
        this.defaultLocale = !Strings.isNullOrEmpty(locale) ? locale : "en";
        this.supportedLocales = Arrays.asList("en", "ar", "fr");
        this.locales = this.supportedLocales.stream().map(Locale::new).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        String language = defaultLocale;
        String country = "";

        // Determine language from headers.
        if (request.getHeader("Accept-Language") != null && !request.getHeader("Accept-Language").isEmpty()) {
            language = request.getHeader("Accept-Language");
        }

        if (request.getHeader("Language") != null && !request.getHeader("Language").isEmpty()) {
            language = request.getHeader("Language");
        }

        if (request.getHeader("lang") != null && !request.getHeader("lang").isEmpty()) {
            language = request.getHeader("lang");
        }

        // Fallback to default locale if language is not supported.
        if (language == null || !supportedLocales.contains(language)) {
            return Locale.getDefault();
        }

        if(request.getHeader("country") != null && !request.getHeader("country").isEmpty()) {
            country  = request.getHeader("country");
        }

        if(country.isEmpty()){
            // Use Locale lookup for other supported combinations.
            List<Locale.LanguageRange> list = Locale.LanguageRange.parse(language);
            return Locale.lookup(list, locales);
        }

        return new Locale(language, country);
    }

}

