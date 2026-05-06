package ca.modmonster.minegit.backport;

import org.lwjgl.input.Keyboard;

public class ScreenUtil {
    public static boolean isAltDown() {
        return Keyboard.isKeyDown(56) || Keyboard.isKeyDown(184);
    }
}
