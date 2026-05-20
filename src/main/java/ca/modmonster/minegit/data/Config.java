package ca.modmonster.minegit.data;

public class Config {
    public final String username;
    public final String patEncrypted;
    private transient String pat;
    public final GitService gitService;
    public final String customWebUrl;
    public final String customApiUrl;

    public Config(String username, String patEncrypted, GitService gitService, String customWebUrl, String customApiUrl) {
        this.username = username;
        this.patEncrypted = patEncrypted;
        this.gitService = gitService;
        this.customWebUrl = customWebUrl;
        this.customApiUrl = customApiUrl;
    }

    public Config() {
        this("", "", GitService.GITHUB, "", "");
    }

    public String getPat() {
        if (pat == null) {
            pat = CryptoManager.decrypt(patEncrypted);
            if (pat == null) pat = "";
        }
        return pat;
    }

    public String getWebUrl() {
        if (gitService.requiresCustomUrl()) {
            return customWebUrl;
        }
        return gitService.getDefaultWebUrl();
    }

    public String getApiUrl() {
        if (gitService.requiresCustomUrl()) {
            return customApiUrl;
        }
        return gitService.getDefaultApiUrl();
    }

    public String buildCloneUrl(String repoName) {
        if (gitService.requiresCustomUrl()) {
            // For custom services, assume the repoName already contains the full path
            // or use the base web URL to construct the clone URL
            String baseUrl = customWebUrl.endsWith("/") ? customWebUrl : customWebUrl + "/";
            if (repoName.endsWith(".git")) {
                return baseUrl + repoName;
            }
            return baseUrl + repoName + ".git";
        }

        String baseUrl = gitService.getDefaultWebUrl();
        return baseUrl + "/" + username + "/" + repoName + ".git";
    }
}