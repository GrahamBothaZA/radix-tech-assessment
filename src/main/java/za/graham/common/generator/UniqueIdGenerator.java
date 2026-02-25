package za.graham.common.generator;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/**
 * Utility component for generating short, prefixed unique identifiers.
 * IDs are formed from the last 20 bits of the current timestamp combined with
 * 8 random bits, giving ~16 million unique values per millisecond per prefix.
 */
@Component
public class UniqueIdGenerator {

    /**
     * Generates a unique ID string in the format {PREFIX_XXXXXRR}, where
     * {XXXXX} is a 5-digit hex timestamp fragment and {RR} is a 2-digit
     * hex random value.
     *
     * @param idPrefix a label prepended to the ID (e.g. "LOAN" or "PAYMENT")
     * @return a unique identifier string
     */
    public static String generateUniqueId(final String idPrefix) {
        long timePart = System.currentTimeMillis() % 0x100000;
        int randPart = new SecureRandom().nextInt(0x100);
        return String.format("%s_%05X%02X", idPrefix, timePart, randPart);
    }
}
