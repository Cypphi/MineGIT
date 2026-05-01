package ca.modmonster.minegit.backport;

import net.minecraft.client.gui.Font;

import java.util.List;
import java.util.stream.Collectors;

public interface MultiLineLabel {
    static MultiLineLabel create(Font font, String text, int i) {
        return createFixed(font, font.split(text, i).stream().map((formattedCharSequence) -> new MultiLineLabel.TextWithWidth(formattedCharSequence, font.width(formattedCharSequence))).collect(Collectors.toList()));
    }

    static MultiLineLabel createFixed(final Font font, final List<TextWithWidth> list) {
        return new MultiLineLabel() {
            public void renderCentered(int i, int j) {
                int k = 9;
                int m = j;

                for (TextWithWidth textWithWidth : list) {
                    font.drawShadow(textWithWidth.text, (float) (i - textWithWidth.width / 2), (float) m, 16777215);
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
