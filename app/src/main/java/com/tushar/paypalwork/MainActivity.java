package com.tushar.paypalwork;

import android.app.Activity;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.view.inputmethod.EditorInfo;
import android.view.KeyEvent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.CookieManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

    private LinearLayout root;
    private EditText urlInput;
    private WebView webView;

    private final String HOME_URL =
            "https://www.paypal.com/ng/welcome/signup/#/login_info";

    private final int BLUE = Color.rgb(13, 57, 145);
    private final int WHITE = Color.WHITE;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setupWindow();
            createLayout();
            setupWebView();

            if (savedInstanceState != null) {
                webView.restoreState(savedInstanceState);
            }

            if (webView.getUrl() == null) {
                webView.loadUrl(HOME_URL);
            }

        } catch (Exception e) {
            Toast.makeText(
                    this,
                    "App error: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    private void setupWindow() {
        Window window = getWindow();

        window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        window.setStatusBarColor(BLUE);
        window.setNavigationBarColor(Color.BLACK);

        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
        }

        if (android.os.Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(true);
        }
    }

    private void createLayout() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(WHITE);

        setContentView(root);

        // Single row: App name + URL + Refresh
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(8), dp(4), dp(6), dp(4));
        topBar.setBackgroundColor(BLUE);

        root.addView(
                topBar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(54)
                )
        );

        TextView title = new TextView(this);
        title.setText("PayPal Work");
        title.setTextColor(WHITE);
        title.setTextSize(15);
        title.setTypeface(null, Typeface.BOLD);
        title.setSingleLine(true);
        title.setGravity(Gravity.CENTER_VERTICAL);

        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        dp(105),
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );

        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setTextSize(11);
        urlInput.setPadding(dp(7), 0, dp(7), 0);
        urlInput.setHint("Enter URL");
        urlInput.setTextColor(Color.BLACK);
        urlInput.setHintTextColor(Color.GRAY);
        urlInput.setImeOptions(EditorInfo.IME_ACTION_GO);

        GradientDrawable urlBg = new GradientDrawable();
        urlBg.setColor(WHITE);
        urlBg.setCornerRadius(dp(8));
        urlBg.setStroke(dp(1), Color.LTGRAY);
        urlInput.setBackground(urlBg);

        LinearLayout.LayoutParams urlParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(40),
                        1f
                );

        urlParams.setMargins(dp(3), 0, dp(3), 0);
        topBar.addView(urlInput, urlParams);

        TextView refresh = new TextView(this);
        refresh.setText("↻");
        refresh.setTextSize(25);
        refresh.setTextColor(WHITE);
        refresh.setGravity(Gravity.CENTER);

        topBar.addView(
                refresh,
                new LinearLayout.LayoutParams(
                        dp(34),
                        dp(44)
                )
        );

        refresh.setOnClickListener(v -> {
            if (webView != null) {
                webView.reload();
            }
        });

        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean go =
                    actionId == EditorInfo.IME_ACTION_GO
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);

            if (go) {
                loadUrlFromInput();

                InputMethodManager imm =
                        (InputMethodManager) getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

                if (imm != null) {
                    imm.hideSoftInputFromWindow(
                            urlInput.getWindowToken(), 0
                    );
                }

                urlInput.clearFocus();
                return true;
            }

            return false;
        });

        // WebView
        webView = new WebView(this);

        root.addView(
                webView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        // Bottom clear cookies button
        Button clearButton = new Button(this);
        clearButton.setText("🧹 Clear Cookies");
        clearButton.setTextColor(WHITE);
        clearButton.setTextSize(15);
        clearButton.setAllCaps(false);
        clearButton.setTypeface(null, Typeface.BOLD);

        GradientDrawable buttonBg = new GradientDrawable();
        buttonBg.setColor(Color.rgb(0, 112, 224));
        buttonBg.setCornerRadius(dp(14));
        clearButton.setBackground(buttonBg);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(52)
                );

        buttonParams.setMargins(
                dp(12), dp(4), dp(12), dp(4)
        );

        root.addView(clearButton, buttonParams);

        clearButton.setOnClickListener(v -> {
            CookieManager cookieManager =
                    CookieManager.getInstance();

            cookieManager.removeAllCookies(success -> {
                cookieManager.flush();

                if (webView != null) {
                    webView.clearCache(true);
                    webView.clearHistory();
                    webView.clearFormData();
                    webView.reload();
                }

                Toast.makeText(
                        MainActivity.this,
                        "Cookies cleared",
                        Toast.LENGTH_SHORT
                ).show();
            });
        });
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setSupportZoom(true);
        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true);
        settings.setLoadsImagesAutomatically(true);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                if (urlInput != null && url != null) {
                    urlInput.setText(url);
                }
            }

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    String url
            ) {
                return false;
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        CookieManager.getInstance().setAcceptCookie(true);
    }

    private void loadUrlFromInput() {
        String address = urlInput.getText().toString().trim();

        if (address.isEmpty()) {
            return;
        }

        if (!address.startsWith("https://")
                && !address.startsWith("http://")) {
            address = "https://" + address;
        }

        Uri uri = Uri.parse(address);

        if (uri.getHost() == null) {
            Toast.makeText(
                    this,
                    "Invalid URL",
                    Toast.LENGTH_SHORT
            ).show();
            return;
        }

        webView.loadUrl(address);
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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (webView != null) {
            webView.saveState(outState);
        }
    }

    @Override
    protected void onDestroy() {
        if (webView != null) {
            webView.stopLoading();
            webView.destroy();
            webView = null;
        }

        super.onDestroy();
    }
}
