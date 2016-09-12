package net.yourhome.app.widget;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.yourhome.app.util.APICaller;
import net.yourhome.app.util.ColorPickerActivity;
import net.yourhome.app.util.Configuration;
import net.yourhome.app.views.SpinnerKeyValue;
import net.yourhome.app.R;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.Toast;

public class WidgetConfigurationActivity extends Activity {
	private Activity me = this;
	private SharedPreferences settings;
	private final String LAST_USED_COLOR = "net.yourhome.controller.widget.LAST_USED_COLOR";
	private final String LAST_USED_BACKGROUND_COLOR = "net.yourhome.controller.widget.LAST_USED_BACKGROUND_COLOR";
	private final String COLOR_KEY_ICON = "net.yourhome.controller.util.selectedColor";
	private final String COLOR_KEY_ICON_BACKGROUND = "net.yourhome.controller.util.selectedBackgroundColor";

	public static enum WidgetTypes {
		SCENE(0);

		int value;

		WidgetTypes(int i) {
			this.value = i;
		};
	}

	private ArrayList<SpinnerKeyValue<WidgetTypes, String>> widgetTypes;
	private ArrayList<SpinnerKeyValue<String, String>> widgetActionTypes;

	//private SpinnerKeyValue<WidgetTypes, String> selectedWidgetType;
	private SpinnerKeyValue<String, String> selectedActionType;

	private String selectedIcon;
	private int iconColor = R.color.white;
	private int iconBackgroundColor = Color.parseColor("#FE5B28");
	private ImageView selectedIconPreview;

	public WidgetConfigurationActivity() {
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		Log.d("widget", "Result from colorpicker!");

		// Check which request we're responding to
		if (requestCode == ColorPickerActivity.REQUEST_COLOR) {
			// Make sure the request was successful
			if (resultCode == RESULT_OK && data != null) {
				if(data.getStringExtra(ColorPickerActivity.STORE_COLOR_KEY).equals(COLOR_KEY_ICON)) {
					iconColor = data.getIntExtra(COLOR_KEY_ICON, iconColor);
					Log.d("widget", "Result from colorpicker: " + iconColor);
					if (selectedIconPreview != null) {
						Bitmap iconBitmap = Configuration.getInstance().getWidgetIcon(me, selectedIcon, 100, iconColor);
						selectedIconPreview.setImageBitmap(iconBitmap);
					}
				}else if(data.getStringExtra(ColorPickerActivity.STORE_COLOR_KEY).equals(COLOR_KEY_ICON_BACKGROUND)) {
					iconBackgroundColor = data.getIntExtra(COLOR_KEY_ICON_BACKGROUND, iconColor);
					Log.d("widget", "Result from colorpicker: " + iconBackgroundColor);
					if (selectedIconPreview != null) {
						//Bitmap iconBitmap = Configuration.getInstance().getWidgetIcon(me, selectedIcon, 100, iconColor);
						//selectedIconPreview.setImageBitmap(iconBitmap);
						final Spinner widgetIconSpinner = (Spinner) findViewById(R.id.widget_icon);
						widgetIconSpinner.setBackgroundColor(iconBackgroundColor);
					}
				}

			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);		
		Log.d("WidgetConfigurationActivity", "onCreate");
		setContentView(R.layout.widget_configuration);
		widgetTypes = new ArrayList<SpinnerKeyValue<WidgetTypes, String>>();
		widgetTypes.add(new SpinnerKeyValue<WidgetTypes, String>(WidgetTypes.SCENE, getResources().getString(R.string.widget_activate_scene)));


		if(settings == null) {
			settings = getBaseContext().getSharedPreferences("USER", Context.MODE_PRIVATE);
		}
		// Check last used color and default this one
		iconColor = settings.getInt(LAST_USED_COLOR, iconColor);
		iconBackgroundColor = settings.getInt(LAST_USED_BACKGROUND_COLOR, iconColor);

		/* Fill spinners */

		// Fetch all scenes. Once completed, fill the dropdown.
		APICaller loader = new WidgetCaller(me);
    	loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "/Scenes");
    	
		/*Spinner widgetTypeSpinner = (Spinner) findViewById(R.id.widget_type);
        ArrayAdapter widgetTypeSpinnerAdapter = new ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item, widgetTypes);
        widgetTypeSpinner.setAdapter(widgetTypeSpinnerAdapter);
        widgetTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, 
		            int pos, long id) {
		    	// Get selected values
		    	selectedWidgetType = (SpinnerKeyValue<WidgetTypes,String>) parent.getItemAtPosition(pos);
		    	switch(selectedWidgetType.getKey()) {
		    	/*case	ZWAVE_GENERAL_COMMAND :
    				widgetActionTypes = new ArrayList<SpinnerKeyValue<String,String>>();
		    		widgetActionTypes.add(new SpinnerKeyValue<String,String>(GeneralCommands.ALL_OFF.convert(), "All Off"));
		    		widgetActionTypes.add(new SpinnerKeyValue<String,String>(GeneralCommands.ALL_ON.convert(), "All On"));

			    	Spinner widgetActionTypeSpinner = (Spinner) findViewById(R.id.widget_action);
			        ArrayAdapter widgetActionTypeSpinnerAdapter = new ArrayAdapter(me,android.R.layout.simple_spinner_dropdown_item, widgetActionTypes);
			        widgetActionTypeSpinner.setAdapter(widgetActionTypeSpinnerAdapter);	
			        
		    		break;*//*
		    	case SCENE :
		    		// Fetch all scenes. Once completed, fill the dropdown.
		    		/*APICaller loader = new APICaller(me);
		        	loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, "/Scenes");*//*
		    		break;
		    	}	    	
		    }
		    public void onNothingSelected(AdapterView<?> parent) {}
        });
        */

        final Spinner widgetIconSpinner = (Spinner) findViewById(R.id.widget_icon);
        widgetIconSpinner.setAdapter(new IconAdapter(me, R.layout.spinner_icon_row, HomeWidgetProvider.getIcons()));
        widgetIconSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    	selectedIcon = (String) parent.getItemAtPosition(pos);
		    	selectedIconPreview = (ImageView) view.findViewById(R.id.image);
		    }
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
        });
		widgetIconSpinner.setBackgroundColor(iconBackgroundColor);

		Spinner widgetActionTypeSpinner = (Spinner) findViewById(R.id.widget_action);
        widgetActionTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
		    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
		    	selectedActionType = (SpinnerKeyValue<String,String>) parent.getItemAtPosition(pos);

				EditText label = (EditText)findViewById(R.id.widget_label);
		    	label.setText(selectedActionType.getValue());

		    	//widgetIconSpinner.requestFocus();
		    }
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
        });
        
        
		/* Buttons */

        Drawable openColorPickerIcon = Configuration.getInstance().getAppIconDrawable(this.getBaseContext(), R.string.icon_search, 30);
        Drawable cancelButtonIcon = Configuration.getInstance().getAppIconDrawable(this.getBaseContext(), R.string.icon_close, 30);
        Drawable createButtonIcon = Configuration.getInstance().getAppIconDrawable(this.getBaseContext(), R.string.icon_file_o, 30);

        Button openColorPickerButton = (Button) findViewById(R.id.widget_start_colorpicker_btn);
        openColorPickerButton.setCompoundDrawablesWithIntrinsicBounds(openColorPickerIcon, null, null, null);
        openColorPickerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(me, ColorPickerActivity.class);
				Bundle intentExtras = new Bundle();
				intentExtras.putString(ColorPickerActivity.STORE_COLOR_KEY, COLOR_KEY_ICON);
				intentExtras.putInt(COLOR_KEY_ICON, iconColor);
				intentExtras.putBoolean(ColorPickerActivity.SHOW_CIRCLE_PICKER, true);
				intentExtras.putBoolean(ColorPickerActivity.SHOW_OPACITY_SLIDER, false);
				intent.putExtras(intentExtras);
				startActivityForResult(intent, ColorPickerActivity.REQUEST_COLOR);
			}
		});
		Button openBackgroundColorPickerButton = (Button) findViewById(R.id.widget_background_start_colorpicker_btn);
		openBackgroundColorPickerButton.setCompoundDrawablesWithIntrinsicBounds(openColorPickerIcon, null, null, null);
		openBackgroundColorPickerButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(me,ColorPickerActivity.class);
				Bundle intentExtras =  new Bundle();
				intentExtras.putString(ColorPickerActivity.STORE_COLOR_KEY, COLOR_KEY_ICON_BACKGROUND);
				intentExtras.putInt(COLOR_KEY_ICON_BACKGROUND, iconBackgroundColor);
                intentExtras.putBoolean(ColorPickerActivity.SHOW_CIRCLE_PICKER, true);
                intentExtras.putBoolean(ColorPickerActivity.SHOW_OPACITY_SLIDER, false);
				intent.putExtras(intentExtras);
				startActivityForResult(intent, ColorPickerActivity.REQUEST_COLOR);
			}
		});
        
		Button cancelButton = (Button) findViewById(R.id.widget_cancel_btn);
		cancelButton.setCompoundDrawablesWithIntrinsicBounds(cancelButtonIcon, null, null, null);
		cancelButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				setResult(RESULT_CANCELED);
				finish();
			}
		});
		
		Button createButton = (Button) findViewById(R.id.widget_create_btn);
		createButton.setCompoundDrawablesWithIntrinsicBounds(createButtonIcon, null, null, null);
		createButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
				Intent intent = getIntent();
				Bundle extras = intent.getExtras();
				if (extras != null) {
					mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
				}
				
				EditText label = (EditText)findViewById(R.id.widget_label);
				
				/* Build widget view */
				Editor edit = settings.edit();
				Set<String> widgetConfig = new HashSet<String>();
				//widgetConfig.add("WIDGET_TYPE="+selectedWidgetType.getKey());
				widgetConfig.add("WIDGET_TYPE="+WidgetTypes.SCENE);
				widgetConfig.add("WIDGET_ACTION_TYPE="+selectedActionType.getKey());
				widgetConfig.add("WIDGET_LABEL="+label.getText());
				widgetConfig.add("WIDGET_ICON="+selectedIcon);
				widgetConfig.add("WIDGET_COLOR="+iconColor);
				widgetConfig.add("WIDGET_BACKGROUND_COLOR="+iconBackgroundColor);
				edit.putStringSet("WIDGET_"+mAppWidgetId, widgetConfig);
				edit.commit();
					
				RemoteViews views = HomeWidgetProvider.buildWidgetView(getBaseContext(), 
																		mAppWidgetId,
																		//selectedWidgetType.getKey(),
																		WidgetTypes.SCENE,
																		selectedActionType.getKey(),
																		label.getText().toString(),
																		selectedIcon,
																		iconColor,
																		iconBackgroundColor);
				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
				if(views != null) {
		            // Tell the AppWidgetManager to perform an update on the app widget
					AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getBaseContext());
		            appWidgetManager.updateAppWidget(mAppWidgetId, views);   
	
		            // Store last used color
					edit.putInt(LAST_USED_COLOR, iconColor);
					edit.commit();
					
		            // Build result
					setResult(RESULT_OK, resultValue);
					finish();
				} else {
					setResult(RESULT_CANCELED, resultValue);
					finish();
				}
			}
		});	
	}
	
	public void onDestroy() {
		Editor edit = settings.edit();
		edit.putInt(LAST_USED_COLOR, iconColor);
		edit.putInt(LAST_USED_BACKGROUND_COLOR, iconBackgroundColor);
		edit.commit();
		super.onDestroy();
	}

	public class IconAdapter extends ArrayAdapter<String> {

		public IconAdapter(Context context, int textViewResourceId, List<String> objects) {
			super(context, textViewResourceId, objects);
		}

		@Override
		public View getDropDownView(int position, View convertView, ViewGroup parent) {
			return getView(position, convertView, parent);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = getLayoutInflater();
			View row = inflater.inflate(R.layout.spinner_icon_row, parent, false);
			ImageView icon = (ImageView) row.findViewById(R.id.image);
			Bitmap iconBitmap = Configuration.getInstance().getWidgetIcon(me, this.getItem(position), 100, iconColor);
			icon.setImageBitmap(iconBitmap);

			//row.setBackgroundColor(getResources().getColor(R.color.grey));
			return row;
		}
	}

	protected class WidgetCaller extends APICaller {

		public WidgetCaller(Context context) {
			super(context);
		}

		private Configuration configuration;
		private Context context;

		protected void onPreExecute() {
			((ProgressBar) findViewById(R.id.widget_action_loading)).setVisibility(View.VISIBLE);
		}

		@Override
		protected void onPostExecute(String result) {
			if (result == null) {
				Toast.makeText(me, "Could not connect to homeserver to retrieve scenes", Toast.LENGTH_LONG).show();
			} else {

				widgetActionTypes = new ArrayList<SpinnerKeyValue<String, String>>();
				try {
					JSONArray scenesArray = new JSONArray(result);
					for (int i = 0; i < scenesArray.length(); i++) {
						JSONObject scenesObject = scenesArray.getJSONObject(i);
						try {
							String name = scenesObject.getJSONObject("description").getString("name");
							widgetActionTypes.add(new SpinnerKeyValue<String, String>(scenesObject.getInt("id") + "", name));
						}catch(JSONException json){json.printStackTrace();}
					}
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				Spinner widgetActionTypeSpinner = (Spinner) findViewById(R.id.widget_action);
				ArrayAdapter widgetActionTypeSpinnerAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, widgetActionTypes);
				widgetActionTypeSpinner.setAdapter(widgetActionTypeSpinnerAdapter);
			}

			((ProgressBar) findViewById(R.id.widget_action_loading)).setVisibility(View.INVISIBLE);
		}
	}

}
