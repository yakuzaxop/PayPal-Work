package com.tushar.paypalwork;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.CookieManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final String DEFAULT_URL =
            "https://www.paypal.com/ng/welcome/signup/#/login_info";

    private static final int BLUE = Color.rgb(0, 48, 135);
    private static final int LIGHT_BLUE = Color.rgb(0, 112, 224);
    private static final int WHITE = Color.WHITE;

    private WebView webView;
    private EditText urlBar;

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable rounded(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout.LayoutParams params(
            int width, int height) {
        return new LinearLayout.LayoutParams(
                width == -1 ? -1 : width == 0 ? 0 : dp(width),
                height == -1 ? -1 : height == 0 ? 0 : dp(height)
        );
    }

    private TextView button(String text, int width) {
        TextView view = new TextView(this);
        view.setText(text);
        view.setTextColor(WHITE);
        view.setTextSize(19);
        view.setTypeface(Typeface.DEFAULT_BOLD);
        view.setGravity(Gravity.CENTER);
        view.setBackground(rounded(BLUE, 10));

        LinearLayout.LayoutParams lp = params(width, 42);
        lp.setMargins(dp(3), 0, dp(3), 0);
        view.setLayoutParams(lp);

        return view;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BLUE);
        getWindow().setNavigationBarColor(Color.BLACK);

        // MAIN LAYOUT

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(WHITE);

        // =================================
        // FIRST ROW: APP NAME + MENU
        // =================================

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(14), dp(5), dp(10), dp(5));
        header.setBackgroundColor(BLUE);

        TextView brand = new TextView(this);
        brand.setText("◉ PayPal Work");
        brand.setTextColor(WHITE);
        brand.setTextSize(20);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setGravity(Gravity.CENTER_VERTICAL);

        header.addView(
                brand,
                new LinearLayout.LayoutParams(0, dp(46), 1f)
        );

        TextView menu = button("⋮", 42);
        menu.setContentDescription("Menu");
        menu.setOnClickListener(v -> showMenu());

        header.addView(menu);
        root.addView(header);

        // =================================
        // SECOND ROW: URL + REFRESH
        // =================================

        LinearLayout addressRow = new LinearLayout(this);
        addressRow.setOrientation(LinearLayout.HORIZONTAL);
        addressRow.setGravity(Gravity.CENTER_VERTICAL);
        addressRow.setPadding(dp(9), dp(7), dp(9), dp(7));
        addressRow.setBackgroundColor(BLUE);

        urlBar = new EditText(this);
        urlBar.setSingleLine(true);
        urlBar.setTextSize(14);
        urlBar.setTextColor(Color.rgb(25, 35, 55));
        urlBar.setHintTextColor(Color.GRAY);
        urlBar.setHint("Enter website address");
        urlBar.setPadding(dp(12), 0, dp(8), 0);
        urlBar.setSelectAllOnFocus(true);

        urlBar.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT
                        | android.text.InputType.TYPE_TEXT_VARIATION_URI
        );

        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBar.setBackground(rounded(WHITE, 12));

        LinearLayout.LayoutParams addressParams =
                new LinearLayout.LayoutParams(0, dp(44), 1f);

        addressParams.setMargins(0, 0, dp(6), 0);
        addressRow.addView(urlBar, addressParams);

        TextView refresh = button("↻", 44);
        refresh.setContentDescription("Refresh page");
        refresh.setOnClickListener(v -> webView.reload());

        addressRow.addView(refresh);
        root.addView(addressRow);

        // =================================
        // WEBSITE VIEW
        // =================================

        webView = new WebView(this);
        webView.setBackgroundColor(WHITE);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setDisplayZoomControls(false);

        webView.setWebChromeClient(new WebChromeClient());

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request) {

                String scheme = request.getUrl().getScheme();

                return !("https".equalsIgnoreCase(scheme)
                        || "http".equalsIgnoreCase(scheme));
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                if (url != null) {
                    urlBar.setText(url);
                }
            }
        });

        root.addView(
                webView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        // =================================
        // BOTTOM CLEAR COOKIES BUTTON
        // =================================

        LinearLayout bottom = new LinearLayout(this);
        bottom.setOrientation(LinearLayout.VERTICAL);
        bottom.setPadding(dp(12), dp(7), dp(12), dp(9));
        bottom.setBackgroundColor(WHITE);

        Button clearCookies = new Button(this);
        clearCookies.setText("🧹  Clear Cookies");
        clearCookies.setAllCaps(false);
        clearCookies.setTextColor(WHITE);
        clearCookies.setTextSize(16);
        clearCookies.setTypeface(Typeface.DEFAULT_BOLD);
        clearCookies.setBackground(rounded(LIGHT_BLUE, 15));
        clearCookies.setElevation(dp(3));

        bottom.addView(
                clearCookies,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                )
        );

        clearCookies.setOnClickListener(
                v -> clearCookiesAndReload()
        );

        root.addView(bottom);

        setContentView(root);

        // LOAD URL FROM KEYBOARD

        urlBar.setOnEditorActionListener(
                (view, actionId, event) -> {

                    boolean enter = event != null
                            && event.getKeyCode()
                            == KeyEvent.KEYCODE_ENTER
                            && event.getAction()
                            == KeyEvent.ACTION_DOWN;

                    if (actionId == EditorInfo.IME_ACTION_GO
                            || actionId == EditorInfo.IME_ACTION_DONE
                            || enter) {

                        loadAddress();
                        return true;
                    }

                    return false;
                }
        );

        // INITIAL PAGE

        if (savedInstanceState != null
                && webView.restoreState(savedInstanceState) != null) {

            // Restore the previous WebView state.

        } else {
            webView.loadUrl(DEFAULT_URL);
        }
    }

    // =================================
    // LOAD WEBSITE
    // =================================

    private void loadAddress() {

        String address = urlBar.getText().toString().trim();

        if (address.isEmpty()) {
            return;
        }

        if (!address.matches("(?i)^https?://.*")) {
            address = "https://" + address;
        }

        Uri uri = Uri.parse(address);

        if (uri.getHost() == null) {
            Toast.makeText(
                    this,
                    "Enter a valid website address",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        InputMethodManager keyboard =
                (InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE
                );

        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(
                    urlBar.getWindowToken(), 0
            );
        }

        webView.loadUrl(uri.toString());
    }

    // =================================
    // CLEAR COOKIES AND CACHE
    // =================================

    private void clearCookiesAndReload() {

        CookieManager cookies = CookieManager.getInstance();

        cookies.removeAllCookies(success -> {

            cookies.flush();

            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();

            urlBar.setText(DEFAULT_URL);
            webView.loadUrl(DEFAULT_URL);

            Toast.makeText(
                    MainActivity.this,
                    "Cookies and cache cleared",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    // =================================
    // MENU
    // =================================

    private void showMenu() {

        String[] options = {
                "Reload current page",
                "Open default PayPal page",
                "Open in external browser",
                "About PayPal Work"
        };

        new AlertDialog.Builder(this)
                .setTitle("PayPal Work")
                .setItems(options, (dialog, which) -> {

                    if (which == 0) {

                        webView.reload();

                    } else if (which == 1) {

                        urlBar.setText(DEFAULT_URL);
                        webView.loadUrl(DEFAULT_URL);

                    } else if (which == 2) {

                        String current = webView.getUrl();

                        if (current != null) {
                            try {
                                startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(current)
                                ));
                            } catch (Exception e) {
                                Toast.makeText(
                                        this,
                                        "Browser unavailable",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                    } else {

                        new AlertDialog.Builder(this)
                                .setTitle("About PayPal Work")
                                .setMessage(
                                        "PayPal Work is an independent " +
                                        "browser wrapper and is not an " +
                                        "official PayPal application. " +
                                        "Always verify the website address " +
                                        "before entering account details."
                                )
                                .setPositiveButton("OK", null)
                                .show();
                    }
                })
                .show();
    }

    @Override
    public void onBackPressed() {

        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {

        super.onSaveInstanceState(state);

        if (webView != null) {
            webView.saveState(state);
        }
    }

    @Override
    protected void onDestroy() {

        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
        }

        super.onDestroy();
    }
}
