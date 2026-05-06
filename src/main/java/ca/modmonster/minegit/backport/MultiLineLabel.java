package ca.modmonster.minegit.backport;

import net.minecraft.client.render.TextRenderer;

import java.util.List;
import java.util.stream.Collectors;

public interface MultiLineLabel {
    static MultiLineLabel create(TextRenderer font, String text, int i) {
        List<String> lines = font.split(text, i);
        List<TextWithWidth> list = lines.stream().map((str) ->
                new TextWithWidth(str, font.getWidth(str))).collect(Collectors.toList());
        return createFixed(font, list);
    }

    static MultiLineLabel createFixed(final TextRenderer font, final List<TextWithWidth> list) {
        return new MultiLineLabel() {
            public void renderCentered(int i, int j) {
                int k = 9;
                int m = j;

                for (TextWithWidth textWithWidth : list) {
                    font.drawWithShadow(textWithWidth.text, i - textWithWidth.width / 2, m, 16777215);
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
