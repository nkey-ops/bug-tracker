package com.bluesky.bugtraker.shared.authorizationenum.converter;

import com.bluesky.bugtraker.shared.authorizationenum.Authority;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.stream.Stream;

    @Converter(autoApply = true)
    public class  AuthorityConverter implements AttributeConverter<Authority, String> {

        @Override
        public String convertToDatabaseColumn(Authority authority) {
            if (authority == null) {
                return null;
            }
            return authority.name();
        }

        @Override
        public Authority convertToEntityAttribute(String code) {
            if (code == null) {
                return null;
            }

            return Stream.of(Authority.values())
                    .filter(c -> c.name().equals(code))
                    .findFirst()
                    .orElseThrow(IllegalArgumentException::new);
        }
    }
