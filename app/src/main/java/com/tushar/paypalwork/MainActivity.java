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
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
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

    private WebView webView;
    private EditText urlBar;

    private int dp(float value) {
        return (int) (value *
                getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable makeBackground(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private TextView makeToolButton(String text, int width) {
        TextView button = new TextView(this);
        button.setText(text);
        button.setTextColor(Color.WHITE);
        button.setTextSize(19);
        button.setGravity(Gravity.CENTER);
        button.setTypeface(Typeface.DEFAULT_BOLD);
        button.setBackground(
                makeBackground(Color.rgb(0, 48, 135), 9));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(dp(width), dp(42));

        params.setMargins(dp(2), 0, dp(2), 0);
        button.setLayoutParams(params);

        return button;
    }

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);

        getWindow().setStatusBarColor(Color.rgb(0, 48, 135));
        getWindow().setNavigationBarColor(Color.BLACK);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.WHITE);

        // TOP TOOLBAR

        LinearLayout toolbar = new LinearLayout(this);
        toolbar.setOrientation(LinearLayout.HORIZONTAL);
        toolbar.setGravity(Gravity.CENTER_VERTICAL);
        toolbar.setPadding(dp(5), dp(7), dp(5), dp(7));
        toolbar.setBackgroundColor(Color.rgb(0, 48, 135));

        // APP NAME

        TextView brand = new TextView(this);
        brand.setText("◉ PayPal Work");
        brand.setTextColor(Color.WHITE);
        brand.setTextSize(14);
        brand.setTypeface(Typeface.DEFAULT_BOLD);
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setSingleLine(true);

        brand.setLayoutParams(
                new LinearLayout.LayoutParams(dp(108), dp(42)));

        toolbar.addView(brand);

        // URL BAR

        urlBar = new EditText(this);
        urlBar.setSingleLine(true);
        urlBar.setTextSize(12);
        urlBar.setTextColor(Color.rgb(20, 36, 65));
        urlBar.setHintTextColor(Color.GRAY);
        urlBar.setHint("Enter URL");
        urlBar.setPadding(dp(8), 0, dp(5), 0);
        urlBar.setSelectAllOnFocus(true);
        urlBar.setInputType(
                android.text.InputType.TYPE_CLASS_TEXT |
                android.text.InputType.TYPE_TEXT_VARIATION_URI);
        urlBar.setImeOptions(EditorInfo.IME_ACTION_GO);
        urlBar.setBackground(makeBackground(Color.WHITE, 9));

        LinearLayout.LayoutParams urlParams =
                new LinearLayout.LayoutParams(0, dp(42), 1f);

        urlParams.setMargins(dp(2), 0, dp(2), 0);
        urlBar.setLayoutParams(urlParams);

        toolbar.addView(urlBar);

        // REFRESH BUTTON

        TextView refresh = makeToolButton("↻", 36);
        refresh.setContentDescription("Refresh page");
        refresh.setOnClickListener(v -> webView.reload());
        toolbar.addView(refresh);

        // MENU BUTTON

        TextView menu = makeToolButton("⋮", 30);
        menu.setContentDescription("Menu");
        menu.setOnClickListener(v -> showMenu());
        toolbar.addView(menu);

        root.addView(toolbar);

        // WEBVIEW

        webView = new WebView(this);
        webView.setBackgroundColor(Color.WHITE);

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
                urlBar.setText(url);
            }
        });

        LinearLayout.LayoutParams webParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0, 1f);

        root.addView(webView, webParams);

        // BOTTOM CLEAR COOKIES BUTTON

        Button clearCookies = new Button(this);
        clearCookies.setText("🧹  Clear Cookies");
        clearCookies.setAllCaps(false);
        clearCookies.setTextColor(Color.WHITE);
        clearCookies.setTextSize(15);
        clearCookies.setTypeface(Typeface.DEFAULT_BOLD);
        clearCookies.setBackground(
                makeBackground(Color.rgb(0, 112, 224), 12));

        LinearLayout.LayoutParams clearParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(48));

        clearParams.setMargins(dp(10), dp(5), dp(10), dp(8));
        root.addView(clearCookies, clearParams);

        clearCookies.setOnClickListener(
                v -> clearCookiesAndReload());

        setContentView(root);

        // LOAD URL FROM KEYBOARD

        urlBar.setOnEditorActionListener((view, actionId, event) -> {

            boolean enterPressed = event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN;

            if (actionId == EditorInfo.IME_ACTION_GO
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || enterPressed) {

                loadAddress();
                return true;
            }

            return false;
        });

        // OPEN DEFAULT PAGE

        if (state != null && webView.restoreState(state) != null) {
            // Restore the previous WebView page.
        } else {
            webView.loadUrl(DEFAULT_URL);
        }
    }

    // OPEN URL

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
            Toast.makeText(this,
                    "Enter a valid website address",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        InputMethodManager keyboard =
                (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);

        if (keyboard != null) {
            keyboard.hideSoftInputFromWindow(
                    urlBar.getWindowToken(), 0);
        }

        webView.loadUrl(uri.toString());
    }

    // CLEAR COOKIES AND CACHE

    private void clearCookiesAndReload() {
        CookieManager cookies = CookieManager.getInstance();

        cookies.removeAllCookies(success -> {
            cookies.flush();

            webView.clearCache(true);
            webView.clearHistory();
            webView.clearFormData();

            urlBar.setText(DEFAULT_URL);
            webView.loadUrl(DEFAULT_URL);

            Toast.makeText(this,
                    "Cookies and cache cleared",
                    Toast.LENGTH_SHORT).show();
        });
    }

    // MENU OPTIONS

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
                        String url = webView.getUrl();

                        if (url != null) {
                            try {
                                startActivity(new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse(url)));

                            } catch (Exception e) {
                                Toast.makeText(this,
                                        "No browser available",
                                        Toast.LENGTH_SHORT).show();
                            }
                        }

                    } else {
                        new AlertDialog.Builder(this)
                                .setTitle("About PayPal Work")
                                .setMessage(
                                        "PayPal Work is an independent " +
                                        "WebView app and is not an official " +
                                        "PayPal product. Verify the website " +
                                        "address before entering account details.")
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
