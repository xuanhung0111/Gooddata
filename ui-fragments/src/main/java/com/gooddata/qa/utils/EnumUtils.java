package com.gooddata.qa.utils;

import static java.util.Objects.isNull;

public final class EnumUtils {

    private EnumUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T extends Enum<T>> T lookup(final String name, final Class<T> enumClass, final T defaultValue,
            final String... getValueMethod) {
        if (isNull(name) || name.isEmpty()) {
            return defaultValue;
        }

        try {
            for (final Enum<?> e : enumClass.getEnumConstants()) {
                final String value = getValueMethod.length > 0 ?
                        (String) e.getClass().getMethod(getValueMethod[0]).invoke(e) : e.name(); 
                if (value.equalsIgnoreCase(name))
                    return (T) e;
            }
        } catch (Exception e) {
            // ignore
        }

        return defaultValue;
    }
}
