package org.fan.slidemeclassic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLEncoder;
import javax.net.ssl.HttpsURLConnection;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MainActivity extends Activity {
    private final List<CatalogItem> allItems = new ArrayList<CatalogItem>();
    private final List<CatalogItem> visibleItems = new ArrayList<CatalogItem>();
    private final ExecutorService io = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());
    private ArrayAdapter<String> adapter;
    private EditText searchBox;
    private TextView statusText;
    private String selectedCategory = "All";
    private AlertDialog progressDialog;
    private Future<?> downloadTask;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildLayout();
        loadCatalog();
    }

    @Override protected void onDestroy() {
        io.shutdownNow();
        if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
        super.onDestroy();
    }

    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(245, 246, 241));
        int pad = dp(12);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("SlideME Reborn");
        title.setTextColor(Color.WHITE);
        title.setTextSize(24);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(14), 0, dp(14), 0);
        title.setBackgroundColor(Color.rgb(55, 89, 40));
        root.addView(title, new LinearLayout.LayoutParams(-1, dp(58)));

        TextView subtitle = new TextView(this);
        subtitle.setText("Offline catalog + Uptodown discovery");
        subtitle.setTextColor(Color.rgb(80, 88, 75));
        subtitle.setTextSize(13);
        subtitle.setPadding(dp(2), dp(10), 0, dp(8));
        root.addView(subtitle);

        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setTextSize(16);
        searchBox.setHint("Search apps and games");
        searchBox.setPadding(dp(12), 0, dp(12), 0);
        root.addView(searchBox, new LinearLayout.LayoutParams(-1, dp(48)));

        LinearLayout sourceRow = new LinearLayout(this);
        sourceRow.setOrientation(LinearLayout.HORIZONTAL);
        sourceRow.setPadding(0, dp(4), 0, dp(4));
        addSourceButton(sourceRow, "Uptodown Apps", "https://en.uptodown.com/android/apps");
        addSourceButton(sourceRow, "Uptodown Games", "https://en.uptodown.com/android/games");
        Button searchSource = new Button(this);
        searchSource.setText("Search Uptodown");
        searchSource.setTextSize(10);
        searchSource.setAllCaps(false);
        searchSource.setOnClickListener(v -> openUptodownSearch());
        sourceRow.addView(searchSource, new LinearLayout.LayoutParams(0, dp(42), 1));
        root.addView(sourceRow);
        searchBox.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { filterItems(); }
            @Override public void afterTextChanged(android.text.Editable s) { }
        });

        LinearLayout categories = new LinearLayout(this);
        categories.setOrientation(LinearLayout.HORIZONTAL);
        categories.setPadding(0, dp(6), 0, dp(6));
        addCategoryButton(categories, "All");
        addCategoryButton(categories, "Tools");
        addCategoryButton(categories, "Games");
        addCategoryButton(categories, "Education");
        root.addView(categories);

        ListView listView = new ListView(this);
        listView.setDividerHeight(dp(1));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, new ArrayList<String>()) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView primary = row.findViewById(android.R.id.text1);
                TextView secondary = row.findViewById(android.R.id.text2);
                CatalogItem item = visibleItems.get(position);
                primary.setText(item.name);
                primary.setTextColor(Color.rgb(35, 42, 31));
                primary.setTextSize(17);
                primary.setTypeface(null, android.graphics.Typeface.BOLD);
                secondary.setText(item.category + "  •  v" + item.version + "  •  Android " + item.minAndroidApi + "+");
                secondary.setTextColor(Color.rgb(98, 102, 94));
                secondary.setTextSize(12);
                row.setPadding(dp(8), dp(5), dp(8), dp(5));
                return row;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> showDetails(visibleItems.get(position)));
        root.addView(listView, new LinearLayout.LayoutParams(-1, 0, 1));

        statusText = new TextView(this);
        statusText.setText("Offline catalog ready");
        statusText.setTextColor(Color.rgb(100, 105, 95));
        statusText.setTextSize(11);
        statusText.setGravity(Gravity.CENTER);
        statusText.setPadding(0, dp(8), 0, dp(2));
        root.addView(statusText);
        setContentView(root);
    }

    private void addSourceButton(LinearLayout row, String label, String url) {
        Button button = new Button(this);
        button.setText(label);
        button.setTextSize(10);
        button.setAllCaps(false);
        button.setOnClickListener(v -> openHttps(url));
        row.addView(button, new LinearLayout.LayoutParams(0, dp(42), 1));
    }

    private void openUptodownSearch() {
        String query = searchBox == null ? "" : searchBox.getText().toString().trim();
        if (query.isEmpty()) {
            openHttps("https://en.uptodown.com/android/apps");
            return;
        }
        try {
            openHttps("https://www.google.com/search?q=" + URLEncoder.encode("site:en.uptodown.com/android " + query, "UTF-8"));
        } catch (Exception e) {
            Toast.makeText(this, "Could not prepare the search.", Toast.LENGTH_SHORT).show();
        }
    }

    private void addCategoryButton(LinearLayout row, final String category) {
        Button button = new Button(this);
        button.setText(category);
        button.setTextSize(11);
        button.setAllCaps(false);
        button.setOnClickListener(v -> {
            selectedCategory = category;
            filterItems();
        });
        row.addView(button, new LinearLayout.LayoutParams(0, dp(40), 1));
    }

    private void loadCatalog() {
        try {
            allItems.clear();
            allItems.addAll(CatalogRepository.loadBundled(this));
            List<CatalogItem> cached = CatalogRepository.loadCached(this);
            if (!cached.isEmpty()) {
                allItems.clear();
                allItems.addAll(cached);
            }
            filterItems();
            statusText.setText(allItems.size() + " catalog entries • checking for updates…");
        } catch (Exception e) {
            statusText.setText("Catalog could not be loaded");
            Toast.makeText(this, "Could not load the offline catalog.", Toast.LENGTH_LONG).show();
            return;
        }

        io.execute(() -> {
            try {
                List<CatalogItem> fresh = CatalogRepository.refreshRemote(this,
                        "https://raw.githubusercontent.com/guptaadarsh034-ui/-slideme-reborn/step-1-native-mvp/android/app/src/main/assets/catalog.json");
                main.post(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    allItems.clear();
                    allItems.addAll(fresh);
                    filterItems();
                    statusText.setText(fresh.size() + " catalog entries • updated online");
                });
            } catch (Exception ignored) {
                main.post(() -> {
                    if (isFinishing() || isDestroyed()) return;
                    statusText.setText(allItems.size() + " catalog entries • offline/cached mode");
                });
            }
        });
    }

    private void filterItems() {
        if (adapter == null) return;
        String query = searchBox == null ? "" : searchBox.getText().toString().trim().toLowerCase(Locale.ROOT);
        visibleItems.clear();
        for (CatalogItem item : allItems) {
            boolean categoryMatch = "All".equals(selectedCategory) || selectedCategory.equalsIgnoreCase(item.category);
            boolean textMatch = item.name.toLowerCase(Locale.ROOT).contains(query)
                    || item.description.toLowerCase(Locale.ROOT).contains(query)
                    || item.category.toLowerCase(Locale.ROOT).contains(query);
            if (categoryMatch && textMatch) visibleItems.add(item);
        }
        List<String> labels = new ArrayList<String>();
        for (CatalogItem item : visibleItems) labels.add(item.name);
        adapter.clear();
        adapter.addAll(labels);
        adapter.notifyDataSetChanged();
    }

    private void showDetails(final CatalogItem item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(item.name);
        String details = item.description + "\n\nCategory: " + item.category
                + "\nVersion: " + item.version
                + "\nMinimum Android API: " + item.minAndroidApi
                + "\nLicence: " + item.license
                + "\nAvailability: " + item.status
                + (item.hasDownload() ? "\nSHA-256 verification: required" : "\nNo verified APK is configured.");
        builder.setMessage(details);
        if (item.hasDownload()) {
            builder.setPositiveButton("Download & verify", (dialog, which) -> startDownload(item));
        } else if (item.sourceUrl != null && item.sourceUrl.startsWith("https://")) {
            builder.setPositiveButton("Open source page", (dialog, which) -> openHttps(item.sourceUrl));
        } else {
            builder.setPositiveButton("Not available yet", null);
        }
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private void startDownload(final CatalogItem item) {
        if (android.os.Build.VERSION.SDK_INT >= 26 && !getPackageManager().canRequestPackageInstalls()) {
            new AlertDialog.Builder(this)
                    .setTitle("Allow app installs")
                    .setMessage("Android requires your approval before this app can hand a downloaded APK to the system installer.")
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Open settings", (d, w) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    }).show();
            return;
        }
        ProgressBar bar = new ProgressBar(this);
        LinearLayout box = new LinearLayout(this);
        box.setPadding(dp(20), dp(16), dp(20), dp(16));
        box.setOrientation(LinearLayout.VERTICAL);
        box.addView(bar);
        TextView label = new TextView(this);
        label.setText("Downloading securely…");
        label.setPadding(0, dp(12), 0, 0);
        box.addView(label);
        progressDialog = new AlertDialog.Builder(this).setTitle(item.name).setView(box)
                .setNegativeButton("Cancel", (d, w) -> { if (downloadTask != null) downloadTask.cancel(true); }).create();
        progressDialog.setCancelable(false);
        progressDialog.show();
        downloadTask = io.submit(() -> {
            File apk = null;
            try {
                HttpsURLConnection connection = openDownloadConnection(item.downloadUrl);
                int code = connection.getResponseCode();
                long length = connection.getContentLengthLong();
                if (length > 500L * 1024L * 1024L) throw new java.io.IOException("APK exceeds the 500 MB safety limit");
                apk = new File(getCacheDir(), item.id + ".apk.part");
                MessageDigest digest = MessageDigest.getInstance("SHA-256");
                long total = 0;
                try (InputStream in = connection.getInputStream(); FileOutputStream out = new FileOutputStream(apk)) {
                    byte[] buffer = new byte[16384];
                    int count;
                    while ((count = in.read(buffer)) != -1) {
                        if (Thread.currentThread().isInterrupted()) throw new InterruptedException("Download cancelled");
                        total += count;
                        if (total > 500L * 1024L * 1024L) throw new java.io.IOException("APK exceeds the 500 MB safety limit");
                        digest.update(buffer, 0, count);
                        out.write(buffer, 0, count);
                    }
                    out.getFD().sync();
                } finally {
                    connection.disconnect();
                }
                String actual = toHex(digest.digest());
                if (!actual.equalsIgnoreCase(item.sha256)) throw new java.io.IOException("SHA-256 mismatch; file rejected");
                verifySigningCertificate(apk, item.signingCertSha256);
                File verified = new File(getCacheDir(), item.id + ".apk");
                if (verified.exists() && !verified.delete()) throw new java.io.IOException("Could not replace cached APK");
                if (!apk.renameTo(verified)) throw new java.io.IOException("Could not finalize verified APK");
                File finalApk = verified;
                main.post(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    try {
                        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".files", finalApk);
                        Intent install = new Intent(Intent.ACTION_VIEW);
                        install.setDataAndType(uri, "application/vnd.android.package-archive");
                        install.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(install);
                    } catch (Exception e) {
                        Toast.makeText(this, "APK verified, but Android could not open the installer.", Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                if (apk != null && apk.exists()) apk.delete();
                final String message = e instanceof InterruptedException ? "Download cancelled" :
                        (e.getMessage() == null ? "Download failed" : e.getMessage());
                main.post(() -> {
                    if (progressDialog != null && progressDialog.isShowing()) progressDialog.dismiss();
                    new AlertDialog.Builder(this).setTitle("Download not completed")
                            .setMessage(message).setPositiveButton("OK", null).show();
                });
            }
        });
    }

    private HttpsURLConnection openDownloadConnection(String address) throws Exception {
        URL current = new URL(address);
        for (int redirects = 0; redirects <= 5; redirects++) {
            if (!"https".equalsIgnoreCase(current.getProtocol()) || current.getUserInfo() != null) {
                throw new java.io.IOException("Only trusted HTTPS download URLs are allowed");
            }
            HttpsURLConnection connection = (HttpsURLConnection) current.openConnection();
            connection.setConnectTimeout(15000);
            connection.setReadTimeout(20000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("User-Agent", "SlideME-Reborn/0.2");
            int code = connection.getResponseCode();
            if (code >= 200 && code < 300) return connection;
            if (code == 301 || code == 302 || code == 303 || code == 307 || code == 308) {
                String location = connection.getHeaderField("Location");
                connection.disconnect();
                if (location == null || location.trim().isEmpty() || redirects == 5) {
                    throw new java.io.IOException("Download redirect could not be followed");
                }
                current = new URL(current, location);
                continue;
            }
            connection.disconnect();
            throw new java.io.IOException("Download server returned HTTP " + code);
        }
        throw new java.io.IOException("Too many download redirects");
    }

    private void openHttps(String address) {
        try {
            if (!address.startsWith("https://")) throw new IllegalArgumentException("Only HTTPS links are allowed");
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(address)));
        } catch (Exception e) {
            Toast.makeText(this, "No app can open this link.", Toast.LENGTH_SHORT).show();
        }
    }

    private void verifySigningCertificate(File apk, String expectedFingerprint) throws Exception {
        if (expectedFingerprint == null || !expectedFingerprint.matches("(?i)[0-9a-f]{64}")) {
            throw new java.io.IOException("Catalog has no valid signing-certificate fingerprint");
        }
        PackageInfo info = getPackageManager().getPackageArchiveInfo(
                apk.getAbsolutePath(), PackageManager.GET_SIGNING_CERTIFICATES);
        if (info == null || info.signingInfo == null) {
            throw new java.io.IOException("Downloaded file is not a valid signed Android package");
        }
        android.content.pm.Signature[] signers = info.signingInfo.getApkContentsSigners();
        if (signers == null || signers.length != 1) {
            throw new java.io.IOException("APK has an unsupported number of signing certificates");
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        String actualFingerprint = toHex(digest.digest(signers[0].toByteArray()));
        if (!actualFingerprint.equalsIgnoreCase(expectedFingerprint)) {
            throw new java.io.IOException("APK signing certificate mismatch; file rejected");
        }
    }

    private String toHex(byte[] bytes) {
        StringBuilder out = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) out.append(String.format(Locale.ROOT, "%02x", b & 0xff));
        return out.toString();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
