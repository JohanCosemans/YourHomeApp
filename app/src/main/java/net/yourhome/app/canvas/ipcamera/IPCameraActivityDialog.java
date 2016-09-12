package net.yourhome.app.canvas.ipcamera;

import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class IPCameraActivityDialog extends IPCameraActivity {

	public void onCreate(Bundle savedInstanceState) {
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);   // Set screen on landscape
		super.onCreate(savedInstanceState);
//		getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		//setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED); 

	}
}
