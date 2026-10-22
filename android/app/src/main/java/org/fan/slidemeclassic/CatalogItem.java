package org.fan.slidemeclassic;

public class CatalogItem {
    public final String id;
    public final String name;
    public final String category;
    public final String version;
    public final int minAndroidApi;
    public final String description;
    public final String license;
    public final String status;
    public final String sourceUrl;
    public final String downloadUrl;
    public final String sha256;

    public CatalogItem(String id, String name, String category, String version,
                       int minAndroidApi, String description, String license,
                       String status, String sourceUrl, String downloadUrl, String sha256) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.version = version;
        this.minAndroidApi = minAndroidApi;
        this.description = description;
        this.license = license;
        this.status = status;
        this.sourceUrl = sourceUrl;
        this.downloadUrl = downloadUrl;
        this.sha256 = sha256;
    }

    public boolean hasDownload() {
        return downloadUrl != null && downloadUrl.startsWith("https://")
                && sha256 != null && sha256.matches("(?i)[0-9a-f]{64}");
    }
}
