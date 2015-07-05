package org.lx.miloliu.webview.capture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.View;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class WVCapture extends CordovaPlugin {
    private CordovaWebView _webView;
    private CordovaInterface _interface;
    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        _webView = webView;
        _interface = cordova;
    }

    @Override
    public void onReset() {
        super.onReset();
        _webView = null;
    }

    @Override
    public String getServiceName() {
        return "WVCapture";
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("capture")) {
            capture(args, callbackContext);
            return true;
        }
        return false;
    }

    protected void capture(JSONArray args, final CallbackContext callbackContext) {
        PluginResult result = null;
        if (_webView != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    View view = _webView.getView();

                    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);
                    view.draw(canvas);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream(bitmap.getByteCount());
                    Base64OutputStream base64OutputStream = new Base64OutputStream(stream, Base64.DEFAULT);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, base64OutputStream);

                    bitmap.recycle();

                    String pngStr = new String(stream.toByteArray());

                    JSONObject object = new JSONObject();
                    PluginResult result;
                    try {
                        object.put("pngStr", pngStr);
                        result = new PluginResult(PluginResult.Status.OK, object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result = new PluginResult(PluginResult.Status.ERROR);
                    }
                    finally {
                        try {
                            stream.close();
                            base64OutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    _webView.sendPluginResult(result, callbackContext.getCallbackId());
                }
            };
            _interface.getActivity().runOnUiThread(runnable);

        }
        else {
            result = new PluginResult(PluginResult.Status.ERROR);
            callbackContext.sendPluginResult(result);
        }



    }
}