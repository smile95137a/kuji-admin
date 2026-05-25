package com.group.admin.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Configuration
public class JacksonDateTimeConfig {

    private static final ZoneId TAIPEI_ZONE = ZoneId.of("Asia/Taipei");

    private static final DateTimeFormatter OUTPUT_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final List<DateTimeFormatter> INPUT_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
    );

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            builder.serializerByType(LocalDateTime.class, new LocalDateTimeSerializer(OUTPUT_FORMATTER));

            builder.deserializerByType(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p,
                                                 DeserializationContext ctxt) throws IOException {
                    String raw = p.getValueAsString();
                    if (raw == null) {
                        return null;
                    }

                    String value = raw.trim();
                    if (value.isEmpty()) {
                        return null;
                    }

                    for (DateTimeFormatter formatter : INPUT_FORMATTERS) {
                        try {
                            return LocalDateTime.parse(value, formatter);
                        } catch (DateTimeParseException ignored) {
                            // Try the next supported format.
                        }
                    }

                    try {
                        return OffsetDateTime.parse(value).atZoneSameInstant(TAIPEI_ZONE).toLocalDateTime();
                    } catch (DateTimeParseException ignored) {
                        // Continue to throw unified error below.
                    }

                    throw ctxt.weirdStringException(
                            value,
                            LocalDateTime.class,
                            "Unsupported datetime format. Expected yyyy-MM-dd HH:mm[:ss], yyyy-MM-dd'T'HH:mm[:ss], or ISO-8601 with timezone"
                    );
                }
            });
        };
    }
}
