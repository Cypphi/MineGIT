package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.backport.ImageButton;
import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import ca.modmonster.minegit.gui.EnableWorldSyncScreen;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldSelectionEntry;
import net.minecraft.client.gui.screen.world.WorldSelectionList;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.storage.WorldSaveInfo;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collections;
import java.util.List;

@Mixin(SelectWorldScreen.class)
public class SinglePlayerScreenMixin extends Screen {
    @Unique
    private String cloneButtonTooltip;

    @Shadow
    private @Nullable WorldSelectionList worldList;

    @Unique @Nullable
    private ButtonWidget cloneButton;

    @Unique @Nullable
    private ImageButton worldSyncButton;

    @Unique @Nullable
    private List<String> worldSyncButtonTooltip;

    @Unique
    private WorldSyncButtonState worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique @Nullable
    private WorldSaveInfo hoveredLevel;

    @Unique
    private boolean altHeld;

    @Inject(at = @At("TAIL"), method = "init", remap = false)
	private void init(CallbackInfo info) {
        cloneButtonTooltip = I18n.translate("minegit.clone.title");

        // Add world sync button
        worldSyncButton = new ImageButton(100, width / 2 - 178, height - 52, ImageButton.ImageButtonTex.CLOUD) {
            @Override
            public void click(double mouseX, double mouseY) {
                if (worldSyncButtonState == WorldSyncButtonState.SETUP || altHeld) {
                    altHeld = false;
                    minecraft.openScreen(new AccountLinkScreen(SinglePlayerScreenMixin.this, () -> {
                        if (worldList != null) returnToScreen();
                        updateWorldSyncButton();
                    }));
                } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                    if (hoveredLevel != null)
                        minecraft.openScreen(new EnableWorldSyncScreen(SinglePlayerScreenMixin.this, hoveredLevel, () -> {
                            if (worldList != null) returnToScreen();
                            updateWorldSyncButton();
                        }));
                }
            }
        };
        worldSyncButton.active = false;
        addButton(worldSyncButton);

        // Add clone button
        cloneButton = new ImageButton(101, width / 2 - 178, height - 28, ImageButton.ImageButtonTex.CLONE) {
            @Override
            public void click(double mouseX, double mouseY) {
                minecraft.openScreen(new CloneScreen(() -> {
                    if (worldList != null) returnToScreen();
                    updateWorldSyncButton();
                }));
            }
        };
        addButton(cloneButton);

        hoveredLevel = null;
        updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && cloneButton.isHovered()) renderTooltip(cloneButtonTooltip, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null &&  worldSyncButton.isHovered()) renderTooltip(worldSyncButtonTooltip, i, j);
    }

    @Inject(at = @At("TAIL"), method = "updateButtons", remap = false)
    private void worldSelected(WorldSelectionEntry selectedWorld, CallbackInfo ci) {
        if (worldSyncButton == null) return;
        if (worldList == null) return;
        hoveredLevel = selectedWorld == null? null : ((WorldListEntryAccessor) (Object) selectedWorld).getSummary();
        updateWorldSyncButton();
    }

    @Unique
    private void updateWorldSyncButton() {
        if (worldSyncButton == null) return;
        if (altHeld) return;
        Config config = ConfigManager.getCurrentConfig();
        if (config.username.replace(" ", "").isEmpty() || config.getPat().replace(" ", "").isEmpty()) {
            // Set the world sync button to configuration state
            worldSyncButtonState = WorldSyncButtonState.SETUP;
            this.worldSyncButton.active = true;
        } else if (hoveredLevel != null && GitManager.syncEnabled(minecraft, hoveredLevel.getSaveName())) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.active = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.active = hoveredLevel != null;
        }

        worldSyncButton.texture = worldSyncButtonState.texture;
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.active = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        if (i == 342) {
            altHeld = true;
            if (worldSyncButton != null) {
                worldSyncButton.active = true;
                worldSyncButton.texture = ImageButton.ImageButtonTex.CLOUD;
                worldSyncButtonTooltip = Collections.singletonList(I18n.translate("minegit.link.setup.open"));
            }
        }
        return false;
    }

    @Override
    public boolean keyReleased(int i, int j, int k) {
        if (i == 342) {
            altHeld = false;
            updateWorldSyncButton();
        }
        return super.keyReleased(i, j, k);
    }

    @Unique
    private void returnToScreen() {
        minecraft.openScreen(this);
    }
}