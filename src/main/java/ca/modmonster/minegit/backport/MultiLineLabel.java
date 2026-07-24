package ca.modmonster.minegit.backport;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.render.font.FontRenderer;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public interface MultiLineLabel {
    static List<String> splitByWidth(FontRenderer font, String text, int width) {
        List<String> lines = new ArrayList<>();

        String[] words = text.split(" ");
        int j = 0;

        String current = "";
        while (j < words.length) {
            // grow line while it still fits
            if (font.stringWidth(current + words[j]) < width) {
                current += words[j++] + " ";
            } else {
                lines.add(current);
                current = "";
            }
        }

        if (!current.trim().isEmpty()) {
            lines.add(current);
        }

        return lines;
    }

    static MultiLineLabel create(Gui gui, FontRenderer font, String text, int i) {
        List<String> lines = splitByWidth(font, text, i);
        List<TextWithWidth> list = lines.stream().map((str) ->
                new TextWithWidth(str, font.stringWidth(str))).collect(Collectors.toList());
        return createFixed(gui, font, list);
    }

    static MultiLineLabel createFixed(final Gui gui, final FontRenderer font, final List<TextWithWidth> list) {
        return new MultiLineLabel() {
            public void renderCentered(int i, int j) {
                int k = 9;
                int m = j;

                for (TextWithWidth textWithWidth : list) {
                    gui.drawStringNoShadow(font, textWithWidth.text, i - textWithWidth.width / 2, m, 16777215);
                    m += k;
                }

            }

            public int getLineCount() {
                return list.size();
            }
        };
    }

    void renderCentered(int i, int j);

    int getLineCount();

    class TextWithWidth {
        private final String text;
        private final int width;

        private TextWithWidth(String text, int i) {
            this.text = text;
            this.width = i;
        }
    }
}
