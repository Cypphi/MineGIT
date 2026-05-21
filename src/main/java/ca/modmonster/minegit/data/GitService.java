package ca.modmonster.minegit.data;

import net.minecraft.resources.Identifier;

public enum GitService {
    GITHUB("GitHub", "GitHub", "https://github.com", "https://api.github.com"),
    GITHUB_ORG("GitHub Org", "GitHub Organization", "https://github.com", "https://api.github.com"),
    GITLAB("GitLab", "GitLab", "https://gitlab.com", "https://gitlab.com/api/v4"),
    GITEA("Gitea", "Gitea", null, null),
    FORGEJO("Forgejo", "Forgejo", null, null),
    CUSTOM("Custom", "Custom Endpoint", null, null);

    private final String shortName;
    private final String displayName;
    private final String defaultWebUrl;
    private final String defaultApiUrl;

    GitService(String shortName, String displayName, String defaultWebUrl, String defaultApiUrl) {
        this.shortName = shortName;
        this.displayName = displayName;
        this.defaultWebUrl = defaultWebUrl;
        this.defaultApiUrl = defaultApiUrl;
    }

    public String getShortName() {
        return shortName;
    }

    public String getDisplayName() {
        return displayName;
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

    public Identifier getIcon() {
        return Identifier.fromNamespaceAndPath("minegit", "services/" + getShortName().toLowerCase().replace(" ", "_"));
    }
}