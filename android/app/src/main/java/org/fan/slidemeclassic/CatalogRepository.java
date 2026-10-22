package org.fan.slidemeclassic;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public final class CatalogRepository {
    private CatalogRepository() { }

    public static List<CatalogItem> loadBundled(Context context) throws Exception {
        InputStream input = context.getAssets().open("catalog.json");
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int count;
        try {
            while ((count = input.read(buffer)) != -1) output.write(buffer, 0, count);
        } finally {
            input.close();
        }
        JSONObject root = new JSONObject(output.toString("UTF-8"));
        JSONArray apps = root.getJSONArray("apps");
        List<CatalogItem> result = new ArrayList<CatalogItem>();
        for (int i = 0; i < apps.length(); i++) {
            JSONObject item = apps.getJSONObject(i);
            result.add(new CatalogItem(
                    item.optString("id", ""),
                    item.optString("name", "Unnamed app"),
                    item.optString("category", "Other"),
                    item.optString("version", ""),
                    item.optInt("minAndroidApi", 15),
                    item.optString("description", ""),
                    item.optString("license", "Unknown"),
                    item.optString("status", "untested"),
                    item.optString("sourceUrl", ""),
                    item.optString("downloadUrl", ""),
                    item.optString("sha256", "")
            ));
        }
        return result;
    }
}
