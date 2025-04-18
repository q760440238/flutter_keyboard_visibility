package com.jrai.flutter_keyboard_visibility;

import android.app.Activity;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import io.flutter.embedding.engine.plugins.activity.ActivityAware;
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding;
import io.flutter.plugin.common.BinaryMessenger;
import io.flutter.plugin.common.EventChannel;

public class FlutterKeyboardVisibilityPlugin implements FlutterPlugin, ActivityAware, EventChannel.StreamHandler, ViewTreeObserver.OnGlobalLayoutListener {
  private EventChannel eventChannel;
  private EventChannel.EventSink eventSink;
  private View mainView;
  private boolean isVisible;

  @Override
  public void onAttachedToEngine(FlutterPluginBinding flutterPluginBinding) {
    init(flutterPluginBinding.getBinaryMessenger());
  }

  private void init(BinaryMessenger messenger) {
    eventChannel = new EventChannel(messenger, "flutter_keyboard_visibility");
    eventChannel.setStreamHandler(this);
  }

  @Override
  public void onDetachedFromEngine(FlutterPluginBinding binding) {
    releaseEventChannel();
    unregisterListener();
    releaseAll();
  }

  @Override
  public void onAttachedToActivity(ActivityPluginBinding binding) {
    listenForKeyboard(binding.getActivity());
  }

  @Override
  public void onDetachedFromActivityForConfigChanges() {
    unregisterListener();
  }

  @Override
  public void onReattachedToActivityForConfigChanges(ActivityPluginBinding binding) {
    listenForKeyboard(binding.getActivity());
  }

  @Override
  public void onDetachedFromActivity() {
    unregisterListener();
  }

  @Override
  public void onListen(Object o, EventChannel.EventSink eventSink) {
    this.eventSink = eventSink;
  }

  @Override
  public void onCancel(Object o) {
    this.eventSink = null;
  }

  @Override
  public void onGlobalLayout() {
    if (mainView != null) {
      Rect r = new Rect();
      mainView.getWindowVisibleDisplayFrame(r);
      boolean newState = ((double) r.height() / mainView.getRootView().getHeight()) < 0.85;
      if (newState != isVisible) {
        isVisible = newState;
        if (eventSink != null) {
          eventSink.success(isVisible ? 1 : 0);
        }
      }
    }
  }

  private void listenForKeyboard(Activity activity) {
    unregisterListener(); // 避免重复注册
    mainView = activity.findViewById(android.R.id.content);
    if (mainView != null) {
      mainView.getViewTreeObserver().addOnGlobalLayoutListener(this);
    }
  }

  private void unregisterListener() {
    if (mainView != null) {
      mainView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
      mainView = null;
    }
  }

  private void releaseEventChannel() {
    if (eventChannel != null) {
      eventChannel.setStreamHandler(null);
      eventChannel = null;
    }
  }

  private void releaseAll() {
    eventSink = null;
  }
}