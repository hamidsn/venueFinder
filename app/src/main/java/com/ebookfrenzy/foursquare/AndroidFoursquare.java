package com.ebookfrenzy.foursquare;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.ebookfrenzy.foursquare.fab.FloatingActionButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by hsedghinezhad on 14/07/2015.
 */
public class AndroidFoursquare extends Activity implements GooglePlayServicesClient.ConnectionCallbacks,
		GooglePlayServicesClient.OnConnectionFailedListener {
	final String CLIENT_ID = "ACAO2JPKM1MXHQJCK45IIFKRFR2ZVL0QASMCBCG5NPJQWF2G";
	final String CLIENT_SECRET = "YZCKUYJ1WHUV2QICBXUBEILZI1DMPUIDP5SHV043O04FKBHL";
	ArrayList<FoursquareVenue> venuesList;
	ListView myListView;
	List<String> name, distance, imageHref, address, phone;
	private String error;
	private Location myCurrentLocation;
	private LocationClient myLocationClient;
	// the foursquare client_id and the client_secret
	private ProgressDialog progressDialog;
	private String latitude = "", longtitude = "";
	private boolean isUserRequested = true;
	private Context context;

	public static String makeCall(String url) throws IOException {
		// passing url to the class to call api
		GetMainRequest example = new GetMainRequest();
		return example.doGetRequest(url).trim();
	}

	private static ArrayList<FoursquareVenue> parseFoursquare(final String response) {

		ArrayList<FoursquareVenue> temp = new ArrayList<FoursquareVenue>();
		try {

			// make an jsonObject in order to parse the response
			JSONObject jsonObject = new JSONObject(response);

			// make an jsonObject in order to parse the response
			if (jsonObject.has("response")) {
				if (jsonObject.getJSONObject("response").has("venues")) {
					JSONArray jsonArray = jsonObject.getJSONObject("response")
							.getJSONArray("venues");

					for (int i = 0; i < jsonArray.length(); i++) {
						FoursquareVenue poi = new FoursquareVenue();
						if (jsonArray.getJSONObject(i).has("name")) {
							poi.setName(jsonArray.getJSONObject(i).getString("name"));

							if (jsonArray.getJSONObject(i).has("contact")) {
								{
									if (jsonArray.getJSONObject(i).getJSONObject("contact")
											.has("phone")) {
										poi.setContact(jsonArray.getJSONObject(i)
												.getJSONObject("contact").getString("phone"));
									}
								}
							}

							if (jsonArray.getJSONObject(i).has("location")) {
								if (jsonArray.getJSONObject(i).getJSONObject("location")
										.has("address")) {
									if (jsonArray.getJSONObject(i).getJSONObject("location")
											.has("distance")) {
										poi.setDistance(jsonArray.getJSONObject(i)
												.getJSONObject("location").getInt("distance"));
									}
									if (jsonArray.getJSONObject(i).getJSONObject("location")
											.has("formattedAddress")) {
										poi.setFormattedAddress(jsonArray.getJSONObject(i)
												.getJSONObject("location")
												.getString("formattedAddress"));
									}
									if (jsonArray.getJSONObject(i).has("categories")) {
										if (jsonArray.getJSONObject(i).getJSONArray("categories")
												.length() > 0) {
											if (jsonArray.getJSONObject(i)
													.getJSONArray("categories").getJSONObject(0)
													.has("icon")) {
												poi.setCategory(jsonArray.getJSONObject(i)
														.getJSONArray("categories").getJSONObject(0)
														.getString("name"));
												poi.setImageHref(jsonArray.getJSONObject(i)
														.getJSONArray("categories").getJSONObject(0)
														.getJSONObject("icon").getString("prefix")
														+ "88" + jsonArray.getJSONObject(i)
														.getJSONArray("categories").getJSONObject(0)
														.getJSONObject("icon").getString("suffix"));
											}
										}
									}
									temp.add(poi);
								}
							}
						}
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ArrayList<FoursquareVenue>();
		}
		return temp;

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_android_foursquare);
		context = getApplicationContext();
		myListView = (ListView) findViewById(R.id.list);
		myLocationClient = new LocationClient(AndroidFoursquare.this, this, this);
		// init the floating button
		init();
		// array lists to pass to the custom adapter
		name = new ArrayList<String>();
		distance = new ArrayList<String>();
		imageHref = new ArrayList<String>();
		address = new ArrayList<String>();
		phone = new ArrayList<String>();
		// initialize current location
		whereAmI();
		// start the AsyncTask that makes the call for the venus search in case of network connection
		///new ().execute();
		/*if (isConnected()) {
			new fourquare().execute();
		} else {
			Toast.makeText(context,
					getResources().getString(R.string.lost_connection),
					Toast.LENGTH_SHORT).show();
		}*/
	}

	//this method to update the current location consequency
	// in any 10000 Mil sec or 10 meters displacing
	@Override
	public void onConnected(Bundle bundle) {
		LocationRequest request = LocationRequest.create().setInterval(10)
				.setSmallestDisplacement(10)
				.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
		myLocationClient
				.requestLocationUpdates(request, new com.google.android.gms.location.LocationListener() {
					@Override
					public void onLocationChanged(Location location) {
						Toast.makeText(context,
								"location changed", Toast.LENGTH_SHORT).show();
						updateLocation(location);
					}
				});

	}

	@Override
	public void onDisconnected() {

	}

	private void updateLocation(Location location) {
		// update lat and lon if we really moved
		Log.d("", "getLatitude" + String.valueOf(myCurrentLocation.getLatitude()));
		if (location != null) {
			if (myCurrentLocation == null ||
					(myCurrentLocation.getLatitude() != location.getLatitude()
							|| myCurrentLocation.getLongitude() != location.getLongitude())) {
				myCurrentLocation = location;
				latitude = String.valueOf(myCurrentLocation.getLatitude());
				longtitude = String.valueOf(myCurrentLocation.getLongitude());
				isUserRequested = false;
				refresh();
			}
		}
	}

	@Override
	public void onConnectionFailed(ConnectionResult connectionResult) {

	}

	private void updateUI() {
		// set the results to the custom list
		CustomListAdapter adapter = new CustomListAdapter(
				AndroidFoursquare.this, name, imageHref, address, distance, phone);
		myListView.invalidate();

		myListView.setAdapter(adapter);

		// call this cafe if it has phone number
		myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//call phone intent

				if (!phone.get(position).isEmpty()) {
					Object listItem = myListView.getItemAtPosition(position);
					Intent callIntent = new Intent(Intent.ACTION_DIAL);
					callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					callIntent.setData(Uri.parse("tel:" + Uri.encode(phone.get(position))));
					startActivity(callIntent);
				}
			}
		});
	}

	protected void refresh() {
		// user can refresh data manually by pressing of a floating button
		new fourquare().execute();
	}

	@Override
	public void onStart() {
		super.onStart();
		myLocationClient.connect();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (myLocationClient.isConnected() || myLocationClient.isConnecting()) {
			myLocationClient.disconnect();
		}
	}

	@Override
	protected void onPause() {
		// just for not crashing on minimize  - because of dialog
		super.onPause();
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}
		progressDialog = null;
	}

	public boolean isConnected() {
		// we check if there is network connection then we call api
		ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
		if (networkInfo != null && networkInfo.isConnected()) {
			return true;
		} else {
			return false;
		}
	}

	private void init() {
		//this method only initialize floating button
		FloatingActionButton fabButton = new FloatingActionButton.Builder(this)
				.withDrawable(getResources().getDrawable(R.drawable.refreshme))
				.withButtonColor(getResources().getColor(R.color.whiteish))
				.withGravity(Gravity.BOTTOM | Gravity.RIGHT)
				.withMargins(0, 0, 16, 16).create();

		fabButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (isConnected()) {
					whereAmI();
					isUserRequested = true;
					refresh();
				} else {
					Toast.makeText(context,
							getResources().getString(R.string.lost_connection),
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private void whereAmI() {
		// this method is for updating my current location
		// we can update it with GPS or Network
		// which Network has more priority because
		// it consume lower battery and is more accessible

		LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		Boolean isGPSEnabled = locationManager
				.isProviderEnabled(LocationManager.GPS_PROVIDER);
		Boolean isNetworkEnabled = locationManager
				.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

		if (isNetworkEnabled) {
			locationManager.requestLocationUpdates(
					LocationManager.NETWORK_PROVIDER, 0, 0, new LocationListener() {
						@Override
						public void onStatusChanged(String provider, int status, Bundle extras) {
						}

						@Override
						public void onProviderEnabled(String provider) {
						}

						@Override
						public void onProviderDisabled(String provider) {
						}

						@Override
						public void onLocationChanged(final Location location) {
						}
					});
			if (locationManager != null) {
				myCurrentLocation = locationManager
						.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
				if (myCurrentLocation != null) {

				}
			}
		}
		if (isGPSEnabled) {
			if (myCurrentLocation == null) {
				locationManager.requestLocationUpdates(
						LocationManager.GPS_PROVIDER, 0, 0, new LocationListener() {
							@Override
							public void onStatusChanged(String provider, int status, Bundle extras) {
							}

							@Override
							public void onProviderEnabled(String provider) {
							}

							@Override
							public void onProviderDisabled(String provider) {
							}

							@Override
							public void onLocationChanged(final Location location) {
							}
						});
				if (locationManager != null) {
					myCurrentLocation = locationManager
							.getLastKnownLocation(LocationManager.GPS_PROVIDER);
				}
			}

		}
		latitude = String.valueOf(myCurrentLocation.getLatitude());
		longtitude = String.valueOf(myCurrentLocation.getLongitude());
	}

	public static class GetMainRequest {
		// standard class to call api
		OkHttpClient client = new OkHttpClient();

		String doGetRequest(String url) throws IOException {
			Request request = new Request.Builder()
					.url(url).build();
			Response response = client.newCall(request).execute();
			return response.body().string();
		}
	}

	//Async because we dont want to interupt UI
	private class fourquare extends AsyncTask<View, Void, String> {
		String rawData;

		@Override
		protected String doInBackground(View... urls) {
			// make Call to the url in the background
			try {
				rawData = makeCall("https://api.foursquare.com/v2/venues/search?client_id="
						+ CLIENT_ID + "&client_secret=" + CLIENT_SECRET + "&v=20130815&ll="
						+ latitude + "," + longtitude + "&query=coffee");

			} catch (IOException e) {
				Log.e("api error", "Failed to get data [" + e + "]");
				error = e.toString();
			}

			return "";
		}

		@Override
		protected void onPreExecute() {
			// showing a dialog before callin api
			super.onPreExecute();
			progressDialog = new ProgressDialog(AndroidFoursquare.this);
			progressDialog.setCancelable(false);
			progressDialog.setMessage(getResources()
					.getString(R.string.loading));
			if (isUserRequested) {
				progressDialog.show();
			}
		}

		@Override
		protected void onPostExecute(String result) {
			if (rawData == null) {
				// we have an error to the call
				Toast.makeText(context,
						getResources().getString(R.string.lost_data) + "error :\n" + error,
						Toast.LENGTH_SHORT).show();
			}
			// cancel dialog
			if (progressDialog != null) {
				progressDialog.dismiss();

				// parseFoursquare venues search result
				venuesList = (ArrayList<FoursquareVenue>) parseFoursquare(rawData);

				//sorting venuesList by distance
				Collections.sort(venuesList, new distanceComparator());
				List<String> listTitle = new ArrayList<String>();
				name.clear();
				distance.clear();
				imageHref.clear();
				address.clear();
				phone.clear();
				for (int i = 0; i < venuesList.size(); i++) {
					// make a list of the venus that are loaded in the list.
					// show the name, the category and the city
					name.add(venuesList.get(i).getName());
					distance.add(String.valueOf(venuesList.get(i).getDistance()));
					imageHref.add(venuesList.get(i).getImageHref());
					address.add(venuesList.get(i).getFormattedAddress());
					phone.add(venuesList.get(i).getContact());
					listTitle.add(i, venuesList.get(i).getName() + ", " + venuesList.get(i)
							.getCategory() + "" + venuesList.get(i)
							.getDistance() + ", " + venuesList.get(i).getFormattedAddress());
				}
				updateUI();

			}
		}
	}

	class distanceComparator implements Comparator<FoursquareVenue> {
		//this class is for sorting venues by distance
		public int compare(FoursquareVenue distance1, FoursquareVenue distance2) {
			return distance1.getDistance() - distance2.getDistance();
		}
	}

}