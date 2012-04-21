package org.apache.cordova.plugins.truenative;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.graphics.drawable.shapes.Shape;
import android.view.View;
import android.view.ViewGroup;

import org.apache.cordova.api.Plugin;
import org.apache.cordova.api.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static junit.framework.Assert.*;

public class ViewPlugin extends ComponentPlugin {
  protected class ViewData extends ComponentData {
    ViewSubclass.LayoutParams layoutParams = 
      new ViewSubclass.LayoutParams(0, 0, 0, 0);
    ViewSubclass parent;

    boolean hasBackgroundColor = false;
    int backgroundColor;
    int highlightedBackgroundColor = -1;
    boolean hasBorderColor = false;
    int borderColor;
    int borderRadius = 0;
    int borderWidth = 0;
  }

  @Override
  protected Object newComponentInstance(Context context) {
    return new ViewSubclass(context, this);
  }

  @Override
  protected ComponentData newComponentDataInstance() {
    return new ViewData();
  }

  @Override
  protected void setupComponent(Object component, JSONObject options) {
    super.setupComponent(component, options);

    setComponentProperties(
        component, options, 
        "top", "left", "width", "height", "backgroundColor", "borderRadius",
        "borderColor", "borderWidth");

    View view = (View)component;
    view.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        fireEvent(v, "click", null);
      }
    });

    try {
      JSONArray children = options.optJSONArray("children");
      if (children != null) {
        for (int i = 0; i < children.length(); ++i) {
          addChildView((ViewSubclass)view, children.getJSONObject(i));
        }
      }
    } catch(JSONException e) {
      e.printStackTrace();
      fail();
    }
  }

  @Override
  public PluginResult execute(
      String action, JSONArray args, String callbackId) {
    JSONObject options;
    try {
      if (action.equals("add")) { this.add(args.getJSONObject(0)); }
      else if (action.equals("sizeToFit")) { 
        this.sizeToFit(args.getJSONObject(0)); 
      }
      else { return super.execute(action, args, callbackId); }
    } catch (JSONException e) {
      return new PluginResult(PluginResult.Status.JSON_EXCEPTION);
    }

    return new PluginResult(PluginResult.Status.OK, "");
  }

  protected void updateLayoutParams(View view, ViewData data) {
    if (data.parent != null) {
      data.parent.updateViewLayout(view, data.layoutParams);
    }
  }

  protected int convertToPixels(Object dips) {
    return Math.round(((Integer)dips).intValue() 
        * getTrueNativeActivity().getResources().getDisplayMetrics().density);
  }

  protected int convertToDips(int pixels) {
    return Math.round(pixels 
        / getTrueNativeActivity().getResources().getDisplayMetrics().density);
  }

  public void onViewSizeChanged(
      View view, int w, int h, int oldw, int oldh) {
    JSONObject data = new JSONObject();
    try {
      data.put("width", convertToDips(w));
      data.put("height", convertToDips(h));
    } catch(Exception e) {
      e.printStackTrace();
      fail();
    }
    fireEvent(view, "resize", data);
  }

  @Override
  public Object getComponentProperty(Object component, String key) {
    View view = (View)component;
    ViewData data = (ViewData)getComponentData(component);

    if (key.equals("top")) {
      return convertToDips(data.layoutParams.y);
    } else if (key.equals("left")) {
      return convertToDips(data.layoutParams.x);
    } else if (key.equals("width")) {
      return convertToDips(view.getMeasuredWidth());
    } else if (key.equals("height")) {
      return convertToDips(view.getMeasuredHeight());
    } else {
      return super.getComponentProperty(component, key);
    }
  }

  private void updateBackground(View view, ViewData data) {
    if (data.borderWidth > 0) {
      assertTrue(
          "If you specify a borderWidth, you must also specify a borderColor"
          + " and backgroundColor",
          !data.hasBackgroundColor || !data.hasBorderColor);

      float[] radii = new float[8];
      for (int i = 0; i < 8; ++i) { radii[i] = data.borderRadius; }
      RoundRectShape roundRect = new RoundRectShape(radii, null, null); 

      view.setBackgroundDrawable(
          new ViewBackground(
            roundRect, data.backgroundColor, 
            data.borderColor, data.borderWidth));
    } else {
      // If we only have a background color but no border, don't use a shape at
      // all.

      if (data.hasBackgroundColor) {
        view.setBackgroundColor(data.backgroundColor);
      }
    }
  }

  // Borrowed from:
  // http://www.betaful.com/2012/01/programmatic-shapes-in-android/
  private class ViewBackground extends ShapeDrawable {
    private final Paint mFillPaint, mStrokePaint;
    private final int mBorderWidth;

    public ViewBackground(
        Shape s, int backgroundColor, int borderColor, int borderWidth) {
      super(s);

      mFillPaint = new Paint(this.getPaint());
      mFillPaint.setColor(backgroundColor);

      mStrokePaint = new Paint(mFillPaint);
      mStrokePaint.setStyle(Paint.Style.STROKE);
      mStrokePaint.setStrokeWidth(borderWidth);
      mStrokePaint.setColor(borderColor);

      mBorderWidth = borderWidth;
    }

    @Override
    protected void onDraw(Shape shape, Canvas canvas, Paint paint) {
      shape.resize(canvas.getClipBounds().right, canvas.getClipBounds().bottom);

      Matrix matrix = new Matrix();
      matrix.setRectToRect(
          new RectF(
            0, 0, 
            canvas.getClipBounds().right, canvas.getClipBounds().bottom),
          new RectF(
            mBorderWidth/2, mBorderWidth/2, 
            canvas.getClipBounds().right - mBorderWidth/2,
            canvas.getClipBounds().bottom - mBorderWidth/2),
          Matrix.ScaleToFit.FILL);
      canvas.concat(matrix);

      shape.draw(canvas, mFillPaint);
      if (mBorderWidth > 0) {
        shape.draw(canvas, mStrokePaint);
      }
    }
  }

  @Override
  public void setComponentProperty(Object component, String key, Object value) {
    View view = (View)component;
    ViewData data = (ViewData)getComponentData(component);

    if (key.equals("backgroundColor")) {
      data.backgroundColor = Util.parseColor((String)value);
      data.hasBackgroundColor = true;
      updateBackground(view, data);
    } else if (key.equals("highlightedBackgroundColor")) {
      data.highlightedBackgroundColor = Util.parseColor((String)value);
      updateBackground(view, data);
    } else if (key.equals("borderColor")) {
      data.borderColor = Util.parseColor((String)value);
      data.hasBorderColor = true;
      updateBackground(view, data);
    } else if (key.equals("borderRadius")) {
      data.borderRadius = convertToPixels(value);
      updateBackground(view, data);
    } else if (key.equals("borderWidth")) {
      data.borderWidth = convertToPixels(value);
      updateBackground(view, data);
    } else if (key.equals("top")) {
      data.layoutParams.y = convertToPixels(value);
      updateLayoutParams(view, data);
    } else if (key.equals("left")) {
      data.layoutParams.x = convertToPixels(value);
      updateLayoutParams(view, data);
    } else if (key.equals("width")) {
      data.layoutParams.width = convertToPixels(value);
      updateLayoutParams(view, data);
    } else if (key.equals("height")) {
      data.layoutParams.height = convertToPixels(value);
      updateLayoutParams(view, data);
    } else {
      super.setComponentProperty(component, key, value);
    }
  }

  private void addChildView(ViewSubclass parent, JSONObject childOptions) {
    View child = (View)createComponent(parent.getContext(), childOptions);
    ViewData childData = (ViewData)getComponentData(child);
    childData.parent = parent;
    parent.addView(child, childData.layoutParams);
  }

  public void add(final JSONObject options) {
    ctx.runOnUiThread(new Runnable() {
      public void run() {
        try {
          String parentID = options.getString("parentID");
          ViewSubclass parent = (ViewSubclass)getComponent(parentID);
          JSONObject childOptions = options.getJSONObject("child");
          addChildView(parent, childOptions);
        } catch(JSONException e) {
          e.printStackTrace();
          fail();
        }
      }
    });
  }

  public void sizeToFit(final JSONObject options) {
    fail();
  }
}
