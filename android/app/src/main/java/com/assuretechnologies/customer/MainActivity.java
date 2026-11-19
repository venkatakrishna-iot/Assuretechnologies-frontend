package com.assuretechnologies.customer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebChromeClient;
import com.getcapacitor.BridgeWebViewClient;

public class MainActivity extends BridgeActivity {

    private static final String TAG = "MainActivityPayment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Window window = getWindow();

        // Ensure edge-to-edge does NOT overlap the web content (dedicated status bar space)
        WindowCompat.setDecorFitsSystemWindows(window, true);

        // Solid system status bar
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(Color.BLACK);

        // Force white icons on black status bar (clock, battery, Wi-Fi)
        WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(window, window.getDecorView());
        if (controller != null) {
            controller.setAppearanceLightStatusBars(false);
        }

        // Configure WebView for payment gateways, wallets, and UPI
        setupPaymentWebView();
    }

    private void setupPaymentWebView() {
        if (getBridge() == null || getBridge().getWebView() == null) {
            return;
        }

        WebView webView = getBridge().getWebView();
        WebSettings settings = webView.getSettings();

        // 1. Sanitize User-Agent: remove "; wv" and "Version/4.0" so Razorpay recognizes mobile Chrome and enables UPI
        String defaultUserAgent = settings.getUserAgentString();
        if (defaultUserAgent != null) {
            String chromeUserAgent = defaultUserAgent
                .replace("; wv", "")
                .replaceAll("Version\\/\\d+\\.\\d+\\s*", "");
            settings.setUserAgentString(chromeUserAgent);
        }

        // 2. Multi-window & automatic popups for 3DS / bank / OTP dialogs
        settings.setSupportMultipleWindows(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        // 3. Storage & DOM settings for payment flows
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);

        // 4. Enable cookies including third-party cookies (critical for PhonePe wallet & Razorpay iframes)
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.setAcceptCookie(true);
        cookieManager.setAcceptThirdPartyCookies(webView, true);

        // 5. Custom BridgeWebViewClient to intercept intent://, upi://, phonepe://, etc.
        getBridge().setWebViewClient(new BridgeWebViewClient(getBridge()) {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri uri = request.getUrl();
                if (handleExternalOrCustomUri(view, uri)) {
                    return true;
                }

                // If it is a payment gateway or banking domain, keep it inside the WebView
                if (isPaymentOrInternalUrl(uri)) {
                    return false;
                }

                return super.shouldOverrideUrlLoading(view, request);
            }
        });

        // 5. Custom BridgeWebChromeClient for handling popups/windows (e.g., bank 3DS OTP)
        webView.setWebChromeClient(new BridgeWebChromeClient(getBridge()) {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
                WebView tempWebView = new WebView(MainActivity.this);
                WebSettings tempSettings = tempWebView.getSettings();
                tempSettings.setJavaScriptEnabled(true);
                tempSettings.setDomStorageEnabled(true);
                tempSettings.setSupportMultipleWindows(true);
                tempSettings.setJavaScriptCanOpenWindowsAutomatically(true);
                CookieManager.getInstance().setAcceptThirdPartyCookies(tempWebView, true);

                tempWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView subView, WebResourceRequest subRequest) {
                        Uri subUri = subRequest.getUrl();
                        if (handleExternalOrCustomUri(view, subUri)) {
                            return true;
                        }
                        // For web URLs opened in a popup window (e.g. 3DS bank OTP page),
                        // load directly in the main WebView
                        view.loadUrl(subUri.toString());
                        return true;
                    }
                });

                WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                transport.setWebView(tempWebView);
                resultMsg.sendToTarget();
                return true;
            }
        });
    }

    private boolean handleExternalOrCustomUri(WebView view, Uri uri) {
        if (uri == null) return false;
        String url = uri.toString();

        // 1. Handle Android Intent scheme (intent://...) used by Razorpay, PhonePe, UPI apps, and banks
        if (url.startsWith("intent://")) {
            try {
                Intent intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent != null) {
                    PackageManager pm = getPackageManager();
                    if (intent.resolveActivity(pm) != null) {
                        startActivity(intent);
                        return true;
                    }
                    // Fallback URL if specified in intent (e.g., play store or web fallback)
                    String fallbackUrl = intent.getStringExtra("browser_fallback_url");
                    if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
                        view.loadUrl(fallbackUrl);
                        return true;
                    }
                    // Try intent data URI directly
                    Uri dataUri = intent.getData();
                    if (dataUri != null) {
                        Intent fallbackIntent = new Intent(Intent.ACTION_VIEW, dataUri);
                        if (fallbackIntent.resolveActivity(pm) != null) {
                            startActivity(fallbackIntent);
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error parsing intent scheme: " + url, e);
            }
            return true;
        }

        // 2. Handle UPI and Payment App URI schemes
        if (url.startsWith("upi://") || url.startsWith("phonepe://") ||
            url.startsWith("paytmmp://") || url.startsWith("tez://") ||
            url.startsWith("gpay://") || url.startsWith("credpay://") ||
            url.startsWith("bhim://") || url.startsWith("mobikwik://") ||
            url.startsWith("freecharge://") || url.startsWith("amazonpay://")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                PackageManager pm = getPackageManager();
                if (intent.resolveActivity(pm) != null) {
                    startActivity(intent);
                } else if (url.startsWith("upi://")) {
                    Intent chooser = Intent.createChooser(intent, "Pay via UPI");
                    startActivity(chooser);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error launching payment app: " + url, e);
            }
            return true;
        }

        // 3. Handle standard external non-HTTP schemes (tel:, mailto:, whatsapp:, etc.)
        if (url.startsWith("tel:") || url.startsWith("mailto:") || url.startsWith("whatsapp://")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            } catch (Exception e) {
                Log.e(TAG, "Error launching scheme: " + url, e);
            }
            return true;
        }

        // 4. Any other non-web scheme (excluding special web protocols)
        if (!url.startsWith("http://") && !url.startsWith("https://") &&
            !url.startsWith("data:") && !url.startsWith("blob:") &&
            !url.startsWith("javascript:") && !url.startsWith("about:")) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error launching external scheme: " + url, e);
            }
            return true;
        }

        return false;
    }

    private boolean isPaymentOrInternalUrl(Uri uri) {
        if (uri == null) return false;
        String host = uri.getHost();
        if (host == null) return false;
        host = host.toLowerCase();

        // Local or app host
        if (host.contains("roboticnaturalautofarming") || host.contains("assuretechnologies") || host.contains("railway.app") || host.equals("localhost")) {
            return true;
        }

        // Known payment gateways, wallets, and aggregators
        if (host.contains("razorpay.com") ||
            host.contains("phonepe.com") ||
            host.contains("paytm.com") ||
            host.contains("paytmbank.com") ||
            host.contains("npci.org.in") ||
            host.contains("google.com") ||
            host.contains("cashfree.com") ||
            host.contains("billdesk.com") ||
            host.contains("ccavenue.com") ||
            host.contains("payu.in") ||
            host.contains("mobikwik.com") ||
            host.contains("freecharge.in") ||
            host.contains("amazon.in") ||
            host.contains("amazon.com")) {
            return true;
        }

        // Bank 3D Secure ACS, OTP, and Netbanking verification domains
        if (host.contains("cardinalcommerce.com") ||
            host.contains("enstage.com") ||
            host.contains("arcot.com") ||
            host.contains("modirum.com") ||
            host.contains("visa.com") ||
            host.contains("mastercard.com") ||
            host.contains("rupay.co.in") ||
            host.contains("bank") ||
            host.contains("sbi.co.in") ||
            host.contains("hdfc") ||
            host.contains("icici") ||
            host.contains("axis") ||
            host.contains("kotak") ||
            host.contains("canarabank") ||
            host.contains("pnbindia")) {
            return true;
        }

        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getBridge() != null && getBridge().getWebView() != null) {
            getBridge().getWebView().setDownloadListener((url, userAgent, contentDisposition, mimetype, contentLength) -> {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
