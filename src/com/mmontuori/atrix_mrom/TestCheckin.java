package com.mmontuori.atrix_mrom;

import android.os.Bundle;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnCancelListener;
import android.content.res.Configuration;
import android.widget.Button;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class TestCheckin extends Activity {
	class Temp {
		public boolean doCancel = false;
		public String version = null;
		public Exception ex = null;
		public ProgressDialog pd;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_checkin);
		final Button button = (Button)findViewById(R.id.btn_check);
		button.setOnClickListener(new Button.OnClickListener() {
			public void onClick(View v) {
				Log.i(Utils.TAG, "clicked");
				doCheck();
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_test_checkin, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_settings: {
					startActivity(new Intent(this, Settings.class));
				}
				break;
			case R.id.menu_about: {
					String version = Utils.getMyVersion(this);
					if (version == null || version.equals("")) {
						version = getText(R.string.version_unknown).toString();
					}
					Utils.showDialog(this,
						getText(R.string.about_title).toString(),
						String.format(getText(R.string.about_text).toString(), Utils.htmlEsc(version))
					);
				}
				break;
			case R.id.menu_quit: {
					finish();
				}
				break;
			default:
				break;
		}
		return false;
	}

	public void onConfigurationChanged(Configuration cfg) {
		super.onConfigurationChanged(cfg);
	}

	protected void showResponse(Temp data) {
		if (data.doCancel) {
			return;
		}
		if (data.ex != null) {
			data.ex.printStackTrace();
			String http = Utils.getData();
			if (http != null) {
				System.err.println("received HTTP data: [" + http + "]");
			}
			String err = null;
			String cname = data.ex.getClass().getName();
			if (Utils.IncompatibleDeviceException.class.getName().equals(cname)) {
				err = getText(R.string.err_device).toString();
			} else if (Utils.NoConnectionException.class.getName().equals(cname)) {
				err = getText(R.string.err_connection).toString();
			} else {
				err = getText(R.string.err_checking).toString();
			}
			Utils.showDialog(this, getText(R.string.err_title).toString(), err);
		} else {
			if (data.version == null) {
				Utils.showDialog(this,
					getText(R.string.main_title).toString(),
					getText(R.string.msg_latest_version).toString()
				);
			} else {
				Utils.showDialog(this,
					getText(R.string.main_title).toString(),
					String.format(getText(R.string.msg_new_version).toString(), Utils.htmlEsc(data.version))
				);
			}
		}
	}

	protected void doCheck() {
		final Temp data = new Temp();
		data.pd = ProgressDialog.show(this, getText(R.string.main_title), getText(R.string.msg_checking), true, false);
		data.pd.setCancelable(false);
		data.pd.setCanceledOnTouchOutside(false);
		data.pd.setOnCancelListener(new OnCancelListener() {
			public void onCancel(DialogInterface arg0) {
				data.doCancel = true;
			}
		});
		try {
			Thread th = new Thread(new Runnable() {
				public void run() {
					try {
						data.version = Utils.checkForNewVersion(TestCheckin.this.getBaseContext(), false);
					}
					catch (Exception e) {
						data.ex = e;
					}
					data.pd.dismiss();
					runOnUiThread(new Runnable() {
						public void run() {
							showResponse(data);
						}
					});
				}
			});
			th.setDaemon(false);
			th.start();
		}
		catch (Exception e) {
			data.ex = e;
			showResponse(data);
		}
	}
}
