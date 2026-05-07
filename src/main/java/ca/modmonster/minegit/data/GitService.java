package ca.modmonster.minegit.data;

public enum GitService {
    GITHUB("GitHub", "https://github.com", "https://api.github.com"),
    GITLAB("GitLab", "https://gitlab.com", "https://gitlab.com/api/v4"),
    GITEA("Gitea/Forgejo", null, null), 
    CUSTOM("Custom", null, null); 

    private final String displayName;
    private final String defaultWebUrl;
    private final String defaultApiUrl;

    GitService(String displayName, String defaultWebUrl, String defaultApiUrl) {
        this.displayName = displayName;
        this.defaultWebUrl = defaultWebUrl;
        this.defaultApiUrl = defaultApiUrl;
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
        return this == GITEA || this == CUSTOM;
    }

    public boolean supportsAutoRepoCreation() {
        return this == GITHUB || this == GITLAB || this == GITEA;
    }
}