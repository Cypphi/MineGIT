package ca.modmonster.minegit.data;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;
import net.minecraft.resources.Identifier;

public enum GitService {
    GITHUB("https://github.com", "https://api.github.com"),
    GITHUB_ORG("https://github.com", "https://api.github.com"),
    GITLAB("https://gitlab.com", "https://gitlab.com/api/v4"),
    GITEA(null, null),
    FORGEJO(null, null),
    CUSTOM(null, null);

    private final String defaultWebUrl;
    private final String defaultApiUrl;

    GitService(String defaultWebUrl, String defaultApiUrl) {
        this.defaultWebUrl = defaultWebUrl;
        this.defaultApiUrl = defaultApiUrl;
    }

    public String getIdentifier() {
        return toString().toLowerCase();
    }

    public String getShortName() {
        return getTranslation("short_name");
    }

    public String getDisplayName() {
        return getTranslation("display_name");
    }

    public String getNaturalName() {
        return getTranslation("natural_name");
    }

    private String getTranslation(String path) {
        String service = "minegit.service." + getIdentifier();
        if (Language.getInstance().has(service + "." + path)) return I18n.get(service + "." + path);
        return I18n.get(service);
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
        return Identifier.fromNamespaceAndPath("minegit", "services/" + getIdentifier());
    }
}