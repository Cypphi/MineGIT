package ca.modmonster.minegit.data;

import net.minecraft.resources.ResourceLocation;

public enum GitService {
    GITHUB("GitHub", "GitHub", "GitHub", "https://github.com", "https://api.github.com"),
    GITHUB_ORG("GitHub Org", "GitHub Organization", "GitHub", "https://github.com", "https://api.github.com"),
    GITLAB("GitLab", "GitLab", "GitLab", "https://gitlab.com", "https://gitlab.com/api/v4"),
    GITEA("Gitea", "Gitea", "Gitea", null, null),
    FORGEJO("Forgejo", "Forgejo", "Forgejo", null, null),
    CUSTOM("Custom", "Custom Endpoint", "remote", null, null);

    private final String shortName;
    private final String displayName;
    private final String naturalName;
    private final String defaultWebUrl;
    private final String defaultApiUrl;

    GitService(String shortName, String displayName, String naturalName, String defaultWebUrl, String defaultApiUrl) {
        this.shortName = shortName;
        this.displayName = displayName;
        this.naturalName = naturalName;
        this.defaultWebUrl = defaultWebUrl;
        this.defaultApiUrl = defaultApiUrl;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getNaturalName() {
        return naturalName;
    }

    public String getDefaultWebUrl() {
        return defaultWebUrl;
    }

    public String getDefaultApiUrl() {
        return defaultApiUrl;
    }

    public boolean requiresCustomUrl() {
        return this == GITLAB || this == GITEA || this == FORGEJO || this == CUSTOM;
    }

    public boolean supportsAutoRepoCreation() {
        return this != CUSTOM;
    }

    public ResourceLocation getIcon() {
        return new ResourceLocation("minegit", "textures/gui/services/" + getShortName().toLowerCase().replace(" ", "_") + ".png");
    }
}