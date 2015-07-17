package com.ebookfrenzy.foursquare;

import android.app.Activity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by hsedghinezhad on 14/07/2015.
 */
public class CustomListAdapter extends ArrayAdapter<String> {
	private final Activity context;
	private String[] name;
	private String[] address;
	private String[] distance;
	private String[] phone;
	private String[] imageURL;

	public CustomListAdapter(Activity context, List<String> name,
							 List<String> imageHref, List<String> address, List<String> distance, List<String> phone) {
		super(context, R.layout.list_item, name);

		this.context = context;
		this.name = new String[name.size()];
		this.name = name.toArray(this.name);
		this.address = new String[address.size()];
		this.address = address.toArray(this.address);
		this.distance = new String[distance.size()];
		this.distance = distance.toArray(this.distance);
		this.phone = new String[phone.size()];
		this.phone = phone.toArray(this.phone);
		this.imageURL = new String[imageHref.size()];
		this.imageURL = imageHref.toArray(this.imageURL);
	}

	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView = inflater.inflate(R.layout.list_item, null, true);

		TextView txtTitle = (TextView) rowView.findViewById(R.id.title);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.itemImage);
		ImageView phoneIcon = (ImageView) rowView.findViewById(R.id.phone);
		TextView txtAddress = (TextView) rowView.findViewById(R.id.desc);
		TextView txtDistance = (TextView) rowView.findViewById(R.id.distance);
		// Check if data is not wrong
		if (TextUtils.isEmpty(name[position]) || TextUtils.isEmpty(address[position]) ||
				TextUtils.isEmpty(distance[position])) {
			txtTitle.setVisibility(View.GONE);
			txtAddress.setVisibility(View.GONE);
			txtDistance.setVisibility(View.GONE);
			rowView.findViewById(R.id.arrow).setVisibility(View.GONE);
		} else {
			txtTitle.setText(name[position]);
			txtAddress.setText(address[position]);
			txtDistance.setText(distance[position]);
		}
		//Lazy loading via Picasso
		//this lib is added to the project, if you do not want to show images, it is
		//easy ro remove it from dependency
		Picasso.with(context).load(imageURL[position]).into(imageView);

		//
		if(phone[position].isEmpty()) {
			phoneIcon.setVisibility(View.GONE);
		}

		return rowView;

	}

}
