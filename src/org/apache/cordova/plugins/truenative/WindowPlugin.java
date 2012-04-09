package org.apache.cordova.plugins.truenative;

import android.os.Process;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static junit.framework.Assert.*;

public class WindowPlugin extends ComponentPlugin {
  @Override
  protected Object newComponentInstance() {
    return new Window(this);
  }

  @Override
  protected void setupComponent(Object component, JSONObject options) {
    super.setupComponent(component, options);

    ((Window)component).setup(options);
  }

  @Override
  public PluginResult execute(
      String action, JSONArray args, String callbackId) {
    JSONObject options;
    try {
      if (action.equals("open")) { this.open(args.getJSONObject(0)); }
      else { return super.execute(action, args, callbackId); }
    } catch (JSONException e) {
      return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
    }

    return new PluginResult(PluginResult.Status.OK, "");
  }

  public static Window openWindow;
  public void open(final JSONObject options) {
    try {
      final JSONObject windowOptions = options.getJSONObject("window");

      ctx.runOnUiThread(new Runnable() {
        public void run() {
          Window window = 
            (Window)createComponent(windowOptions);
          window.open();
          openWindow = window;
        }
      });
    } catch (JSONException e) {
      e.printStackTrace();
      fail();
    }
  }
}
