package org.jamesgames.digitalrain.rain;

import java.util.Random;

/**
 * RainDropCharacter is an enum that represents special characters that could be drawn (like by {@link
 * org.jamesgames.digitalrain.rain.RainDropSprite}) to create the digital rain effect.
 *
 * @author James Murphy
 */
enum RainDropCharacter {
    char_00(0xFF61), char_01(0xFF62), char_02(0xFF63), char_03(0xFF64), char_04(0xFF65), char_05(0xFF66),
    char_06(0xFF67), char_07(0xFF68), char_08(0xFF69), char_09(0xFF6A), char_10(0xFF6B), char_11(0xFF6C),
    char_12(0xFF6D), char_13(0xFF6D), char_14(0xFF6E), char_15(0xFF6F), char_16(0xFF71), char_17(0xFF72),
    char_18(0xFF73), char_19(0xFF74), char_20(0xFF75), char_21(0xFF76), char_22(0xFF77), char_23(0xFF78),
    char_24(0xFF79), char_25(0xFF7A), char_26(0xFF7B), char_27(0xFF7C), char_28(0xFF7D), char_29(0xFF7D),
    char_30(0xFF7E), char_31(0xFF7F), char_32(0xFF81), char_33(0xFF82), char_34(0xFF83), char_35(0xFF84),
    char_36(0xFF85), char_37(0xFF86), char_38(0xFF87), char_39(0xFF88), char_40(0xFF89), char_41(0xFF8A),
    char_42(0xFF8B), char_43(0xFF8C), char_44(0xFF8D), char_45(0xFF8D), char_46(0xFF8E), char_47(0xFF8F),
    char_48(0xFF91), char_49(0xFF92), char_50(0xFF93), char_51(0xFF94), char_52(0xFF95), char_53(0xFF96),
    char_54(0xFF97), char_55(0xFF98), char_56(0xFF99), char_57(0xFF9A), char_58(0xFF9B), char_059(0xFF9C),
    char_60(0xFF9D), char_61(0xFF9E), char_62(0xFF9F), char_63('0'), char_64('7'), char_65('8');

    private final char character;

    RainDropCharacter(int character) {
        this.character = (char) character;
    }

    public char getCharacter() {
        return character;
    }

    public static RainDropCharacter randomRainDropCharacter() {
        return values()[randomCharacter.nextInt(values().length)];
    }

    private static final Random randomCharacter = new Random();
}
