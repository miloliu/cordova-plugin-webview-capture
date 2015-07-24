package org.lx.miloliu.webview.capture;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.Base64;
import android.util.Base64OutputStream;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

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

    private static final int THUMB_SIZE = 150;

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

                    TextureView textureView = findTextureView((ViewGroup) view);

                    Bitmap bitmap = null;
                    if (textureView != null) {
                        bitmap = textureView.getBitmap();//Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                        //Canvas canvas = new Canvas(bitmap);
                        //view.draw(canvas);
                    }
                    else {
                        View rootView = _interface.getActivity().getWindow().getDecorView().getRootView();
                        rootView.setDrawingCacheEnabled(true);
                        bitmap = Bitmap.createBitmap(view.getDrawingCache());
                        rootView.setDrawingCacheEnabled(false);
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream(bitmap.getByteCount());
                    Base64OutputStream base64OutputStream = new Base64OutputStream(stream, Base64.NO_WRAP);
                    bitmap.compress(Bitmap.CompressFormat.PNG, 80, base64OutputStream);

                    Bitmap thumbnail = Bitmap.createScaledBitmap(bitmap, THUMB_SIZE, THUMB_SIZE, true);
                    ByteArrayOutputStream thumbStream = new ByteArrayOutputStream(thumbnail.getByteCount());
                    Base64OutputStream thumbBase64OutputStream = new Base64OutputStream(thumbStream, Base64.NO_WRAP);
                    thumbnail.compress(Bitmap.CompressFormat.PNG, 100, thumbBase64OutputStream);

                    //bitmap.recycle();

                    String pngStr = new String(stream.toByteArray());
                    String pngThumbStr = new String(thumbStream.toByteArray());

                    JSONObject object = new JSONObject();
                    PluginResult result;
                    try {
                        object.put("pngStr", pngStr);
                        object.put("pngThumbStr", pngThumbStr);
                        result = new PluginResult(PluginResult.Status.OK, object);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        result = new PluginResult(PluginResult.Status.ERROR);
                    } finally {
                        try {
                            stream.close();
                            thumbStream.close();
                            base64OutputStream.close();
                            thumbBase64OutputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    _webView.sendPluginResult(result, callbackContext.getCallbackId());
                }
            };
            _interface.getActivity().runOnUiThread(runnable);

        } else {
            result = new PluginResult(PluginResult.Status.ERROR);
            callbackContext.sendPluginResult(result);
        }


    }

    private TextureView findTextureView(ViewGroup group) {
        int cnt = group.getChildCount();
        for (int i = 0; i <  cnt; i++) {
            View view = group.getChildAt(i);
            if (view instanceof TextureView) {
                String parentClassName = view.getParent().getClass().toString();
                if (parentClassName.contains("XWalk")) {
                    return (TextureView) view;
                }
            }
            else if (view instanceof ViewGroup) {
                return findTextureView((ViewGroup)view);
            }
        }

        return null;
    }
}
