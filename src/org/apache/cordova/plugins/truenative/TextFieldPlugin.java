package org.apache.cordova.plugins.truenative;

import android.text.InputType;
import android.view.View;
import android.widget.EditText;

import android.content.Context;
import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static junit.framework.Assert.*;

public class TextFieldPlugin extends ViewPlugin {
  protected class TextFieldData extends ViewData {
  }

  @Override
  protected Object newComponentInstance(Context context) {
    return new TextFieldSubclass(context, this);
  }

  @Override
  protected ComponentData newComponentDataInstance() {
    return new TextFieldData();
  }

  @Override
  protected void setupComponent(Object component, JSONObject options) {
    super.setupComponent(component, options);

    setComponentProperties(
      component, options,
      "hint");

    EditText textField = (EditText)component;
    textField.setSingleLine(true);
  }

  @Override
  public Object getComponentProperty(Object component, String key) {
    EditText textField = (EditText)component;
    TextFieldData data = (TextFieldData)getComponentData(component);

    if (key.equals("")) {
      return null;
    } else {
      return super.getComponentProperty(component, key);
    }
  }

  @Override
  public void setComponentProperty(Object component, String key, Object value) {
    EditText textField = (EditText)component;
    TextFieldData data = (TextFieldData)getComponentData(component);

    if (key.equals("hint")) {
      textField.setHint((String)value);
    } else if (key.equals("text")) {
      textField.setText((String)value);
    } else {
      super.setComponentProperty(component, key, value);
    }
  }

  @Override
  public void sizeToFit(final JSONObject options) {
    ctx.runOnUiThread(new Runnable() {
      public void run() {
        try {
          String viewID = options.getString("viewID");
          EditText textField = (EditText)getComponent(viewID);
          TextFieldData data = (TextFieldData)getComponentData(textField);

          textField.measure(0, 0);
          data.layoutParams.width = textField.getMeasuredWidth();
          data.layoutParams.height = textField.getMeasuredHeight();
          updateLayoutParams(textField, data);

        } catch(JSONException e) {
          e.printStackTrace();
          fail();
        }
      }
    });
  }
}
