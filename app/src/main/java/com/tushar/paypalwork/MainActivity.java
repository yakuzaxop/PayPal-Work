package com.tushar.paypalwork;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebResourceRequest;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;
import android.view.KeyEvent;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.view.inputmethod.EditorInfo;

public class MainActivity extends Activity {

    private LinearLayout root;
    private LinearLayout topBar;
    private EditText urlInput;
    private WebView webView;
    private TextView refreshButton;

    private final String HOME_URL =
            "https://www.paypal.com/ng/welcome/signup/#/login_info";

    private int blue = Color.rgb(13, 57, 145);
    private int brightBlue = Color.rgb(0, 112, 224);
    private int white = Color.WHITE;

    private int dp(float value) {
        return (int) (value * getResources()
                .getDisplayMetrics().density + 0.5f);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        configureSystemBars();
        buildLayout();
        setupWebView();

        if (savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }

        if (webView.getUrl() == null) {
            webView.loadUrl(HOME_URL);
        }
    }

    private void configureSystemBars() {
        Window window = getWindow();

        window.clearFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        window.setStatusBarColor(blue);
        window.setNavigationBarColor(Color.BLACK);

        if (Build.VERSION.SDK_INT >= 29) {
            window.setNavigationBarContrastEnforced(false);
            window.setStatusBarContrastEnforced(false);
        }

        if (Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false);

            WindowInsetsController controller =
                    window.getInsetsController();

            if (controller != null) {
                controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                );
            }
        } else {
            window.getDecorView().setSystemUiVisibility(0);
        }
    }

    private void buildLayout() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(white);

        // Handle status bar and navigation bar insets.
        if (Build.VERSION.SDK_INT >= 30) {
            root.setOnApplyWindowInsetsListener((view, insets) -> {
                android.graphics.Insets bars =
                        insets.getInsets(
                                WindowInsets.Type.statusBars()
                                | WindowInsets.Type.navigationBars()
                        );

                view.setPadding(
                        0,
                        bars.top,
                        0,
                        bars.bottom
                );

                return insets;
            });
        } else {
            root.setFitsSystemWindows(true);
        }

        setContentView(root);

        // TOP BAR: App name + URL + Refresh in one row.
        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setPadding(dp(10), dp(5), dp(8), dp(5));
        topBar.setBackgroundColor(blue);

        root.addView(
                topBar,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(60)
                )
        );

        // App title
        TextView title = new TextView(this);
        title.setText("PayPal Work");
        title.setTextColor(white);
        title.setTextSize(16);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setSingleLine(true);

        topBar.addView(
                title,
                new LinearLayout.LayoutParams(
                        dp(112),
                        LinearLayout.LayoutParams.MATCH_PARENT
                )
        );

        // URL field
        urlInput = new EditText(this);
        urlInput.setSingleLine(true);
        urlInput.setTextSize(12);
        urlInput.setTextColor(Color.rgb(35, 35, 35));
        urlInput.setHintTextColor(Color.GRAY);
        urlInput.setHint("Enter URL");
        urlInput.setPadding(dp(8), 0, dp(8), 0);
        urlInput.setSelectAllOnFocus(true);
        urlInput.setImeOptions(EditorInfo.IME_ACTION_GO);

        GradientDrawable urlBackground = new GradientDrawable();
        urlBackground.setColor(white);
        urlBackground.setCornerRadius(dp(10));
        urlBackground.setStroke(dp(1), Color.LTGRAY);
        urlInput.setBackground(urlBackground);

        LinearLayout.LayoutParams urlParams =
                new LinearLayout.LayoutParams(
                        0,
                        dp(42),
                        1f
                );

        urlParams.setMargins(dp(3), 0, dp(3), 0);
        topBar.addView(urlInput, urlParams);

        // Refresh button
        refreshButton = new TextView(this);
        refreshButton.setText("↻");
        refreshButton.setTextSize(26);
        refreshButton.setTextColor(white);
        refreshButton.setGravity(Gravity.CENTER);
        refreshButton.setClickable(true);

        topBar.addView(
                refreshButton,
                new LinearLayout.LayoutParams(
                        dp(38),
                        dp(44)
                )
        );

        // WebView fills all remaining space.
        webView = new WebView(this);

        root.addView(
                webView,
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                )
        );

        // Fixed bottom button.
        Button clearCookies = new Button(this);
        clearCookies.setText("🧹 Clear Cookies");
        clearCookies.setTextColor(white);
        clearCookies.setTextSize(16);
        clearCookies.setAllCaps(false);
        clearCookies.setTypeface(
                Typeface.DEFAULT,
                Typeface.BOLD
        );

        GradientDrawable buttonBackground =
                new GradientDrawable();

        buttonBackground.setColor(brightBlue);
        buttonBackground.setCornerRadius(dp(18));
        clearCookies.setBackground(buttonBackground);

        LinearLayout.LayoutParams buttonParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        dp(58)
                );

        buttonParams.setMargins(
                dp(14),
                dp(5),
                dp(14),
                dp(5)
        );

        root.addView(clearCookies, buttonParams);

        // Load typed URL when Go is pressed.
        urlInput.setOnEditorActionListener((v, actionId, event) -> {
            boolean goPressed =
                    actionId == EditorInfo.IME_ACTION_GO
                    || (event != null
                    && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                    && event.getAction() == KeyEvent.ACTION_DOWN);

            if (goPressed) {
                loadEnteredUrl();

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

        refreshButton.setOnClickListener(v -> {
            if (webView != null) {
                webView.reload();
            }
        });

        clearCookies.setOnClickListener(v -> {
            CookieManager cookies = CookieManager.getInstance();
            cookies.removeAllCookies(success -> {
                cookies.flush();

                webView.clearCache(true);
                webView.clearHistory();
                webView.clearFormData();

                Toast.makeText(
                        MainActivity.this,
                        "Cookies cleared",
                        Toast.LENGTH_SHORT
                ).show();

                webView.reload();
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
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setDefaultTextEncodingName("UTF-8");

        webView.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(
                    WebView view,
                    WebResourceRequest request
            ) {
                Uri uri = request.getUrl();
                String scheme = uri.getScheme();

                if ("http".equalsIgnoreCase(scheme)
                        || "https".equalsIgnoreCase(scheme)) {
                    return false;
                }

                return true;
            }

            @Override
            public void onPageFinished(
                    WebView view,
                    String url
            ) {
                super.onPageFinished(view, url);

                if (url != null && urlInput != null) {
                    urlInput.setText(url);
                }
            }
        });

        webView.setWebChromeClient(new WebChromeClient());

        CookieManager.getInstance()
                .setAcceptCookie(true);

        CookieManager.getInstance()
                .setAcceptThirdPartyCookies(webView, true);
    }

    private void loadEnteredUrl() {
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
                    "Please enter a valid URL",
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