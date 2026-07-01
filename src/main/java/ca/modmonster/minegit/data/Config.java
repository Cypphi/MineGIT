package ca.modmonster.minegit.data;

public class Config {
    public final String username;
    public final String patEncrypted;
    private transient String pat;
    public final GitService gitService;

    /**
     * Base website URL used when cloning a repo
     */
    public final String customWebUrl;

    /**
     * Base API URL used when making request to create repo
     */
    public final String customApiUrl;

    /**
     * Whether to skip SSL certificate validation when making requests to the endpoint
     */
    public final boolean ignoreSSL;

    public Config(String username, String patEncrypted, GitService gitService, String customWebUrl, String customApiUrl, boolean ignoreSSL) {
        this.username = username;
        this.patEncrypted = patEncrypted;
        this.gitService = gitService;
        this.customWebUrl = customWebUrl;
        this.customApiUrl = customApiUrl;
        this.ignoreSSL = ignoreSSL;
    }

    public Config() {
        this("", "", GitService.GITHUB, "", "", false);
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

    /**
     * @param repoName Repository name in format [username]/[repo name]
     * @return URL to clone provided repo
     */
    public String buildCloneUrl(String repoName) {
        String apiUrl = getWebUrl();
        String baseUrl = apiUrl.endsWith("/") ? apiUrl : apiUrl + "/";
        return baseUrl + repoName + ".git";
    }
}