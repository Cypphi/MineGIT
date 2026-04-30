package ca.modmonster.minegit.data;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class NetworkManager {
    public static boolean hasValidCredentials = false;

    public static int testCredentials(String username, String pat) {
        URL url;
        try {
            url = new URL("https://api.github.com/users/" + username);
        } catch (MalformedURLException e) {
            hasValidCredentials = false;
            return -1;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Authorization", "token " + pat);

            int status = connection.getResponseCode();
            hasValidCredentials = status == 200;

            return status;
        } catch (IOException e) {
            hasValidCredentials = false;
            return -1;
        }
    }

    public static HttpResponse createRepo(String pat, String worldId, String worldName) {
        URL url;
        try {
            url = new URL("https://api.github.com/user/repos");
        } catch (MalformedURLException e) {
            return null;
        }

        try {
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setDoOutput(true);

            connection.setRequestProperty("Authorization", "token " + pat);
            connection.setRequestProperty("X-GitHub-Api-Version", "2026-03-10");
            connection.setRequestProperty("Accept", "application/vnd.github+json");

            String json = String.format("{\"name\":\"minegit_%s\",\"description\":\"Minecraft save for %s. Cloud sync by MineGIT\",\"private\":true}", worldId, worldName);
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = json.getBytes(StandardCharsets.UTF_8);
                os.write(input);
                os.flush();
            }

            int status = connection.getResponseCode();

            InputStream stream = (status >= 200 && status < 300)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(stream, StandardCharsets.UTF_8)
            );

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();

            String body = response.toString();
            return new HttpResponse(status, body);
        } catch (IOException e) {
            return null;
        }
    }

    public static class HttpResponse {
        private final int statusCode;
        private final String body;

        public HttpResponse(int statusCode, String body) {
            this.statusCode = statusCode;
            this.body = body;
        }

        public int statusCode() {
            return this.statusCode;
        }

        public String body() {
            return this.body;
        }
    }
}
