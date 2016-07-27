package net.teknoraver.ryanair;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.TableRow;

public class Select extends Activity implements OnItemClickListener, OnClickListener, OnDateSetListener {
	private static final Pattern patt = Pattern.compile(".*\\(([A-Z]{3})\\)$");
	private AutoCompleteTextView from, to;
	private Button db1, db2;
	private Date d1, d2;
	static Date now; 
	private String codef, codet;
	private RadioButton roundradio;
	private boolean isRound;
	private Button settingDate;
	private TableRow roundgroup;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.select);

		GregorianCalendar justnow = new GregorianCalendar();
		now = d1 = d2 = new GregorianCalendar(justnow.get(Calendar.YEAR), justnow.get(Calendar.MONTH), justnow.get(Calendar.DAY_OF_MONTH)).getTime();

		from = (AutoCompleteTextView)findViewById(R.id.from);
		to = (AutoCompleteTextView)findViewById(R.id.to);

		ImageButton invert = (ImageButton)findViewById(R.id.invert);
		invert.setOnClickListener(this);

		db1 = (Button)findViewById(R.id.date1);
		db1.setText(Results.dfmt.format(d1));
		db1.setOnClickListener(this);

		db2 = (Button)findViewById(R.id.date2);
		db2.setText(Results.dfmt.format(d2));
		db2.setOnClickListener(this);

		RadioButton r1 = (RadioButton)findViewById(R.id.one);
		r1.setOnClickListener(this);

		roundradio = (RadioButton)findViewById(R.id.round);
		roundradio.setOnClickListener(this);

		roundgroup = (TableRow)findViewById(R.id.roundgroup);

//		from.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, Routes.names));
		from.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Routes.names));
		from.setOnItemClickListener(this);

		((Button)findViewById(R.id.search)).setOnClickListener(this);
	}

	@Override
	public void onResume() {
		super.onResume();

		isRound = roundradio.isChecked();
		if(isRound)
			roundgroup.setVisibility(View.VISIBLE);
		else
			roundgroup.setVisibility(View.GONE);
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()) {
		case R.id.search:
			search();
			break;
		case R.id.invert:
			invert();
			break;
		case R.id.date1:
		case R.id.date2:
			GregorianCalendar calendar = new GregorianCalendar();
			if(view.getId() == R.id.date1)
				calendar.setTime(d1);
			else
				calendar.setTime(d2);
			settingDate = (Button)view;
			new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
			break;
		case R.id.one:
			isRound = false;
			roundgroup.setVisibility(View.GONE);
			break;
		case R.id.round:
			isRound = true;
			roundgroup.setVisibility(View.VISIBLE);
			break;
		}
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Date date = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();

		if(date.before(now)) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage("The date " + Results.dfmt.format(date) + " is in the past")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			return;
		}

		if(settingDate == db1) {
			d1 = date;
			if(d1.after(d2)) {
				d2 = date;
				db2.setText(Results.dfmt.format(date));
			}
		} else
			d2 = date;
		settingDate.setText(Results.dfmt.format(date));
	}

	private void search() {
		if(isRound && d1.after(d2)) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.error)
				.setMessage(R.string.baddate)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			return;
		}

		String f = from.getText().toString();
		String t = to.getText().toString();

		boolean valid = false;
		// grep for a matching pattern
		Matcher matcher = patt.matcher(f);
		if(matcher.find() && Routes.names.indexOf(f) != -1) {
			codef = matcher.group(1);
			matcher = patt.matcher(t);
			if(matcher.find() && Routes.names.indexOf(t) != -1) {
				// get the destinations from the departure
				codet = matcher.group(1);
				List<String> dests = Routes.getDestinations(f);
				if(dests != null && dests.indexOf(t) != -1)
					valid = true;
			}
		}

		if(!valid) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.selectt)
				.setMessage(R.string.selectm)
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			return;
		}

		f = f.substring(0, f.length() - 6);
		t = t.substring(0, t.length() - 6);

		Intent intent;

		if(isRound)
			intent = new Intent(this, Tab.class).putExtra(Results.DATE2, d2);
		else
			intent = new Intent(this, Results.class);

		startActivity(intent
			.putExtra(Results.TITLE, f + " to " + t)
			.putExtra(Results.FROM, codef)
			.putExtra(Results.TO, codet)
			.putExtra(Results.DATE, d1));
	}

	private void invert() {
		String tmp = from.getText().toString();
		from.setText(to.getText().toString());

		List<String> destinations = Routes.getDestinations(from.getText().toString());

		if(destinations != null)
			to.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, destinations));
//			to.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, destinations));

		to.setText(tmp);
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		List<String> destinations = Routes.getDestinations(from.getText().toString());

		if(destinations != null)
			to.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, destinations));
//			to.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, destinations));
		else {
			new AlertDialog.Builder(this)
			.setTitle(R.string.app_name)
			.setMessage(R.string.noflight)
			.setIcon(android.R.drawable.ic_dialog_info)
			.show();
			from.setText("");
			to.setText("");
		}
	}
}
