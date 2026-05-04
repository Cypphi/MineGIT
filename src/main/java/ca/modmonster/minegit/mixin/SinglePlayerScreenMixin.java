package ca.modmonster.minegit.mixin;

import ca.modmonster.minegit.data.Config;
import ca.modmonster.minegit.data.ConfigManager;
import ca.modmonster.minegit.data.GitManager;
import ca.modmonster.minegit.gui.AccountLinkScreen;
import ca.modmonster.minegit.gui.CloneScreen;
import ca.modmonster.minegit.gui.EnableWorldSyncScreen;
import ca.modmonster.minegit.widget.WorldSyncButtonState;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.gui.screen.world.WorldListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.world.level.storage.LevelSummary;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collections;
import java.util.List;

@Mixin(SelectWorldScreen.class)
public class SinglePlayerScreenMixin extends Screen {
    @Unique
    private String cloneButtonTooltip;

    @Shadow
    private @Nullable WorldListWidget levelList;

    @Shadow
    protected TextFieldWidget searchBox;

    protected SinglePlayerScreenMixin(Text title) {
        super(title);
    }

    @Unique @Nullable
    private ButtonWidget cloneButton;

    @Unique @Nullable
    private ButtonWidget worldSyncButton;

    @Unique @Nullable
    private List<String> worldSyncButtonTooltip;

    @Unique
    private WorldSyncButtonState worldSyncButtonState = WorldSyncButtonState.SETUP;

    @Unique @Nullable
    private LevelSummary hoveredLevel;

    @Unique
    private boolean altHeld;

    @Inject(at = @At("TAIL"), method = "init", remap = false)
	private void init(CallbackInfo info) {
        cloneButtonTooltip = I18n.translate("minegit.clone.title");

        // Add world sync button
        worldSyncButton = new ButtonWidget(width / 2 - 178, height - 52, 20, 20, "☁", button -> {
            if (worldSyncButtonState == WorldSyncButtonState.SETUP || altHeld) {
                altHeld = false;
                this.minecraft.openScreen(new AccountLinkScreen(this, () -> {
                    if (this.levelList != null) returnToScreen();
                    updateWorldSyncButton();
                }));
            } else if (worldSyncButtonState == WorldSyncButtonState.ENABLE) {
                if (hoveredLevel != null) this.minecraft.openScreen(new EnableWorldSyncScreen(this, hoveredLevel, () -> {
                    if (this.levelList != null) returnToScreen();
                    updateWorldSyncButton();
                }));
            }
        });
        worldSyncButton.active = false;
        addButton(worldSyncButton);

        // Add clone button
        cloneButton = new ButtonWidget(width / 2 - 178, height - 28, 20, 20, "↓", button -> this.minecraft.openScreen(new CloneScreen(() -> {
            if (this.levelList != null) returnToScreen();
            updateWorldSyncButton();
        })));
        addButton(cloneButton);

        updateWorldSyncButton();
	}

    @Inject(at = @At("TAIL"), method = "render", remap = false)
    public void render(int i, int j, float f, CallbackInfo ci) {
        if (cloneButton != null && cloneButton.isHovered()) renderTooltip(cloneButtonTooltip, i, j);
        if (worldSyncButtonTooltip != null && worldSyncButton != null &&  worldSyncButton.isHovered()) renderTooltip(worldSyncButtonTooltip, i, j);
    }

    @Inject(at = @At("TAIL"), method = "worldSelected", remap = false)
    private void worldSelected(boolean bl, CallbackInfo ci) {
        if (worldSyncButton == null) return;
        if (levelList == null) return;
        hoveredLevel = !bl? null : ((WorldListEntryAccessor) (Object) levelList.getSelected()).getSummary();
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
        } else if (hoveredLevel != null && GitManager.syncEnabled(minecraft, hoveredLevel.getName())) {
            worldSyncButtonState = WorldSyncButtonState.WORLD_CONFIGURE;
            this.worldSyncButton.active = false;
        } else {
            worldSyncButtonState = WorldSyncButtonState.ENABLE;
            this.worldSyncButton.active = hoveredLevel != null;
        }

        worldSyncButton.setMessage(worldSyncButtonState.message);
        worldSyncButtonTooltip = worldSyncButtonState.getTooltip();
        if (cloneButton != null) cloneButton.active = worldSyncButtonState != WorldSyncButtonState.SETUP;
    }

    @Inject(at = @At("HEAD"), method = "keyPressed")
    public void keyPressed(int i, int j, int k, CallbackInfoReturnable<Boolean> cir) {
        if (i == 342) {
            altHeld = true;
            if (worldSyncButton != null) {
                worldSyncButton.active = true;
                worldSyncButton.setMessage("☁");
                worldSyncButtonTooltip = Collections.singletonList(I18n.translate("minegit.link.setup.open"));
            }
        }
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
        WorldListWidget list = ((SelectWorldScreenAccessor) this).getLevelList();
        ((WorldSelectionListInvoker) list).invokeReloadWorldList(() -> searchBox.getText(), true);
        minecraft.openScreen(this);
    }
}