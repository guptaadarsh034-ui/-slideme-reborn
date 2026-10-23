package org.fan.slidemeclassic;

import java.net.URI;
import java.util.Locale;

public final class CatalogItem {
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
    public final String signingCertSha256;

    public CatalogItem(String id, String name, String category, String version,
                       int minAndroidApi, String description, String license,
                       String status, String sourceUrl, String downloadUrl, String sha256, String signingCertSha256) {
        this.id = safe(id);
        this.name = safe(name);
        this.category = safe(category);
        this.version = safe(version);
        this.minAndroidApi = Math.max(1, minAndroidApi);
        this.description = safe(description);
        this.license = safe(license);
        this.status = safe(status);
        this.sourceUrl = safe(sourceUrl);
        this.downloadUrl = safe(downloadUrl);
        this.sha256 = safe(sha256).toLowerCase(Locale.ROOT);
        this.signingCertSha256 = safe(signingCertSha256).toLowerCase(Locale.ROOT);
    }

    public boolean hasDownload() {
        return id.matches("[A-Za-z0-9._-]{1,80}")
                && isHttps(downloadUrl)
                && sha256.matches("[0-9a-f]{64}")
                && signingCertSha256.matches("[0-9a-f]{64}");
    }

    public static boolean isHttps(String value) {
        if (value == null || value.length() > 2048 || !value.startsWith("https://")) return false;
        try {
            URI uri = URI.create(value);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null && !uri.getHost().isEmpty()
                    && uri.getUserInfo() == null;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
