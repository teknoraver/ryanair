package net.teknoraver.ryanair;

import java.util.Date;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;

public class Tab extends TabActivity {
	private String from, to, title;
	private Date date1, date2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab);

		from = getIntent().getStringExtra(Results.FROM);
		to = getIntent().getStringExtra(Results.TO);
		date1 = (Date)getIntent().getSerializableExtra(Results.DATE);
		date2 = (Date)getIntent().getSerializableExtra(Results.DATE2);
		title = getIntent().getStringExtra(Results.TITLE);

		setTitle(title);
		Resources res = getResources();
		TabHost tabHost = getTabHost();
		TabHost.TabSpec spec;

		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("outward")
			.setIndicator(getText(R.string.outward), res.getDrawable(R.drawable.outward))
			.setContent(new Intent(this, Results.class)
				.putExtra(Results.FROM, from)
				.putExtra(Results.TO, to)
				.putExtra(Results.DATE, date1));
		tabHost.addTab(spec);
	
		// Initialize a TabSpec for each tab and add it to the TabHost
		spec = tabHost.newTabSpec("return")
			.setIndicator(getText(R.string.ret), res.getDrawable(R.drawable.returno))
			.setContent(new Intent(this, Results.class)
				.putExtra(Results.FROM, to)
				.putExtra(Results.TO, from)
				.putExtra(Results.DATE, date2));
		tabHost.addTab(spec);
	}
}
