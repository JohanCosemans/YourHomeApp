/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
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
package net.yourhome.app.views;

import android.app.Activity;
/**
 * Meter chart with options for accumulated/delta and realtime/daily/monthly
 */
public class MeterDetailActivity extends Activity
{
	/*private AbstractBinding sensorBinding;
	
    private ControlIdentifiers identifiers;
	private SharedPreferences settings;
    private Activity me = this;
    private RelativeLayout layout;
    
    private ZWaveMeterPeriodTypes selectedPeriodType;
    private ZWaveMeterOperations selectedOperation;
    private int numberOfPoints = 100;
    private int offset = 0;

	private TChart chart;
    private ProgressBar progressBar;
    private Spinner numberOfPointsSpinner;
    private Spinner periodSpinner;
    private Spinner operationSpinner;
    
    private String TAG = "SensorDetailActivity";
    private ImageButton previousButton;
    private ImageButton nextButton;
    private TextView title;
    
    private Map<ZWaveMeterPeriodTypes, List<SpinnerKeyValue<Integer,String>>> numberOfPointsMap;
    private List<SpinnerKeyValue<ZWaveMeterPeriodTypes,String>> periodTypes;
    private List<SpinnerKeyValue<ZWaveMeterOperations,String>> operations;
    
    private boolean initialized = false;
    
    public MeterDetailActivity() {

		periodTypes = new ArrayList<SpinnerKeyValue<ZWaveMeterPeriodTypes,String>>();
		periodTypes.add(new SpinnerKeyValue<ZWaveMeterPeriodTypes,String>(ZWaveMeterPeriodTypes.REALTIME, "Realtime"));
		periodTypes.add(new SpinnerKeyValue<ZWaveMeterPeriodTypes,String>(ZWaveMeterPeriodTypes.DAILY, "Daily"));
		periodTypes.add(new SpinnerKeyValue<ZWaveMeterPeriodTypes,String>(ZWaveMeterPeriodTypes.WEEKLY, "Weekly"));
		periodTypes.add(new SpinnerKeyValue<ZWaveMeterPeriodTypes,String>(ZWaveMeterPeriodTypes.MONTHLY, "Monthly"));

		operations = new ArrayList<SpinnerKeyValue<ZWaveMeterOperations,String>>();
		operations.add(new SpinnerKeyValue<ZWaveMeterOperations,String>(ZWaveMeterOperations.AVERAGE, "Average"));
		operations.add(new SpinnerKeyValue<ZWaveMeterOperations,String>(ZWaveMeterOperations.DELTA, "Max - Min"));
		operations.add(new SpinnerKeyValue<ZWaveMeterOperations,String>(ZWaveMeterOperations.MIN, "Minimum"));
		operations.add(new SpinnerKeyValue<ZWaveMeterOperations,String>(ZWaveMeterOperations.MAX, "Maximum"));

		numberOfPointsMap = new HashMap<ZWaveMeterPeriodTypes,List<SpinnerKeyValue<Integer,String>>>();
    	
    	List<SpinnerKeyValue<Integer,String>> realtimePoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(100, "100 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(200, "200 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(300, "300 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(300, "500 values"));
    	realtimePoints.add(new SpinnerKeyValue<Integer,String>(300, "1000 values"));
    	numberOfPointsMap.put(ZWaveMeterPeriodTypes.REALTIME, realtimePoints);

    	List<SpinnerKeyValue<Integer,String>> dailyPoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(7, "7 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(14, "14 days"));
    	dailyPoints.add(new SpinnerKeyValue<Integer,String>(30, "30 days"));
    	numberOfPointsMap.put(ZWaveMeterPeriodTypes.DAILY, dailyPoints);

    	List<SpinnerKeyValue<Integer,String>> weeklyPoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(4, "4 weeks"));
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(8, "8 weeks"));
    	weeklyPoints.add(new SpinnerKeyValue<Integer,String>(52, "52 weeks"));
    	numberOfPointsMap.put(ZWaveMeterPeriodTypes.WEEKLY, weeklyPoints);

    	List<SpinnerKeyValue<Integer,String>> monthlyPoints = new ArrayList<SpinnerKeyValue<Integer,String>>();
    	monthlyPoints.add(new SpinnerKeyValue<Integer,String>(12, "12 months"));
    	monthlyPoints.add(new SpinnerKeyValue<Integer,String>(24, "24 months"));
    	monthlyPoints.add(new SpinnerKeyValue<Integer,String>(36, "36 months"));
    	numberOfPointsMap.put(ZWaveMeterPeriodTypes.MONTHLY, monthlyPoints);
    	
    }
    
    private void loadData(Series series, ZWaveMeterPeriodTypes timePeriod, ZWaveMeterOperations operation){
    	SensorDetailLoader loader = new SensorDetailLoader();
    	loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new SensorDetailTaskParams(series, timePeriod, operation));
    }
    
    private Area createRealtimeChart(TChart chart) {
    	chart.removeAllSeries();
    	Area realtime = new Area();
    	chart.addSeries(realtime);
    	realtime.getXValues().setDateTime(true);
        realtime.getAreaLines().setVisible(false);
        realtime.setTransparency(35);
        realtime.getLinePen().setWidth(4);
        realtime.getLinePen().setColor(com.steema.teechart.drawing.Color.fromArgb(100,100,156));
        realtime.getLinePen().setTransparency(50);
        chart.getAxes().getBottom().getLabels().setDateTimeFormat("HH:mm");
        return realtime;
    }
    
    private Bar createDailyChart(TChart chart) {
    	chart.removeAllSeries();
    	Bar dailyBarChart = new Bar();
    	dailyBarChart.getMarks().getArrow().setVisible(false);
    	dailyBarChart.getMarks().setStyle(MarksStyle.VALUE);
    	dailyBarChart.getMarks().setTransparent(true);
    	dailyBarChart.getMarks().getFont().setSize(28);
    	chart.addSeries(dailyBarChart);
    	return dailyBarChart;
    }
    
    private Bar createWeeklyChart(TChart chart) {
    	chart.removeAllSeries();
    	Bar weeklyBarChart = new Bar();
    	weeklyBarChart.getMarks().getArrow().setVisible(false);
    	weeklyBarChart.getMarks().setStyle(MarksStyle.VALUE);
    	weeklyBarChart.getMarks().setTransparent(true);
    	weeklyBarChart.getMarks().getFont().setSize(28);
    	chart.addSeries(weeklyBarChart);
    	return weeklyBarChart;
    }
    
    private Bar createMonthlyChart(TChart chart) {
    	chart.removeAllSeries();
    	Bar monthlyBarChart = new Bar();
    	monthlyBarChart.getMarks().getArrow().setVisible(false);
    	monthlyBarChart.getMarks().setStyle(MarksStyle.VALUE);
    	monthlyBarChart.getMarks().setTransparent(true);
    	monthlyBarChart.getMarks().getFont().setSize(28);
    	chart.addSeries(monthlyBarChart);
    	return monthlyBarChart;
    }
    
    @SuppressLint("NewApi")
	public void initView() {
		chart = new TChart(me);
        chart.getWalls().setVisible(false);
        chart.getLegend().setVisible(false);
		chart.getHeader().setVisible(false);
		chart.getGraphics3D().getAspect().setView3D(false);
        chart.getAxes().getBottom().getLabels().getFont().setSize(26);
        chart.getAxes().getBottom().getGrid().setTransparency(75);
        chart.getAxes().getLeft().getLabels().getFont().setSize(26);
        chart.getAxes().getLeft().getGrid().setTransparency(75);
        chart.getZoom().setAllow(false);
        chart.getZoom().setActive(false);
        chart.getPanning().setActive(false);
        chart.getPanning().setAllow(ScrollMode.NONE);
        chart.getPanel().setColor(com.steema.teechart.drawing.Color.TRANSPARENT);
        chart.getPanel().setTransparent(true);
        chart.setBackground(com.steema.teechart.drawing.Color.TRANSPARENT);
        chart.setPadding(0,5,0,0);
        chart.setId(2); 
        chart.getPanel().setBevelOuter(BevelStyle.NONE);
        chart.getPanel().setBevelInner(BevelStyle.NONE);
        chart.getPanel().setBevelWidth(0);
        chart.getPanel().getBorderPen().setColor(com.steema.teechart.drawing.Color.TRANSPARENT);
    	chart.getPanel().getBorderPen().setVisible(false);
    	chart.getPanel().setBorderRound(0);
    	chart.getPanel().setMarginBottom(1);
    	chart.getPanel().setMarginTop(1);
    	chart.getPanel().setMarginLeft(1);
    	chart.getPanel().setMarginRight(1);
    	
        /* ProgressBar *//*
    	progressBar = (ProgressBar) findViewById(R.id.progress);
        progressBar.setId(3);
        progressBar.setVisibility(View.INVISIBLE);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);

        /* Button previous *//*
        previousButton = (ImageButton) findViewById(R.id.graph_left);
        previousButton.setBackground(Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_arrow_left, 20));
        previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				offset += numberOfPoints/2;
				updateChart();
			}
        });  
        
        /* Button next *//*
        nextButton = (ImageButton) findViewById(R.id.graph_right);        
        nextButton.setBackground(Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_arrow_right, 20));
        nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if(offset >= numberOfPoints/2) {
					offset -= numberOfPoints/2;
					updateChart();
				}
			}
        });  

        /* Graph Title *//*
        title = (TextView)findViewById(R.id.graph_title);
        
        /* Window Layout *//*
        RelativeLayout.LayoutParams lpChart = new RelativeLayout.LayoutParams(
        		RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        lpChart.addRule(RelativeLayout.ABOVE, previousButton.getId());
        lpChart.addRule(RelativeLayout.ABOVE, nextButton.getId());
        chart.setLayoutParams(lpChart);
        
        layout.addView(chart,0);
        
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
    
    private void updateChart() {
    	if(initialized) {
	    	Series series = null;
	    	if(selectedOperation != null & selectedPeriodType != null) {
	    		switch(this.selectedPeriodType) {
			    	case REALTIME:
			        	series = createRealtimeChart(chart);
			    		break;
			    	case DAILY:
			    		series = createDailyChart(chart);
			    		break;
			    	case WEEKLY:
			    		series = createWeeklyChart(chart);
			    		break;
			    	case MONTHLY:
			    		series = createMonthlyChart(chart);
			    		break;
		    	}
	    		loadData(series,selectedPeriodType, selectedOperation);
	    	}
    	}
    }
    
	/** Called when the activity is first created. *//*
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.zwave_meter);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);   // Set screen on landscape
	    
		settings = getBaseContext().getSharedPreferences("USER", Context.MODE_PRIVATE);

		/* Set spinner values *//*

		operationSpinner = (Spinner) findViewById(R.id.operation);
		ArrayAdapter operationsArrayAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, operations);
		operationSpinner.setAdapter(operationsArrayAdapter);

		periodSpinner = (Spinner) findViewById(R.id.period);
		ArrayAdapter periodArryaAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, periodTypes);
		periodSpinner.setAdapter(periodArryaAdapter);

		/* Number of points spinner *//*
		numberOfPointsSpinner = (Spinner) findViewById(R.id.number_of_points);

		/* Instantiate Graph *//*
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

		operationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				// Get selected values
				SpinnerKeyValue<ZWaveMeterOperations, String> selectedItem = (SpinnerKeyValue<ZWaveMeterOperations, String>) parent.getItemAtPosition(pos);
				selectedOperation = selectedItem.getKey();
				updateChart();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		periodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

				// Get selected values
				SpinnerKeyValue<ZWaveMeterPeriodTypes, String> selectedItem = (SpinnerKeyValue<ZWaveMeterPeriodTypes, String>) parent.getItemAtPosition(pos);
				selectedPeriodType = selectedItem.getKey();

				// Update value list of amount spinner
				ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(me, android.R.layout.simple_spinner_dropdown_item, numberOfPointsMap.get(selectedPeriodType));
				numberOfPointsSpinner.setAdapter(spinnerArrayAdapter);
				numberOfPoints = numberOfPointsMap.get(selectedPeriodType).get(0).getKey();

				if (selectedPeriodType == ZWaveMeterPeriodTypes.REALTIME) {
					operationSpinner.setVisibility(View.GONE);
				} else {
					operationSpinner.setVisibility(View.VISIBLE);
				}

				updateChart();
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		numberOfPointsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SpinnerKeyValue keyValue = (SpinnerKeyValue) parent.getItemAtPosition(position);
				numberOfPoints = (Integer) keyValue.getKey();
				updateChart();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		initialized = true;
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
						this.selectedPeriodType = ZWaveMeterPeriodTypes.fromInt(Integer.parseInt(detailLineSplit[1]));
					}else if(detailLineSplit[0].equals("OPERATION")) {
						this.selectedOperation = ZWaveMeterOperations.fromInt(Integer.parseInt(detailLineSplit[1]));
					}/*else if(detailLineSplit[0].equals("NUMBER_OF_POINTS")) {
			        	this.numberOfPoints = Integer.parseInt(detailLineSplit[1]);
					}*//*
				}
			}
		
			// Update period type spinner
			Integer result = findInArray(periodTypes, selectedPeriodType);
			if(result != null) {
				periodSpinner.setSelection(result);
			}
			// Update operations spinner
			result = findInArray(operations, selectedOperation);
			if(result != null) {
				operationSpinner.setSelection(result);
			}
			// Update number of points spinner - will be overwritten by default selection of period spinner
	    	result = findInArray(numberOfPointsMap.get(selectedPeriodType), numberOfPoints);
			if(result != null) {
				ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(me,android.R.layout.simple_spinner_dropdown_item, numberOfPointsMap.get(selectedPeriodType));
	        	numberOfPointsSpinner.setAdapter(spinnerArrayAdapter);
				numberOfPointsSpinner.setSelection(result);
			}
			
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
        Series series;
        ZWaveMeterPeriodTypes periodType;
        ZWaveMeterOperations operation;
        
        SensorDetailTaskParams(Series series, ZWaveMeterPeriodTypes periodType, ZWaveMeterOperations operation) {
            this.series = series;
            this.periodType = periodType;
            this.operation = operation;
        }
    }
    private class SensorDetailLoader extends AsyncTask<SensorDetailTaskParams,Void,Void> {
    	
        final ProgressBar progress = (ProgressBar) findViewById(R.id.progress);
        ArrayList<ChartData> chartData = null;
        
        SensorDetailTaskParams taskParameters = null;
        ValueHistoryMessage sensorHistory;
        
        @Override
        protected void onPreExecute(){
        	progressBar.setVisibility(View.VISIBLE);
        }
        
		@Override
		protected Void doInBackground(SensorDetailTaskParams...series) {
			
			taskParameters = series[0];
			
			sensorHistory = SensorBinding.getHistoricValues(sensorBinding.getControlIdentifier(),offset,numberOfPoints,taskParameters.periodType, taskParameters.operation);
			// Add values in Graph
			try {

				chartData = new ArrayList<ChartData>();
				
				switch(taskParameters.periodType) {
					case DAILY:
						SimpleDateFormat dailyDateFormat = new SimpleDateFormat("E d/M");
						for(int i=0;i<sensorHistory.sensorValues.value.size();i++) {
							DateTime dateTime = Util.convertTimestampToDateTime(sensorHistory.sensorValues.time.get(i));
							String label = dateTime.toString(dailyDateFormat);
							chartData.add(new ChartData(dateTime,sensorHistory.sensorValues.value.get(i), dateTime.toString(dailyDateFormat)));
						}
					break;
					case WEEKLY:
						SimpleDateFormat weeklyDateFormat = new SimpleDateFormat("d/M");
						for(int i=0;i<sensorHistory.sensorValues.value.size();i++) {
							DateTime dateTime = Util.convertTimestampToDateTime(sensorHistory.sensorValues.time.get(i));
							String label = dateTime.toString(weeklyDateFormat);
							chartData.add(new ChartData(dateTime,sensorHistory.sensorValues.value.get(i), dateTime.toString(weeklyDateFormat)));
						}
					break;
					case MONTHLY:
						SimpleDateFormat monthlyDateFormat = new SimpleDateFormat("MMM yy");
						for(int i=0;i<sensorHistory.sensorValues.value.size();i++) {
							DateTime dateTime = Util.convertTimestampToDateTime(sensorHistory.sensorValues.time.get(i));
							String label = dateTime.toString(monthlyDateFormat);
							chartData.add(new ChartData(dateTime,sensorHistory.sensorValues.value.get(i), dateTime.toString(monthlyDateFormat)));
						}
					break;
					case REALTIME:
						for(int i=0;i<sensorHistory.sensorValues.value.size();i++) {
							DateTime dateTime = Util.convertTimestampToDateTime(sensorHistory.sensorValues.time.get(i));
							chartData.add(new ChartData(dateTime,sensorHistory.sensorValues.value.get(i), null));
						}
					break;
				}
				
				/*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    	for(ChartData point : chartData) {
                    		taskParameters.series.add(point.time,point.value,point.label);
                    	}
                    	
                    	chart.getAxes().getLeft().setAutomatic(true);
                    	switch(taskParameters.periodType) {
	    					case DAILY:
	    					case WEEKLY:
	    					case MONTHLY:
	            				chart.getAxes().getBottom().setAutomatic(true);
	    					break;
	    					case REALTIME:
	    						chart.getAxes().getLeft().setMinMax(taskParameters.series.getMinYValue() - 1, taskParameters.series.getMaxYValue()+1);
	    					break;
                    	}
                    	
                    	title.setText(sensorHistory.title);
                    }
                });*//*
			} catch (Exception e) {
				e.printStackTrace();
			}
	        
			return null;
		}
    
        @Override
        protected void onPostExecute(Void result){
        	progressBar.setVisibility(View.INVISIBLE);
        	if(chartData != null) {
	        	for(ChartData point : chartData) {
	        		taskParameters.series.add(point.time,point.value,point.label);
	        	}
	        	
	        	chart.getAxes().getLeft().setAutomatic(true);
	        	switch(taskParameters.periodType) {
					case DAILY:
					case WEEKLY:
					case MONTHLY:
	    				chart.getAxes().getBottom().setAutomatic(true);
					break;
					case REALTIME:
						chart.getAxes().getLeft().setMinMax(taskParameters.series.getMinYValue() - 1, taskParameters.series.getMaxYValue()+1);
					break;
	        	}
	        	
	        	title.setText(sensorHistory.title);
        	}
        }
    }
    
    private class ChartData {
    	public ChartData(DateTime time,double value,String label) {
    		this.time = time;
    		this.label = label;
    		this.value = value;
    	}
    	DateTime time;
    	String label;
    	double value;
    }
	*/
}
