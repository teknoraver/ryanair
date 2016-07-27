package net.teknoraver.ryanair;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

public class Results extends ListActivity implements OnClickListener, OnDateSetListener, Runnable {
	static final String details = "details";

	static final SimpleDateFormat dfmt = new SimpleDateFormat("dd/MM/yyyy");

	private String from, to;
	private Date date;
	private Button curdate;
	private ProgressDialog wait;
	private ImageButton prev;
	FlightResult res[];
	private FlightAdapter adapter;

	static final String FROM	= "from";
	static final String TO		= "to";
	static final String DATE	= "date";
	static final String DATE2	= "date2";
	static final String TITLE	= "title";

	private final Handler handler = new Handler();
	private Runnable updater = new Runnable() {
		@Override
		public void run() {
			wait.dismiss();
			adapter.clear();
			if(res == null) {
				new AlertDialog.Builder(Results.this)
					.setTitle(R.string.error)
					.setMessage(R.string.connerror)
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show();
			} else {
				for(FlightResult f : res)
					adapter.add(f);
//				adapter.notifyDataSetChanged();
			}
		}
	};

	private class FlightAdapter extends ArrayAdapter<FlightResult> {
		public FlightAdapter(ListActivity l) {
			super(l, R.layout.row);
		}

		@Override
		public View getView (int position, View convertView, ViewGroup parent) {
			if (convertView == null)
				convertView = getLayoutInflater().inflate(R.layout.row, null);
			FlightResult f = getItem(position);
			((TextView)convertView.findViewById(R.id.depart)).setText(f.depart);
			((TextView)convertView.findViewById(R.id.arrival)).setText(f.arrive);
			((TextView)convertView.findViewById(R.id.price)).setText(f.price);
			return convertView;
		}
	}

	@Override
	public void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.result);

		from = getIntent().getStringExtra(FROM);
		to = getIntent().getStringExtra(TO);
		date = (Date)getIntent().getSerializableExtra(DATE);
		setTitle(getIntent().getStringExtra(TITLE));

		curdate = (Button)findViewById(R.id.curdate);
		curdate.setOnClickListener(this);

		prev = (ImageButton)findViewById(R.id.prev);
		prev.setOnClickListener(this);

		ImageButton next = (ImageButton)findViewById(R.id.next);
		next.setOnClickListener(this);

		adapter = new FlightAdapter(this);
		setListAdapter(adapter);

		search();
	}

	private void search() {
		prev.setEnabled(date.compareTo(Select.now) > 0);
		curdate.setText(dfmt.format(date));
		wait = ProgressDialog.show(this, "Searching", "Looking for flight\nplease wait");
		new Thread(this).start();
	}

	@Override
	public void run() {
		res = null;
		try {
			res = Ryanair.get(new FlightRequest(from, to, dfmt.format(date)));
		} catch (Exception e) {
			e.printStackTrace();
		}

		handler.post(updater);
	}

	@Override
	public void onClick(View view) {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.setTime(date);

		switch(view.getId()) {
		case R.id.prev:
			calendar.add(Calendar.DAY_OF_MONTH, -1);
			break;
		case R.id.next:
			calendar.add(Calendar.DAY_OF_MONTH, 1);
			break;
		case R.id.curdate:
			new DatePickerDialog(this, this, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
		default:
			return;
		}
		date = calendar.getTime();
		search();
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
		Date setd = new GregorianCalendar(year, monthOfYear, dayOfMonth).getTime();
		if(setd.before(Select.now)) {
			new AlertDialog.Builder(this)
				.setTitle(R.string.app_name)
				.setMessage("The date " + Results.dfmt.format(setd) + " is in the past")
				.setIcon(android.R.drawable.ic_dialog_alert)
				.show();
			return;
		}
		date = setd;
		search();
	}
}
