/*
 * Copyright 2010 Colin McDonough
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.colinmcdonough.android.torch;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.View;

/*
 * Torch is an LED flashlight.
 */
public class Torch extends Activity {

  private static final String TAG = Torch.class.getSimpleName();

  private static final String WAKE_LOCK_TAG = "TORCH_WAKE_LOCK";

  private static final int COLOR_DARK = 0xCC000000;
  private static final int COLOR_LIGHT = 0xCCBFBFBF;
  private static final int COLOR_WHITE = 0xFFFFFFFF;

  private Camera mCamera;
  private boolean lightOn;
  private boolean previewOn;
  private View button;

  private WakeLock wakeLock;

  private void getCamera() {
    if (mCamera == null) {
      try {
        mCamera = Camera.open();
      } catch (RuntimeException e) {
      }
    }
  }

  /*
   * Called by the view (see main.xml)
   */
  public void toggleLight(View view) {
    if (lightOn) {
      turnLightOff();
    } else {
      turnLightOn();
    }
  }

  private void turnLightOn() {
    if (mCamera == null) {
      return;
    }
    lightOn = true;

    Parameters parameters = mCamera.getParameters();

    if (parameters == null) {
      // Use the screen as a flashlight (next best thing)
      button.setBackgroundColor(COLOR_WHITE);
      return;
    }

    List<String> flashModes = parameters.getSupportedFlashModes();

    // Check if camera flash exists
    if (flashModes == null) {
      // Use the screen as a flashlight (next best thing)
      button.setBackgroundColor(COLOR_WHITE);
      return;
    }

    String flashMode = parameters.getFlashMode();
    if (!Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
      // Turn on the flash
      if (flashModes.contains(Parameters.FLASH_MODE_TORCH)) {
        parameters.setFlashMode(Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);
        button.setBackgroundColor(COLOR_LIGHT);
        startWakeLock();
      } else {
        Log.e(TAG, "FLASH_MODE_TORCH not supported");
      }
    }
  }

  private void turnLightOff() {
    if (mCamera == null) {
      return;
    }
    lightOn = false;

    Parameters parameters = mCamera.getParameters();
    
    if (parameters == null) {
      // set the background to dark
      button.setBackgroundColor(COLOR_DARK);
      return;
    }
    
    List<String> flashModes = parameters.getSupportedFlashModes();
    String flashMode = parameters.getFlashMode();

    // Check if camera flash exists
    if (flashModes == null) {
      // set the background to dark
      button.setBackgroundColor(COLOR_DARK);
      return;
    }

    if (!Parameters.FLASH_MODE_OFF.equals(flashMode)) {
      // Turn off the flash
      if (flashModes.contains(Parameters.FLASH_MODE_OFF)) {
        parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);
        button.setBackgroundColor(COLOR_DARK);
        stopWakeLock();
      } else {
        Log.e(TAG, "FLASH_MODE_OFF not supported");
      }
    }
  }

  private void startPreview() {
    if (!previewOn && mCamera != null) {
      mCamera.startPreview();
      previewOn = true;
    }
  }

  private void stopPreview() {
    if (previewOn && mCamera != null) {
      mCamera.stopPreview();
      previewOn = false;
    }
  }

  private void startWakeLock() {
    if (wakeLock == null) {
      PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
      wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
    }
    wakeLock.acquire();
  }

  private void stopWakeLock() {
    if (wakeLock != null) {
      wakeLock.release();
    }
  }

  /** Called when the activity is first created. */
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.main);
    button = findViewById(R.id.button);
  }

  @Override
  public void onRestart() {
    super.onRestart();
  }

  @Override
  public void onStart() {
    super.onStart();
    getCamera();
    startPreview();
    turnLightOn();
  }

  @Override
  public void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onStop() {
    super.onStop();
    if (mCamera != null) {
      stopPreview();
      mCamera.release();
      mCamera = null;
    }
  }

  @Override
  public void onDestroy() {
    super.onDestroy();
    if (mCamera != null) {
      turnLightOff();
      stopPreview();
      mCamera.release();
    }
  }
}