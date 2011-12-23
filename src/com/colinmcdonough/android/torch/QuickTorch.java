package com.colinmcdonough.android.torch;

import android.app.Activity;
import android.content.Intent;

public class QuickTorch extends Activity {
  
  /**
   * Start Torch when triggered
   */
  @Override
  public void onStart() {
    super.onStart();
    if (Torch.getTorch() == null) {
      Intent intent = new Intent(this, Torch.class);
      startActivity(intent);
    } else {
    }
    finish();
  }
}
