package org.fan.slidemeclassic;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity {
    private final List<CatalogItem> allItems = new ArrayList<CatalogItem>();
    private final List<CatalogItem> visibleItems = new ArrayList<CatalogItem>();
    private ArrayAdapter<String> adapter;
    private EditText searchBox;
    private String selectedCategory = "All";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildLayout();
        loadCatalog();
    }

    private void buildLayout() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.rgb(242, 242, 242));
        int pad = dp(10);
        root.setPadding(pad, pad, pad, pad);

        TextView title = new TextView(this);
        title.setText("SlideME Classic");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(dp(12), 0, dp(12), 0);
        title.setBackgroundColor(Color.rgb(67, 96, 48));
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(52)));

        TextView subtitle = new TextView(this);
        subtitle.setText("FAN-MADE LEGACY APP CATALOG");
        subtitle.setTextColor(Color.rgb(76, 76, 76));
        subtitle.setTextSize(11);
        subtitle.setPadding(0, dp(10), 0, dp(4));
        root.addView(subtitle);

        searchBox = new EditText(this);
        searchBox.setSingleLine(true);
        searchBox.setTextSize(16);
        searchBox.setHint("Search apps and games");
        root.addView(searchBox, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(46)));
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
        root.addView(categories);

        ListView listView = new ListView(this);
        listView.setDividerHeight(dp(1));
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_2,
                android.R.id.text1, new ArrayList<String>()) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row = super.getView(position, convertView, parent);
                TextView primary = (TextView) row.findViewById(android.R.id.text1);
                TextView secondary = (TextView) row.findViewById(android.R.id.text2);
                CatalogItem item = visibleItems.get(position);
                primary.setText(item.name);
                primary.setTextColor(Color.rgb(38, 38, 38));
                primary.setTextSize(17);
                secondary.setText(item.category + "  •  v" + item.version
                        + "  •  Android API " + item.minAndroidApi + "+");
                secondary.setTextColor(Color.rgb(100, 100, 100));
                return row;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                showDetails(visibleItems.get(position));
            }
        });
        root.addView(listView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1));

        TextView footer = new TextView(this);
        footer.setText("Offline catalog • Downloads require internet");
        footer.setTextColor(Color.rgb(100, 100, 100));
        footer.setTextSize(11);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(8), 0, dp(2));
        root.addView(footer);
        setContentView(root);
    }

    private void addCategoryButton(LinearLayout row, final String category) {
        Button button = new Button(this);
        button.setText(category);
        button.setTextSize(12);
        button.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                selectedCategory = category;
                filterItems();
            }
        });
        row.addView(button, new LinearLayout.LayoutParams(0, dp(42), 1));
    }

    private void loadCatalog() {
        try {
            allItems.clear();
            allItems.addAll(CatalogRepository.loadBundled(this));
            filterItems();
        } catch (Exception e) {
            Toast.makeText(this, "Could not load the offline catalog.", Toast.LENGTH_LONG).show();
        }
    }

    private void filterItems() {
        if (adapter == null) return;
        String query = searchBox == null ? "" : searchBox.getText().toString().trim().toLowerCase(Locale.US);
        visibleItems.clear();
        for (CatalogItem item : allItems) {
            boolean categoryMatch = "All".equals(selectedCategory) || selectedCategory.equals(item.category);
            boolean textMatch = item.name.toLowerCase(Locale.US).contains(query)
                    || item.description.toLowerCase(Locale.US).contains(query);
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
                + "\nArchive status: " + item.status;
        builder.setMessage(details);
        if (item.hasDownload()) {
            builder.setPositiveButton("Open download", (dialog, which) -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(item.downloadUrl));
                startActivity(intent);
            });
        } else if (item.sourceUrl != null && item.sourceUrl.startsWith("https://")) {
            builder.setPositiveButton("Open source page", (dialog, which) -> {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.sourceUrl)));
            });
        } else {
            builder.setPositiveButton("No download yet", null);
        }
        builder.setNegativeButton("Close", null);
        builder.show();
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
