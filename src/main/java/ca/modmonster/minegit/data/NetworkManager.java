package ca.modmonster.minegit.data;

import ca.modmonster.minegit.MineGIT;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class NetworkManager {
    public static boolean hasValidCredentials = false;

    private static final HttpClient client;
    static {
        try {
            client = HttpClient.newBuilder().build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HTTP client", e);
        }
    }

    public static int testCredentials(Config config) {
        if (config.gitService.requiresCustomUrl() && config.customApiUrl.isEmpty()) {
            hasValidCredentials = false;
            return -2; // Custom URL required but not provided
        }

        String apiUrl = config.getApiUrl();
        String username = config.username;
        String pat = config.getPat();

        HttpRequest request;

        switch (config.gitService) {
            case GITHUB:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/users/" + username))
                        .header("Authorization", "token " + pat)
                        .GET().build();
                break;
            case GITHUB_ORG:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/orgs/" + username))
                        .header("Authorization", "token " + pat)
                        .GET().build();
                break;
            case GITLAB:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/user"))
                        .header("PRIVATE-TOKEN", pat)
                        .GET().build();
                break;
            case CODEBERG, GITEA, FORGEJO, CUSTOM:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/user"))
                        .header("Authorization", "Bearer " + pat)
                        .GET().build();
                break;
            default:
                hasValidCredentials = false;
                return -1;
        }

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            int status = response.statusCode();
            hasValidCredentials = status == 200;
            if (!hasValidCredentials) MineGIT.LOGGER.error("Error with credentials: {}", response.body());
            return status;
        } catch (IOException | InterruptedException e) {
            MineGIT.LOGGER.error("Error testing credentials!", e);
            hasValidCredentials = false;
            return -1;
        }
    }

    public static HttpResponse<String> createRepo(Config config, String worldId, String worldName) {
        if (config.gitService.requiresCustomUrl() && config.customApiUrl.isEmpty()) {
            return null;
        }

        if (!config.gitService.supportsAutoRepoCreation()) {
            // Custom services don't support auto-repo creation
            return null;
        }

        String apiUrl = config.getApiUrl();
        String pat = config.getPat();
        String encodedName = worldName.replace("\"", "\\\"");

        // remove bad characters from worldId
        worldId = worldId.replace(' ', '-')
                .replaceAll("[^a-zA-Z0-9-_.]", "");

        HttpRequest request;

        switch (config.gitService) {
            case GITHUB:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/user/repos"))
                        .header("Authorization", "token " + pat)
                        .header("X-GitHub-Api-Version", "2026-03-10")
                        .header("Accept", "application/vnd.github+json")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format(
                                "{\"name\":\"minegit_%s\",\"description\":\"Minecraft save for %s. Cloud sync by MineGIT\",\"private\":true}",
                                worldId, encodedName))).build();
                break;
            case GITHUB_ORG:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/orgs/" + config.username + "/repos"))
                        .header("Authorization", "token " + pat)
                        .header("X-GitHub-Api-Version", "2026-03-10")
                        .header("Accept", "application/vnd.github+json")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format(
                                "{\"name\":\"minegit_%s\",\"description\":\"Minecraft save for %s. Cloud sync by MineGIT\",\"private\":true}",
                                worldId, encodedName))).build();
                break;
            case GITLAB:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/projects"))
                        .header("PRIVATE-TOKEN", pat)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format(
                                "{\"name\":\"minegit_%s\",\"description\":\"Minecraft save for %s. Cloud sync by MineGIT\",\"visibility\":\"private\"}",
                                worldId, encodedName))).build();
                break;
            case CODEBERG, GITEA, FORGEJO:
                request = HttpRequest.newBuilder()
                        .uri(URI.create(apiUrl + "/user/repos"))
                        .header("Authorization", "Bearer " + pat)
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(String.format(
                                "{\"name\":\"minegit_%s\",\"description\":\"Minecraft save for %s. Cloud sync by MineGIT\",\"private\":true}",
                                worldId, encodedName))).build();
                break;
            default:
                return null;
        }

        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            return null;
        }
    }

    /**
     * Parse clone URL from repository creation response
     */
    public static String parseCloneUrl(HttpResponse<String> response, GitService service) {
        if (response == null || response.body() == null) {
            return null;
        }

        try {
            var json = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();

            switch (service) {
                case GITHUB, GITHUB_ORG, CODEBERG, GITEA, FORGEJO:
                    if (json.has("clone_url")) {
                        return json.get("clone_url").getAsString();
                    }
                    break;
                case GITLAB:
                    if (json.has("http_url_to_repo")) {
                        return json.get("http_url_to_repo").getAsString();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            // Fall through to return null
        }
        return null;
    }
}