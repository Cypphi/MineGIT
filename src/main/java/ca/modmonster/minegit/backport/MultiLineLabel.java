package ca.modmonster.minegit.backport;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.FormattedText;

import java.util.List;
import java.util.stream.Collectors;

public interface MultiLineLabel {
    static MultiLineLabel create(Font font, FormattedText formattedText, int i) {
        return createFixed(font, font.split(formattedText, i).stream().map((formattedCharSequence) -> new MultiLineLabel.TextWithWidth(formattedCharSequence, font.width(formattedCharSequence))).collect(Collectors.toList()));
    }

    static MultiLineLabel createFixed(final Font font, final List<TextWithWidth> list) {
        return new MultiLineLabel() {
            public int renderCentered(PoseStack poseStack, int i, int j) {
                return this.renderCentered(poseStack, i, j, 9, 16777215);
            }

            public int renderCentered(PoseStack poseStack, int i, int j, int k, int l) {
                int m = j;

                for(TextWithWidth textWithWidth : list) {
                    font.drawShadow(poseStack, textWithWidth.text, (float)(i - textWithWidth.width / 2), (float)m, l);
                    m += k;
                }

                return m;
            }

            public int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l) {
                int m = j;

                for(TextWithWidth textWithWidth : list) {
                    font.drawShadow(poseStack, textWithWidth.text, (float)i, (float)m, l);
                    m += k;
                }

                return m;
            }

            public int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l) {
                int m = j;

                for(TextWithWidth textWithWidth : list) {
                    font.draw(poseStack, textWithWidth.text, (float)i, (float)m, l);
                    m += k;
                }

                return m;
            }

            public int getLineCount() {
                return list.size();
            }
        };
    }

    int renderCentered(PoseStack poseStack, int i, int j);

    int renderCentered(PoseStack poseStack, int i, int j, int k, int l);

    int renderLeftAligned(PoseStack poseStack, int i, int j, int k, int l);

    int renderLeftAlignedNoShadow(PoseStack poseStack, int i, int j, int k, int l);

    int getLineCount();

    class TextWithWidth {
        private final FormattedText text;
        private final int width;

        private TextWithWidth(FormattedText text, int i) {
            this.text = text;
            this.width = i;
        }
    }
}
