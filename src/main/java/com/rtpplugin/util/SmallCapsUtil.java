package com.rtpplugin.util;

public class SmallCapsUtil {

    private static final char[] SMALL_CAPS = new char[128];

    static {
        // Initialize with identity mapping
        for (int i = 0; i < 128; i++) {
            SMALL_CAPS[i] = (char) i;
        }
        // Lowercase small caps mappings
        SMALL_CAPS['a'] = '\u1D00'; // ᴀ
        SMALL_CAPS['b'] = '\u0299'; // ʙ
        SMALL_CAPS['c'] = '\u1D04'; // ᴄ
        SMALL_CAPS['d'] = '\u1D05'; // ᴅ
        SMALL_CAPS['e'] = '\u1D07'; // ᴇ
        SMALL_CAPS['f'] = '\u0493'; // ғ
        SMALL_CAPS['g'] = '\u0262'; // ɢ
        SMALL_CAPS['h'] = '\u029C'; // ʜ
        SMALL_CAPS['i'] = '\u026A'; // ɪ
        SMALL_CAPS['j'] = '\u1D0A'; // ᴊ
        SMALL_CAPS['k'] = '\u1D0B'; // ᴋ
        SMALL_CAPS['l'] = '\u029F'; // ʟ
        SMALL_CAPS['m'] = '\u1D0D'; // ᴍ
        SMALL_CAPS['n'] = '\u0274'; // ɴ
        SMALL_CAPS['o'] = '\u1D0F'; // ᴏ
        SMALL_CAPS['p'] = '\u1D18'; // ᴘ
        SMALL_CAPS['q'] = '\u01EB'; // ǫ
        SMALL_CAPS['r'] = '\u0280'; // ʀ
        SMALL_CAPS['s'] = 's';      // s (no good small cap)
        SMALL_CAPS['t'] = '\u1D1B'; // ᴛ
        SMALL_CAPS['u'] = '\u1D1C'; // ᴜ
        SMALL_CAPS['v'] = '\u1D20'; // ᴠ
        SMALL_CAPS['w'] = '\u1D21'; // ᴡ
        SMALL_CAPS['x'] = 'x';      // x (no good small cap)
        SMALL_CAPS['y'] = '\u028F'; // ʏ
        SMALL_CAPS['z'] = '\u1D22'; // ᴢ
        // Uppercase maps to same small caps
        SMALL_CAPS['A'] = '\u1D00';
        SMALL_CAPS['B'] = '\u0299';
        SMALL_CAPS['C'] = '\u1D04';
        SMALL_CAPS['D'] = '\u1D05';
        SMALL_CAPS['E'] = '\u1D07';
        SMALL_CAPS['F'] = '\u0493';
        SMALL_CAPS['G'] = '\u0262';
        SMALL_CAPS['H'] = '\u029C';
        SMALL_CAPS['I'] = '\u026A';
        SMALL_CAPS['J'] = '\u1D0A';
        SMALL_CAPS['K'] = '\u1D0B';
        SMALL_CAPS['L'] = '\u029F';
        SMALL_CAPS['M'] = '\u1D0D';
        SMALL_CAPS['N'] = '\u0274';
        SMALL_CAPS['O'] = '\u1D0F';
        SMALL_CAPS['P'] = '\u1D18';
        SMALL_CAPS['Q'] = '\u01EB';
        SMALL_CAPS['R'] = '\u0280';
        SMALL_CAPS['S'] = 's';
        SMALL_CAPS['T'] = '\u1D1B';
        SMALL_CAPS['U'] = '\u1D1C';
        SMALL_CAPS['V'] = '\u1D20';
        SMALL_CAPS['W'] = '\u1D21';
        SMALL_CAPS['X'] = 'x';
        SMALL_CAPS['Y'] = '\u028F';
        SMALL_CAPS['Z'] = '\u1D22';
    }

    private SmallCapsUtil() {}

    /**
     * Converts text to small caps, preserving Minecraft color codes (& and §).
     */
    public static String convert(String input) {
        if (input == null || input.isEmpty()) return input;

        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            // Skip color codes - preserve & or § and the following character
            if ((c == '&' || c == '\u00A7') && i + 1 < input.length()) {
                result.append(c);
                result.append(input.charAt(i + 1));
                i++; // skip next char
                continue;
            }
            // Convert ASCII letters
            if (c < 128) {
                result.append(SMALL_CAPS[c]);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
}
