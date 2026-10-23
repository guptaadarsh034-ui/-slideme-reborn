package org.fan.slidemeclassic;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.net.ssl.HttpsURLConnection;

public final class CatalogRepository {
    private static final int MAX_CATALOG_BYTES = 2 * 1024 * 1024;
    private static final String PREFS = "catalog_cache";
    private static final String CATALOG_KEY = "json";
    private CatalogRepository() { }

    public static List<CatalogItem> loadBundled(Context context) throws Exception {
        try (InputStream input = context.getAssets().open("catalog.json")) {
            return parse(readLimited(input));
        }
    }

    public static List<CatalogItem> loadCached(Context context) {
        String cached = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(CATALOG_KEY, "");
        if (cached.isEmpty()) return new ArrayList<CatalogItem>();
        try { return parse(cached); } catch (Exception ignored) { return new ArrayList<CatalogItem>(); }
    }

    public static List<CatalogItem> refreshRemote(Context context, String address) throws Exception {
        if (!CatalogItem.isHttps(address)) throw new IllegalArgumentException("Catalog URL must use HTTPS");
        URL current = new URL(address);
        String json = null;
        for (int redirects = 0; redirects <= 4; redirects++) {
            if (!"https".equalsIgnoreCase(current.getProtocol()) || current.getUserInfo() != null) {
                throw new java.io.IOException("Catalog redirect must remain HTTPS");
            }
            HttpsURLConnection connection = (HttpsURLConnection) current.openConnection();
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(15000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("User-Agent", "SlideME-Reborn/0.3");
            int status = connection.getResponseCode();
            if (status >= 200 && status < 300) {
                try (InputStream input = connection.getInputStream()) {
                    json = readLimited(input);
                } finally {
                    connection.disconnect();
                }
                break;
            }
            if (status == 301 || status == 302 || status == 303 || status == 307 || status == 308) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null || redirects == 4) throw new java.io.IOException("Too many catalog redirects");
                current = new URL(current, location);
                continue;
            }
            connection.disconnect();
            throw new java.io.IOException("Catalog server returned HTTP " + status);
        }
        if (json == null) throw new java.io.IOException("Catalog download failed");
        List<CatalogItem> parsed = parse(json);
        if (parsed.isEmpty()) throw new java.io.IOException("Remote catalog contained no valid entries");
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(CATALOG_KEY, json).apply();
        return parsed;
    }

    public static List<CatalogItem> parse(String json) throws Exception {
        if (json == null || json.length() > MAX_CATALOG_BYTES) {
            throw new IllegalArgumentException("Catalog is empty or too large");
        }
        JSONObject root = new JSONObject(json);
        JSONArray apps = root.getJSONArray("apps");
        if (apps.length() > 5000) throw new IllegalArgumentException("Catalog has too many entries");
        List<CatalogItem> result = new ArrayList<CatalogItem>();
        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < apps.length(); i++) {
            JSONObject item = apps.optJSONObject(i);
            if (item == null) continue;
            String id = item.optString("id", "").trim();
            String name = item.optString("name", "").trim();
            if (!id.matches("[A-Za-z0-9._-]{1,80}") || name.isEmpty() || name.length() > 120
                    || !ids.add(id)) continue;
            result.add(new CatalogItem(
                    id, name,
                    item.optString("category", "Other"),
                    item.optString("version", ""),
                    item.optInt("minAndroidApi", 29),
                    item.optString("description", ""),
                    item.optString("license", "Unknown"),
                    item.optString("status", "unverified"),
                    item.optString("sourceUrl", ""),
                    item.optString("downloadUrl", ""),
                    item.optString("sha256", "")
            ));
        }
        return result;
    }

    private static String readLimited(InputStream input) throws Exception {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int count;
            int total = 0;
            while ((count = input.read(buffer)) != -1) {
                total += count;
                if (total > MAX_CATALOG_BYTES) throw new IllegalArgumentException("Catalog exceeds 2 MB");
                output.write(buffer, 0, count);
            }
            return output.toString("UTF-8");
        }
    }
}
