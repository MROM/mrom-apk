package com.mmontuori.atrix_mrom;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Enumeration;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Utils {
	final static public String TAG = "MROM";
	final static public String BASEURL = "http://www.montuori.net/mrom/checkin.php";

	@SuppressWarnings("serial")
	static public class IncompatibleDeviceException extends Exception { }

	@SuppressWarnings("serial")
	static public class NoConnectionException extends Exception { }

	static public String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.e("Checkin", ex.toString());
		}
		return null;
	}

	static public String join(String separator, String... data) {
		String ret = "";
		for (int i = 0; i < data.length; i++) {
			if (i > 0) {
				ret += separator;
			}
			ret += data[i];
		}
		return ret;
	}

	static public String getBuildData(String separator) {
		String[] data = {
			Build.DEVICE,
			Build.DISPLAY,
			Build.VERSION.INCREMENTAL,
			Build.ID,
			Build.MODEL,
			Build.VERSION.RELEASE,
			Build.VERSION.SDK,
		};
		return join(separator, data);
	}

	static public String getBuildData() {
		return getBuildData(":");
	}

	static public String getLocalBuild(String separator) {
		String[] data = {
			Build.DEVICE,
			Build.DISPLAY,
			Build.VERSION.INCREMENTAL,
		};
		return join(separator, data);
	}

	static public String getLocalBuild() {
		return getLocalBuild("-");
	}

	static public String getDeviceId(Context ctx) {
		final TelephonyManager tm = (TelephonyManager)ctx.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	static public String checkForNewVersion(final Context ctx, final boolean isService) throws Exception {
		//Thread.sleep(2000);
		String myIP = null;
		while (true) {
			myIP = getLocalIpAddress();
			if (myIP != null || !isService) {
				// return Service.START_FLAG_RETRY;
				break;
			}
			Thread.sleep(500);
		}
		if (myIP == null) {
			throw new NoConnectionException();
		}
		URL url = new URL(String.format("%s?ip=%s&build=%s&device_id=%s",
			BASEURL,
			URLEncoder.encode(myIP),
			URLEncoder.encode(getBuildData()),
			URLEncoder.encode(getDeviceId(ctx))
		));
		Log.i(TAG, "Checking in: " + url.toString());
		String data = "";
		try {
			URLConnection urlConnection = url.openConnection();
			BufferedInputStream in = new BufferedInputStream(urlConnection.getInputStream(), 2048);
			try {
				while (in.available() > 0) {
					byte bt[] = new byte[2048];
					in.read(bt);
					data += new String(bt);
				}
			} finally {
				in.close();
			}
		}
		catch (IOException ex) {
			throw new NoConnectionException();
		}

		JSONArray a = new JSONArray(data);
		String remote_version = null;
		boolean found = false;
		for (int i = 0; i < a.length(); i++) {
			JSONObject obj = a.getJSONObject(i);
			if (obj.has("name")) {
				String name = obj.getString("name");
				//Log.i(Utils.TAG, "name:" + name);
				if (name.equals("current_version")) {
					String tmpVersion = obj.getString("value");
					if (tmpVersion.startsWith(Build.DEVICE + "-")) {
						remote_version = tmpVersion;
						Log.i(Utils.TAG, "latest version available:" + remote_version);
						found = true;
					}
				}
			}
		}
		if (!found) {
			throw new IncompatibleDeviceException();
		}

		String buildToCheck = Utils.getLocalBuild();
		Log.i(Utils.TAG, String.format("local version: [%s]", buildToCheck));

		if (remote_version != null && !remote_version.equals(buildToCheck)) {
			Log.i(TAG, "new version is available!");
			return remote_version;
		}
		return null;
	}

	public static void showDialog(Activity act, String title, String text) {
		LayoutInflater li = (LayoutInflater)act.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = li.inflate(R.layout.dialog, (ViewGroup)act.findViewById(R.id.dialog_root));
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setView(v);
		builder.setCancelable(true);
		builder.setTitle((CharSequence)title);
		TextView content = (TextView)v.findViewById(R.id.dialog_text);
		content.setText(Html.fromHtml(text));
		content.setMovementMethod(LinkMovementMethod.getInstance());
		builder.setPositiveButton(act.getText(R.string.btn_ok), new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		builder.create().show();
	}
}
