package io.mawhebty.support;


import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageSource messageSource;

    public String getMessage(final String code, @Nullable final Object[] args) {
        return this.messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
    }

    public String getMessage(final String code) {
        return this.getMessage(code, null);
    }

    public String getMessage(final String code, final Locale locale, @Nullable final Object[] args) {
        return this.messageSource.getMessage(code, args, locale);
    }
}
