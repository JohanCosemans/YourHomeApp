package net.yourhome.app.views;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import net.yourhome.app.bindings.AbstractBinding;
import net.yourhome.app.bindings.BindingController;
import net.yourhome.app.bindings.SensorBinding;
import net.yourhome.app.util.Configuration;
import net.yourhome.common.base.enums.ControllerTypes;
import net.yourhome.common.base.enums.zwave.DataHistoryOperations;
import net.yourhome.common.base.enums.zwave.DataHistoryPeriodTypes;
import net.yourhome.common.net.messagestructures.general.HistoryValues;
import net.yourhome.common.net.messagestructures.general.ValueHistoryMessage;
import net.yourhome.common.net.model.binding.ControlIdentifiers;
import net.yourhome.app.R;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import lecho.lib.hellocharts.formatter.AxisValueFormatter;
import lecho.lib.hellocharts.gesture.ZoomType;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.LineChartView;
/**
 * Meter chart with options for accumulated/delta and realtime/daily/monthly
 */
public class DataHistoryActivity extends Activity
{
    
	private AbstractBinding sensorBinding;
    private ControlIdentifiers identifiers;
	private SharedPreferences settings;
    private Activity me = this;
    private RelativeLayout layout;
    
    private DataHistoryPeriodTypes selectedPeriodType;
    private DataHistoryOperations selectedOperation;
    private int numberOfPoints = 0;
    private int offset = 0;

    private ProgressBar progressBar;
    private Spinner numberOfPointsSpinner;
    private Spinner periodSpinner;
    private Spinner operationSpinner;
    
    private String TAG = "SensorDetailActivity";
    private ImageButton previousButton;
    private ImageButton nextButton;
    private TextView title;
    private RelativeLayout chartView;
    
    private Map<DataHistoryPeriodTypes, List<SpinnerKeyValue<Integer,String>>> numberOfPointsMap;
    private List<SpinnerKeyValue<DataHistoryPeriodTypes,String>> periodTypes;
    private List<SpinnerKeyValue<DataHistoryOperations,String>> operations;
    
    
    private int initializedSpinners = 0;
    
    public DataHistoryActivity() {

		periodTypes = new ArrayList<SpinnerKeyValue<DataHistoryPeriodTypes,String>>();
		periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes,String>(DataHistoryPeriodTypes.REALTIME, "Realtime"));
		periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes,String>(DataHistoryPeriodTypes.DAILY, "Daily"));
		periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes,String>(DataHistoryPeriodTypes.WEEKLY, "Weekly"));
		periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes,String>(DataHistoryPeriodTypes.MONTHLY, "Monthly"));

		operations = new ArrayList<SpinnerKeyValue<DataHistoryOperations,String>>();
		operations.add(new SpinnerKeyValue<DataHistoryOperations,String>(DataHistoryOperations.AVERAGE, "Average"));
		operations.add(new SpinnerKeyValue<DataHistoryOperations,String>(DataHistoryOperations.DELTA, "Max - Min"));
		operations.add(new SpinnerKeyValue<DataHistoryOperations,String>(DataHistoryOperations.MIN, "Minimum"));
		operations.add(new SpinnerKeyValue<DataHistoryOperations,String>(DataHistoryOperations.MAX, "Maximum"));

		numberOfPointsMap = new HashMap<DataHistoryPeriodTypes,List<SpinnerKeyValue<Integer,String>>>();
    	
    	List<SpinnerKeyValue<Integer,String>> realtimePoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(100, "100 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(200, "200 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(300, "300 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(500, "500 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(1000, "1000 values"));
    	numberOfPointsMap.put(DataHistoryPeriodTypes.REALTIME, realtimePoints);

    	List<SpinnerKeyValue<Integer,String>> dailyPoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(7, "7 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(14, "14 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(30, "30 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(60, "60 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(120, "120 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(365, "365 days"));
    	numberOfPointsMap.put(DataHistoryPeriodTypes.DAILY, dailyPoints);

    	List<SpinnerKeyValue<Integer,String>> weeklyPoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(4, "4 weeks"));
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(8, "8 weeks"));
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(52, "52 weeks"));
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(104, "104 weeks"));
    	numberOfPointsMap.put(DataHistoryPeriodTypes.WEEKLY, weeklyPoints);

    	List<SpinnerKeyValue<Integer,String>> monthlyPoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	monthlyPoints.add(new SpinnerKeyValue<Integer,String>(12, "12 months"));
    	monthlyPoints.add(new SpinnerKeyValue<Integer,String>(24, "24 months"));
    	monthlyPoints.add(new SpinnerKeyValue<Integer,String>(36, "36 months"));
    	numberOfPointsMap.put(DataHistoryPeriodTypes.MONTHLY, monthlyPoints);
    	
    }
    
    private void loadData(String placeHolder, DataHistoryPeriodTypes timePeriod, DataHistoryOperations operation){
    	if(timePeriod != null && operation != null && numberOfPoints > 0) {
    		SensorDetailLoader loader = new SensorDetailLoader();
    		loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new SensorDetailTaskParams(placeHolder, timePeriod, operation));
    	}
    }
    
    private LineChartView createLineChart(){
		LineChartView lineChart = new LineChartView(getBaseContext());
		lineChart.setInteractive(true);
		lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
		float density = getResources().getDisplayMetrics().density;
		int paddingLeft = ChartUtils.dp2px(density, 12 + 4);//for default settings use ChartUtils.dp2px(density, 6 + 4)
		int padding = ChartUtils.dp2px(density, 6 + 4);//for default settings use ChartUtils.dp2px(density, 6 + 4)
		lineChart.setPadding(paddingLeft, padding,padding, 0);
		return lineChart;
    }
    
    private SimpleDateFormat lineDateFormater = new SimpleDateFormat("dd/MM HH:mm", Locale.getDefault());
    private SimpleDateFormat lineTimeFormater = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private SimpleDateFormat dailyDateFormat = new SimpleDateFormat("E d/M", Locale.getDefault());
    private SimpleDateFormat weeklyDateFormat = new SimpleDateFormat("d/M", Locale.getDefault());
    private SimpleDateFormat monthlyDateFormat = new SimpleDateFormat("MMM yy", Locale.getDefault());
    private Calendar today = Calendar.getInstance();
    private String getDateLabel(Date labelDate) {
    	Calendar labelCalendar = Calendar.getInstance();
		labelCalendar.setTime(labelDate);
		String formattedLabel = "";
		if(labelCalendar.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
		&& labelCalendar.get(Calendar.YEAR) == today.get(Calendar.YEAR)) {
			// If the record is on the same day, only show hours
			formattedLabel = lineTimeFormater.format(labelDate);
		}else {
			formattedLabel = lineDateFormater.format(labelDate);
		}
		return formattedLabel;
    }
    private LineChartData generateLineData(HistoryValues historyData) {
    	// Values
    	SimpleDateFormat timeFormatter = lineDateFormater;
        List<PointValue> values = new ArrayList<PointValue>();
        final Calendar firstDate = Calendar.getInstance();
        if(historyData.time.size() > 0) {
        	firstDate.setTime(new Date(historyData.time.get(historyData.time.size()-1)*1000L));        	
        }
		for(int i=0;i<historyData.value.size();i++) {
			int xIndex = (int) (historyData.time.get(i)-firstDate.getTimeInMillis()/1000L);
	        values.add(new PointValue(xIndex, historyData.value.get(i).floatValue()));
		}
		
        // Line graph
        Line line = new Line(values)
        		.setCubic(false)
        		.setFilled(true)
        		.setStrokeWidth(1)
        		.setHasPoints(false)
        		.setPointColor(Color.TRANSPARENT)
        		.setPointRadius(0)
        		.setHasLabels(false)
        		.setColor(Color.parseColor("#0A7EA8"));
        
        List<Line> lines = new ArrayList<Line>();
        lines.add(line);
        LineChartData data = new LineChartData();
        data.setLines(lines);
        Axis axisX = new Axis().setHasLines(true);
        Axis axisY = new Axis().setHasLines(true);
        if(historyData.valueUnit != null) {
        	axisY.setName(historyData.valueUnit);
        }
        axisX.setMaxLabelChars( timeFormatter.toPattern().length()+1);
        axisX.setFormatter(new AxisValueFormatter() {
			
			@Override
			public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {
				// TODO Auto-generated method stub
				return 0;
			}
			
			@Override
			public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {
				Date labelDate = new Date((long) (firstDate.getTimeInMillis()+value*1000L));
				char[] label = getDateLabel(labelDate).toCharArray();
	            System.arraycopy(label, 0, formattedValue, formattedValue.length - label.length, label.length);
				return label.length;
			}
		});
        
        axisX.setTextColor(Color.BLACK);
        axisY.setTextColor(Color.BLACK);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        
        
        return data;
    }
    
    private ColumnChartData generateBarData(HistoryValues historyData, SimpleDateFormat labelFormat) {
    	// Values
    	SimpleDateFormat timeFormatter = lineDateFormater;
        List<Column> columns = new ArrayList<Column>();
        List<AxisValue> axisValuesForX = new ArrayList<AxisValue>();

        String unit = historyData.valueUnit==null?"":" "+historyData.valueUnit;
        
        for(int i=historyData.value.size()-1;i>=0;i--) {
			// 1 column with 1 subcolumn per value
			SubcolumnValue value = new SubcolumnValue(historyData.value.get(i).floatValue(),Color.parseColor("#770A7EA8")); 
			List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
			value.setLabel(String.format("%.2f",historyData.value.get(i))+unit);
	        values.add(value);
	        
	        AxisValue xValue = new AxisValue(historyData.value.size()-1-i);
	        xValue.setLabel(labelFormat.format(new Date(historyData.time.get(i)*1000L)));
	        axisValuesForX.add(xValue);
	        Column column = new Column(values);
	        column.setHasLabelsOnlyForSelected(true);
	        column.setHasLabels(false);
	        columns.add(column);
		}
		
        ColumnChartData data = new ColumnChartData();
        data.setColumns(columns);
        Axis axisX = new Axis().setHasLines(false);
        axisX.setValues(axisValuesForX);
        axisX.setMaxLabelChars(labelFormat.toPattern().length());
        Axis axisY = new Axis().setHasLines(true).setAutoGenerated(true);
        if(unit != null && !unit.equals("")) {
        	axisY.setName(unit);
        }
        axisX.setMaxLabelChars( timeFormatter.toPattern().length()+1);
        axisX.setTextColor(Color.BLACK);
        axisY.setTextColor(Color.BLACK);
        data.setAxisXBottom(axisX);
        data.setAxisYLeft(axisY);
        
        
        return data;
    }
    private ColumnChartView createBarChart(){
		ColumnChartView columnChart = new ColumnChartView(getBaseContext());
		columnChart.setInteractive(true);
		columnChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
		float density = getResources().getDisplayMetrics().density;
		int paddingLeft = ChartUtils.dp2px(density, 12 + 4);//for default settings use ChartUtils.dp2px(density, 6 + 4)
		int padding = ChartUtils.dp2px(density, 6 + 4);//for default settings use ChartUtils.dp2px(density, 6 + 4)
		columnChart.setPadding(paddingLeft, padding,padding, 0);
		return columnChart;
    }
    
    
    private void updateChart(ValueHistoryMessage values) {
		chartView.removeAllViews();
		title.setText(values.title);
    	switch(selectedPeriodType) {
		case DAILY:
			ColumnChartView dailyColumnChart = createBarChart();
			dailyColumnChart.setColumnChartData(generateBarData(values.sensorValues, dailyDateFormat));
			chartView.addView(dailyColumnChart);
			break;
		case WEEKLY:
			ColumnChartView weeklyColumnChart = createBarChart();
			weeklyColumnChart.setColumnChartData(generateBarData(values.sensorValues, weeklyDateFormat));
			chartView.addView(weeklyColumnChart);
			break;
		case MONTHLY:
			ColumnChartView monthlyColumnChart = createBarChart();
			monthlyColumnChart.setColumnChartData(generateBarData(values.sensorValues, monthlyDateFormat));
			chartView.addView(monthlyColumnChart);
		break;
		case REALTIME:
			LineChartView lineChart = createLineChart();
			lineChart.setLineChartData(generateLineData(values.sensorValues));
			chartView.addView(lineChart);
		break;
	}
    }
    
	public void initView() {
    	
        /* ProgressBar */
    	progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setId(3);
        progressBar.setVisibility(View.INVISIBLE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        /* Button previous */
        previousButton = (ImageButton) findViewById(R.id.graph_left);
        previousButton.setBackground(Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_arrow_left, 20));
        previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				offset += numberOfPoints/2;
		    	loadData(null,selectedPeriodType, selectedOperation);
			}
        });  
        
        /* Button next */
        nextButton = (ImageButton) findViewById(R.id.graph_right);        
        nextButton.setBackground(Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_arrow_right, 20));
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(offset >= numberOfPoints/2) {
					offset -= numberOfPoints/2;
			    	loadData(null,selectedPeriodType, selectedOperation);
				}
			}
        });  

        /* Graph Title */
        title = (TextView)findViewById(R.id.graph_title);
        
        /* Window Layout */
        LinearLayout.LayoutParams chartLayoutParams = new LinearLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        chartView = (RelativeLayout) findViewById(R.id.chart);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int)(size.x * 0.9),(int)(size.y * 0.9));
        LinearLayout mainLayout = (LinearLayout) findViewById(R.id.graph_main_layout);
        mainLayout.setLayoutParams(layoutParams);
        
        // Attach close to action
 		ImageButton closeButton = (ImageButton)this.findViewById(R.id.ip_camera_close);
 		closeButton.setOnClickListener(new OnClickListener() {
 			@Override
 			public void onClick(View arg0) {
 				finish();
 			}
 		});
    }
    

	/** Called when the activity is first created.*/
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);   // Set screen on landscape
		setContentView(R.layout.zwave_meter);

		settings = getBaseContext().getSharedPreferences("USER", Context.MODE_PRIVATE);

		/* Set spinner values */

		operationSpinner = (Spinner) findViewById(R.id.operation);
		ArrayAdapter<?> operationsArrayAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, operations);
		operationSpinner.setAdapter(operationsArrayAdapter);

		periodSpinner = (Spinner) findViewById(R.id.period);
		ArrayAdapter<?> periodArryaAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, periodTypes);
		periodSpinner.setAdapter(periodArryaAdapter);

		/* Number of points spinner */
		numberOfPointsSpinner = (Spinner) findViewById(R.id.number_of_points);

		/* Instantiate Graph */
		layout = (RelativeLayout) findViewById(R.id.chart);

		// Get value that we are reporting on and get the last used report
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		String controllerIdentifier = extras.getString("controllerIdentifier");
		String nodeIdentifier = extras.getString("nodeIdentifier");
		String valueIdentifier = extras.getString("valueIdentifier");
		identifiers = new ControlIdentifiers();
		identifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
		identifiers.setNodeIdentifier(nodeIdentifier);
		identifiers.setValueIdentifier(valueIdentifier);
		List<AbstractBinding> bindingList = BindingController.getInstance().getBindingsFor(identifiers);
		this.sensorBinding = bindingList.get(0);

		readSettings();

		// Init default view and load data
		initView();

		periodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    offset = 0;
				//if(initializedSpinners++>=3) {
					// Get selected values
					SpinnerKeyValue<DataHistoryPeriodTypes, String> selectedItem = (SpinnerKeyValue<DataHistoryPeriodTypes, String>) parent.getItemAtPosition(pos);
					selectedPeriodType = selectedItem.getKey();
	
					// Update value list of amount spinner
					ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, numberOfPointsMap.get(selectedPeriodType));
					numberOfPointsSpinner.setAdapter(spinnerArrayAdapter);
					//numberOfPoints = numberOfPointsMap.get(selectedPeriodType).get(0).getKey();
	
					if (selectedPeriodType == DataHistoryPeriodTypes.REALTIME) {
						operationSpinner.setVisibility(View.GONE);
					} else {
						operationSpinner.setVisibility(View.VISIBLE);
					}
	
			    	//loadData(null,selectedPeriodType, selectedOperation);
			    	
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		numberOfPointsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    offset = 0;
				//if(initializedSpinners++>=3) {
					SpinnerKeyValue keyValue = (SpinnerKeyValue) parent.getItemAtPosition(position);
					numberOfPoints = (Integer) keyValue.getKey();
			    	loadData(null,selectedPeriodType, selectedOperation);
			    	//}
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		operationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    offset = 0;
				//if(initializedSpinners++>=3) {
					// Get selected values
					SpinnerKeyValue<DataHistoryOperations, String> selectedItem = (SpinnerKeyValue<DataHistoryOperations, String>) parent.getItemAtPosition(pos);
					selectedOperation = selectedItem.getKey();
					
					// destroy bar chart?
					createLineChart();
			    	loadData(null,selectedPeriodType, selectedOperation);
			    	//}
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	}

	@Override
    public void onDestroy() {
		super.onDestroy();
		writeSettings();
		
	}
	private void readSettings() {
		Set<String> graphDetails = settings.getStringSet("GRAPH_"+this.identifiers.getKey(), null);
			if(graphDetails != null) {
			for(String detailLine : graphDetails) {
				String[] detailLineSplit = detailLine.split("=");
				if(detailLineSplit.length == 2) {
					if(detailLineSplit[0].equals("PERIOD_TYPE")) {
						//this.selectedPeriodType = DataHistoryPeriodTypes.fromInt(Integer.parseInt(detailLineSplit[1]));
						// Update period type spinner
						Integer result = findInArray(periodTypes, DataHistoryPeriodTypes.fromInt(Integer.parseInt(detailLineSplit[1])));
						if(result != null) {
							periodSpinner.setSelection(result);
						}
					}else if(detailLineSplit[0].equals("OPERATION")) {
						// Update operations spinner
						//this.selectedOperation = DataHistoryOperations.fromInt(Integer.parseInt(detailLineSplit[1]));
						Integer result = findInArray(operations, DataHistoryOperations.fromInt(Integer.parseInt(detailLineSplit[1])));
						if(result != null) {
							operationSpinner.setSelection(result);
						}
					}
				}
			}
		
			// Update number of points spinner - will be overwritten by default selection of period spinner
	    	/*result = findInArray(numberOfPointsMap.get(selectedPeriodType), numberOfPoints);
			if(result != null) {
				ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(me,android.R.layout.simple_spinner_dropdown_item, numberOfPointsMap.get(selectedPeriodType));
	        	numberOfPointsSpinner.setAdapter(spinnerArrayAdapter);
				numberOfPointsSpinner.setSelection(result);
			}*/
			
		}
		
	}
	private Integer findInArray(List<?> spinnerKeyValue, Object lookupValue) {
		int i=0; boolean found = false;
		while(!found && i<spinnerKeyValue.size()) {
			SpinnerKeyValue<?, ?> castedSpinnerKeyValue = (SpinnerKeyValue<?,?>)spinnerKeyValue.get(i);
			if(castedSpinnerKeyValue.getKey().equals(lookupValue)) {
				found = true;
			}else {
				i++;
			}
		}
		return found?i:null;
	}
	private void writeSettings() {
		Editor edit = settings.edit();
		Set<String> graphDetails = new HashSet<String>();
		graphDetails.add("PERIOD_TYPE="+this.selectedPeriodType.convert());
		graphDetails.add("OPERATION="+this.selectedOperation.convert());
		//graphDetails.add("NUMBER_OF_POINTS="+this.numberOfPoints);
		edit.putStringSet("GRAPH_"+this.identifiers.getKey(), graphDetails);
		edit.commit();
	}
    private static class SensorDetailTaskParams {
    	String placeholder;
        DataHistoryPeriodTypes periodType;
        DataHistoryOperations operation;
        
        SensorDetailTaskParams(String placeholder, DataHistoryPeriodTypes periodType, DataHistoryOperations operation) {
            this.placeholder = placeholder;
            this.periodType = periodType;
            this.operation = operation;
        }
    }
    private class SensorDetailLoader extends AsyncTask<SensorDetailTaskParams,Void,ValueHistoryMessage> {
    	
        final ProgressBar progress;
        SensorDetailTaskParams taskParameters = null;
        public SensorDetailLoader(){
        	progress = (ProgressBar) findViewById(R.id.progress);
        }
        
        @Override
        protected void onPreExecute(){
        	progressBar.setVisibility(View.VISIBLE);
        }
        
		@Override
		protected ValueHistoryMessage doInBackground(SensorDetailTaskParams...series) {
			
			taskParameters = series[0];
			return SensorBinding.getHistoricValues(sensorBinding.getControlIdentifier(),offset,numberOfPoints,taskParameters.periodType, taskParameters.operation);
			
		}
    
        @Override
        protected void onPostExecute(ValueHistoryMessage result){
        	if(result != null) {
        		updateChart(result);
        	}
        	progressBar.setVisibility(View.GONE);
        	
        }
    }
    
}
