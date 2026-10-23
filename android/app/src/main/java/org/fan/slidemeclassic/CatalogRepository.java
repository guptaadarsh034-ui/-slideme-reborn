package org.fan.slidemeclassic;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CatalogRepository {
    private static final int MAX_CATALOG_BYTES = 2 * 1024 * 1024;
    private CatalogRepository() { }

    public static List<CatalogItem> loadBundled(Context context) throws Exception {
        try (InputStream input = context.getAssets().open("catalog.json")) {
            return parse(readLimited(input));
        }
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
            CatalogItem parsed = new CatalogItem(
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
            );
            result.add(parsed);
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
