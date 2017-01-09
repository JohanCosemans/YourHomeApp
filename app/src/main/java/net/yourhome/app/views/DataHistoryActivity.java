/*-
 * Copyright (c) 2016 Coteq, Johan Cosemans
 * All rights reserved.
 *
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY COTEQ AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE FOUNDATION OR CONTRIBUTORS
 * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
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
import net.yourhome.app.R;
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

/**
 * Meter chart with options for accumulated/delta and realtime/daily/monthly
 */
public class DataHistoryActivity extends Activity {

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
	private android.widget.TextView title;
	private RelativeLayout chartView;

	private Map<DataHistoryPeriodTypes, List<SpinnerKeyValue<Integer, String>>> numberOfPointsMap;
	private List<SpinnerKeyValue<DataHistoryPeriodTypes, String>> periodTypes;
	private List<SpinnerKeyValue<DataHistoryOperations, String>> operations;

	public DataHistoryActivity() {

		this.periodTypes = new ArrayList<SpinnerKeyValue<DataHistoryPeriodTypes, String>>();
		this.periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes, String>(DataHistoryPeriodTypes.REALTIME, "Realtime"));
		this.periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes, String>(DataHistoryPeriodTypes.DAILY, "Daily"));
		this.periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes, String>(DataHistoryPeriodTypes.WEEKLY, "Weekly"));
		this.periodTypes.add(new SpinnerKeyValue<DataHistoryPeriodTypes, String>(DataHistoryPeriodTypes.MONTHLY, "Monthly"));

		this.operations = new ArrayList<SpinnerKeyValue<DataHistoryOperations, String>>();
		this.operations.add(new SpinnerKeyValue<DataHistoryOperations, String>(DataHistoryOperations.AVERAGE, "Average"));
		this.operations.add(new SpinnerKeyValue<DataHistoryOperations, String>(DataHistoryOperations.DELTA, "Max - Min"));
		this.operations.add(new SpinnerKeyValue<DataHistoryOperations, String>(DataHistoryOperations.MIN, "Minimum"));
		this.operations.add(new SpinnerKeyValue<DataHistoryOperations, String>(DataHistoryOperations.MAX, "Maximum"));

		this.numberOfPointsMap = new HashMap<DataHistoryPeriodTypes, List<SpinnerKeyValue<Integer, String>>>();

		List<SpinnerKeyValue<Integer, String>> realtimePoints = new ArrayList<SpinnerKeyValue<Integer, String>>();
		realtimePoints.add(new SpinnerKeyValue<Integer, String>(100, "100 values"));
		realtimePoints.add(new SpinnerKeyValue<Integer, String>(200, "200 values"));
		realtimePoints.add(new SpinnerKeyValue<Integer, String>(300, "300 values"));
		realtimePoints.add(new SpinnerKeyValue<Integer, String>(500, "500 values"));
		realtimePoints.add(new SpinnerKeyValue<Integer, String>(1000, "1000 values"));
		this.numberOfPointsMap.put(DataHistoryPeriodTypes.REALTIME, realtimePoints);

		List<SpinnerKeyValue<Integer, String>> dailyPoints = new ArrayList<SpinnerKeyValue<Integer, String>>();
		dailyPoints.add(new SpinnerKeyValue<Integer, String>(7, "7 days"));
		dailyPoints.add(new SpinnerKeyValue<Integer, String>(14, "14 days"));
		dailyPoints.add(new SpinnerKeyValue<Integer, String>(30, "30 days"));
		dailyPoints.add(new SpinnerKeyValue<Integer, String>(60, "60 days"));
		dailyPoints.add(new SpinnerKeyValue<Integer, String>(120, "120 days"));
		dailyPoints.add(new SpinnerKeyValue<Integer, String>(365, "365 days"));
		this.numberOfPointsMap.put(DataHistoryPeriodTypes.DAILY, dailyPoints);

		List<SpinnerKeyValue<Integer, String>> weeklyPoints = new ArrayList<SpinnerKeyValue<Integer, String>>();
		weeklyPoints.add(new SpinnerKeyValue<Integer, String>(4, "4 weeks"));
		weeklyPoints.add(new SpinnerKeyValue<Integer, String>(8, "8 weeks"));
		weeklyPoints.add(new SpinnerKeyValue<Integer, String>(52, "52 weeks"));
		weeklyPoints.add(new SpinnerKeyValue<Integer, String>(104, "104 weeks"));
		this.numberOfPointsMap.put(DataHistoryPeriodTypes.WEEKLY, weeklyPoints);

		List<SpinnerKeyValue<Integer, String>> monthlyPoints = new ArrayList<SpinnerKeyValue<Integer, String>>();
		monthlyPoints.add(new SpinnerKeyValue<Integer, String>(12, "12 months"));
		monthlyPoints.add(new SpinnerKeyValue<Integer, String>(24, "24 months"));
		monthlyPoints.add(new SpinnerKeyValue<Integer, String>(36, "36 months"));
		this.numberOfPointsMap.put(DataHistoryPeriodTypes.MONTHLY, monthlyPoints);

	}

	private void loadData(String placeHolder, DataHistoryPeriodTypes timePeriod, DataHistoryOperations operation) {
		if (timePeriod != null && operation != null && this.numberOfPoints > 0) {
			SensorDetailLoader loader = new SensorDetailLoader();
			loader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new SensorDetailTaskParams(placeHolder, timePeriod, operation));
		}
	}

	private LineChartView createLineChart() {
		LineChartView lineChart = new LineChartView(getBaseContext());
		lineChart.setInteractive(true);
		lineChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
		float density = getResources().getDisplayMetrics().density;
		int paddingLeft = ChartUtils.dp2px(density, 12 + 4);// for default
															// settings use
															// ChartUtils.dp2px(density,
															// 6 + 4)
		int padding = ChartUtils.dp2px(density, 6 + 4);// for default settings
														// use
														// ChartUtils.dp2px(density,
														// 6 + 4)
		lineChart.setPadding(paddingLeft, padding, padding, 0);
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
		if (labelCalendar.get(Calendar.DAY_OF_YEAR) == this.today.get(Calendar.DAY_OF_YEAR) && labelCalendar.get(Calendar.YEAR) == this.today.get(Calendar.YEAR)) {
			// If the record is on the same day, only show hours
			formattedLabel = this.lineTimeFormater.format(labelDate);
		} else {
			formattedLabel = this.lineDateFormater.format(labelDate);
		}
		return formattedLabel;
	}

	private LineChartData generateLineData(HistoryValues historyData) {
		// Values
		SimpleDateFormat timeFormatter = this.lineDateFormater;
		List<PointValue> values = new ArrayList<PointValue>();
		final Calendar firstDate = Calendar.getInstance();
		if (historyData.time.size() > 0) {
			firstDate.setTime(new Date(historyData.time.get(historyData.time.size() - 1) * 1000L));
		}
		for (int i = 0; i < historyData.value.size(); i++) {
			int xIndex = (int) (historyData.time.get(i) - firstDate.getTimeInMillis() / 1000L);
			values.add(new PointValue(xIndex, historyData.value.get(i).floatValue()));
		}

		// Line graph
		Line line = new Line(values).setCubic(false).setFilled(true).setStrokeWidth(1).setHasPoints(false).setPointColor(Color.TRANSPARENT).setPointRadius(0).setHasLabels(false).setColor(Color.parseColor("#0A7EA8"));

		List<Line> lines = new ArrayList<Line>();
		lines.add(line);
		LineChartData data = new LineChartData();
		data.setLines(lines);
		Axis axisX = new Axis().setHasLines(true);
		Axis axisY = new Axis().setHasLines(true);
		if (historyData.valueUnit != null) {
			axisY.setName(historyData.valueUnit);
		}
		axisX.setMaxLabelChars(timeFormatter.toPattern().length() + 1);
		axisX.setFormatter(new AxisValueFormatter() {

			@Override
			public int formatValueForManualAxis(char[] formattedValue, AxisValue axisValue) {
				// TODO Auto-generated method stub
				return 0;
			}

			@Override
			public int formatValueForAutoGeneratedAxis(char[] formattedValue, float value, int autoDecimalDigits) {
				Date labelDate = new Date((long) (firstDate.getTimeInMillis() + value * 1000L));
				char[] label = DataHistoryActivity.this.getDateLabel(labelDate).toCharArray();
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
		SimpleDateFormat timeFormatter = this.lineDateFormater;
		List<Column> columns = new ArrayList<Column>();
		List<AxisValue> axisValuesForX = new ArrayList<AxisValue>();

		String unit = historyData.valueUnit == null ? "" : " " + historyData.valueUnit;

		for (int i = historyData.value.size() - 1; i >= 0; i--) {
			// 1 column with 1 subcolumn per value
			SubcolumnValue value = new SubcolumnValue(historyData.value.get(i).floatValue(), Color.parseColor("#770A7EA8"));
			List<SubcolumnValue> values = new ArrayList<SubcolumnValue>();
			value.setLabel(String.format("%.2f", historyData.value.get(i)) + unit);
			values.add(value);

			AxisValue xValue = new AxisValue(historyData.value.size() - 1 - i);
			xValue.setLabel(labelFormat.format(new Date(historyData.time.get(i) * 1000L)));
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
		if (unit != null && !unit.equals("")) {
			axisY.setName(unit);
		}
		axisX.setMaxLabelChars(timeFormatter.toPattern().length() + 1);
		axisX.setTextColor(Color.BLACK);
		axisY.setTextColor(Color.BLACK);
		data.setAxisXBottom(axisX);
		data.setAxisYLeft(axisY);

		return data;
	}

	private ColumnChartView createBarChart() {
		ColumnChartView columnChart = new ColumnChartView(getBaseContext());
		columnChart.setInteractive(true);
		columnChart.setZoomType(ZoomType.HORIZONTAL_AND_VERTICAL);
		float density = getResources().getDisplayMetrics().density;
		int paddingLeft = ChartUtils.dp2px(density, 12 + 4);// for default
															// settings use
															// ChartUtils.dp2px(density,
															// 6 + 4)
		int padding = ChartUtils.dp2px(density, 6 + 4);// for default settings
														// use
														// ChartUtils.dp2px(density,
														// 6 + 4)
		columnChart.setPadding(paddingLeft, padding, padding, 0);
		return columnChart;
	}

	private void updateChart(ValueHistoryMessage values) {
		this.chartView.removeAllViews();
		this.title.setText(values.title);
		switch (this.selectedPeriodType) {
		case DAILY:
			ColumnChartView dailyColumnChart = this.createBarChart();
			dailyColumnChart.setColumnChartData(this.generateBarData(values.sensorValues, this.dailyDateFormat));
			this.chartView.addView(dailyColumnChart);
			break;
		case WEEKLY:
			ColumnChartView weeklyColumnChart = this.createBarChart();
			weeklyColumnChart.setColumnChartData(this.generateBarData(values.sensorValues, this.weeklyDateFormat));
			this.chartView.addView(weeklyColumnChart);
			break;
		case MONTHLY:
			ColumnChartView monthlyColumnChart = this.createBarChart();
			monthlyColumnChart.setColumnChartData(this.generateBarData(values.sensorValues, this.monthlyDateFormat));
			this.chartView.addView(monthlyColumnChart);
			break;
		case REALTIME:
			LineChartView lineChart = this.createLineChart();
			lineChart.setLineChartData(this.generateLineData(values.sensorValues));
			this.chartView.addView(lineChart);
			break;
		}
	}

	public void initView() {

		/* ProgressBar */
		this.progressBar = (ProgressBar) findViewById(R.id.progress);
		//this.progressBar.setId(3);
		this.progressBar.setVisibility(View.INVISIBLE);

		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		/* Button previous */
		this.previousButton = (ImageButton) findViewById(R.id.graph_left);
		this.previousButton.setBackground(Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_arrow_left, 20));
		this.previousButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				DataHistoryActivity.this.offset += DataHistoryActivity.this.numberOfPoints / 2;
				DataHistoryActivity.this.loadData(null, DataHistoryActivity.this.selectedPeriodType, DataHistoryActivity.this.selectedOperation);
			}
		});

		/* Button next */
		this.nextButton = (ImageButton) findViewById(R.id.graph_right);
		this.nextButton.setBackground(Configuration.getInstance().getAppIconDrawable(getBaseContext(), R.string.icon_arrow_right, 20));
		this.nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (DataHistoryActivity.this.offset >= DataHistoryActivity.this.numberOfPoints / 2) {
					DataHistoryActivity.this.offset -= DataHistoryActivity.this.numberOfPoints / 2;
					DataHistoryActivity.this.loadData(null, DataHistoryActivity.this.selectedPeriodType, DataHistoryActivity.this.selectedOperation);
				}
			}
		});

		/* Graph Title */
		this.title = (android.widget.TextView) findViewById(R.id.graph_title);

		/* Window Layout */
		LinearLayout.LayoutParams chartLayoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		this.chartView = (RelativeLayout) findViewById(R.id.chart);
		FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams((int) (size.x * 0.9), (int) (size.y * 0.9));
		LinearLayout mainLayout = (LinearLayout) findViewById(R.id.graph_main_layout);
		mainLayout.setLayoutParams(layoutParams);

		// Attach close to action
		ImageButton closeButton = (ImageButton) this.findViewById(R.id.ip_camera_close);
		closeButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
	}

	/** Called when the activity is first created. */
	@SuppressLint("NewApi")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Set
																			// screen
																			// on
																			// landscape
		setContentView(R.layout.zwave_meter);

		this.settings = getBaseContext().getSharedPreferences("USER", Context.MODE_PRIVATE);

		/* Set spinner values */

		this.operationSpinner = (Spinner) findViewById(R.id.operation);
		ArrayAdapter<?> operationsArrayAdapter = new ArrayAdapter(this.me, android.R.layout.simple_spinner_dropdown_item, this.operations);
		this.operationSpinner.setAdapter(operationsArrayAdapter);

		this.periodSpinner = (Spinner) findViewById(R.id.period);
		ArrayAdapter<?> periodArryaAdapter = new ArrayAdapter(this.me, android.R.layout.simple_spinner_dropdown_item, this.periodTypes);
		this.periodSpinner.setAdapter(periodArryaAdapter);

		/* Number of points spinner */
		this.numberOfPointsSpinner = (Spinner) findViewById(R.id.number_of_points);

		/* Instantiate Graph */
		this.layout = (RelativeLayout) findViewById(R.id.chart);

		// Get value that we are reporting on and get the last used report
		Intent intent = this.getIntent();
		Bundle extras = intent.getExtras();
		String controllerIdentifier = extras.getString("controllerIdentifier");
		String nodeIdentifier = extras.getString("nodeIdentifier");
		String valueIdentifier = extras.getString("valueIdentifier");
		this.identifiers = new ControlIdentifiers();
		this.identifiers.setControllerIdentifier(ControllerTypes.convert(controllerIdentifier));
		this.identifiers.setNodeIdentifier(nodeIdentifier);
		this.identifiers.setValueIdentifier(valueIdentifier);
		List<AbstractBinding> bindingList = BindingController.getInstance().getBindingsFor(this.identifiers);
		this.sensorBinding = bindingList.get(0);

		this.readSettings();

		// Init default view and load data
		this.initView();

		this.periodSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				DataHistoryActivity.this.offset = 0;
				// if(initializedSpinners++>=3) {
				// Get selected values
				SpinnerKeyValue<DataHistoryPeriodTypes, String> selectedItem = (SpinnerKeyValue<DataHistoryPeriodTypes, String>) parent.getItemAtPosition(pos);
				DataHistoryActivity.this.selectedPeriodType = selectedItem.getKey();

				// Update value list of amount spinner
				ArrayAdapter spinnerArrayAdapter = new ArrayAdapter(DataHistoryActivity.this.me, android.R.layout.simple_spinner_dropdown_item, DataHistoryActivity.this.numberOfPointsMap.get(DataHistoryActivity.this.selectedPeriodType));
				DataHistoryActivity.this.numberOfPointsSpinner.setAdapter(spinnerArrayAdapter);
				// numberOfPoints =
				// numberOfPointsMap.get(selectedPeriodType).get(0).getKey();

				if (DataHistoryActivity.this.selectedPeriodType == DataHistoryPeriodTypes.REALTIME) {
					DataHistoryActivity.this.operationSpinner.setVisibility(View.GONE);
				} else {
					DataHistoryActivity.this.operationSpinner.setVisibility(View.VISIBLE);
				}

				// loadData(null,selectedPeriodType, selectedOperation);

			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		this.numberOfPointsSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				DataHistoryActivity.this.offset = 0;
				// if(initializedSpinners++>=3) {
				SpinnerKeyValue keyValue = (SpinnerKeyValue) parent.getItemAtPosition(position);
				DataHistoryActivity.this.numberOfPoints = (Integer) keyValue.getKey();
				DataHistoryActivity.this.loadData(null, DataHistoryActivity.this.selectedPeriodType, DataHistoryActivity.this.selectedOperation);
				// }
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		this.operationSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
				DataHistoryActivity.this.offset = 0;
				// if(initializedSpinners++>=3) {
				// Get selected values
				SpinnerKeyValue<DataHistoryOperations, String> selectedItem = (SpinnerKeyValue<DataHistoryOperations, String>) parent.getItemAtPosition(pos);
				DataHistoryActivity.this.selectedOperation = selectedItem.getKey();

				// destroy bar chart?
				DataHistoryActivity.this.createLineChart();
				DataHistoryActivity.this.loadData(null, DataHistoryActivity.this.selectedPeriodType, DataHistoryActivity.this.selectedOperation);
				// }
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		this.writeSettings();

	}

	private void readSettings() {
		Set<String> graphDetails = this.settings.getStringSet("GRAPH_" + this.identifiers.getKey(), null);
		if (graphDetails != null) {
			for (String detailLine : graphDetails) {
				String[] detailLineSplit = detailLine.split("=");
				if (detailLineSplit.length == 2) {
					if (detailLineSplit[0].equals("PERIOD_TYPE")) {
						// this.selectedPeriodType =
						// DataHistoryPeriodTypes.fromInt(Integer.parseInt(detailLineSplit[1]));
						// Update period type spinner
						Integer result = this.findInArray(this.periodTypes, DataHistoryPeriodTypes.fromInt(Integer.parseInt(detailLineSplit[1])));
						if (result != null) {
							this.periodSpinner.setSelection(result);
						}
					} else if (detailLineSplit[0].equals("OPERATION")) {
						// Update operations spinner
						// this.selectedOperation =
						// DataHistoryOperations.fromInt(Integer.parseInt(detailLineSplit[1]));
						Integer result = this.findInArray(this.operations, DataHistoryOperations.fromInt(Integer.parseInt(detailLineSplit[1])));
						if (result != null) {
							this.operationSpinner.setSelection(result);
						}
					}
				}
			}

			// Update number of points spinner - will be overwritten by default
			// selection of period spinner
			/*
			 * result = findInArray(numberOfPointsMap.get(selectedPeriodType),
			 * numberOfPoints); if(result != null) { ArrayAdapter
			 * spinnerArrayAdapter = new
			 * ArrayAdapter(me,android.R.layout.simple_spinner_dropdown_item,
			 * numberOfPointsMap.get(selectedPeriodType));
			 * numberOfPointsSpinner.setAdapter(spinnerArrayAdapter);
			 * numberOfPointsSpinner.setSelection(result); }
			 */

		}

	}

	private Integer findInArray(List<?> spinnerKeyValue, Object lookupValue) {
		int i = 0;
		boolean found = false;
		while (!found && i < spinnerKeyValue.size()) {
			SpinnerKeyValue<?, ?> castedSpinnerKeyValue = (SpinnerKeyValue<?, ?>) spinnerKeyValue.get(i);
			if (castedSpinnerKeyValue.getKey().equals(lookupValue)) {
				found = true;
			} else {
				i++;
			}
		}
		return found ? i : null;
	}

	private void writeSettings() {
		Editor edit = this.settings.edit();
		Set<String> graphDetails = new HashSet<String>();
		graphDetails.add("PERIOD_TYPE=" + this.selectedPeriodType.convert());
		graphDetails.add("OPERATION=" + this.selectedOperation.convert());
		// graphDetails.add("NUMBER_OF_POINTS="+this.numberOfPoints);
		edit.putStringSet("GRAPH_" + this.identifiers.getKey(), graphDetails);
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

	private class SensorDetailLoader extends AsyncTask<SensorDetailTaskParams, Void, ValueHistoryMessage> {

		final ProgressBar progress;
		SensorDetailTaskParams taskParameters = null;

		public SensorDetailLoader() {
			this.progress = (ProgressBar) findViewById(R.id.progress);
		}

		@Override
		protected void onPreExecute() {
			DataHistoryActivity.this.progressBar.setVisibility(View.VISIBLE);
		}

		@Override
		protected ValueHistoryMessage doInBackground(SensorDetailTaskParams... series) {

			this.taskParameters = series[0];
			return SensorBinding.getHistoricValues(DataHistoryActivity.this.sensorBinding.getControlIdentifier(), DataHistoryActivity.this.offset, DataHistoryActivity.this.numberOfPoints, this.taskParameters.periodType, this.taskParameters.operation);

		}

		@Override
		protected void onPostExecute(ValueHistoryMessage result) {
			if (result != null) {
				DataHistoryActivity.this.updateChart(result);
			}
			DataHistoryActivity.this.progressBar.setVisibility(View.GONE);

		}
	}

}
