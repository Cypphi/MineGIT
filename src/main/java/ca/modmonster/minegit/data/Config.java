package ca.modmonster.minegit.data;

public class Config {
    public String username;
    public String patEncrypted;
    private transient String pat;

    public GitService gitService = GitService.GITHUB;
    public String customWebUrl = "";
    public String customApiUrl = "";

    public Config(String username, String patEncrypted) {
        this.username = username;
        this.patEncrypted = patEncrypted;
    }

    public Config() {
        this("", "");
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