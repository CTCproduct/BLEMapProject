package com.example.ctc.blemapapplication;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;

public class ReceivedDeviceAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater layoutInflater = null;
    private ArrayList<ReceiveDeviceItem> mDeviceList;

    public ReceivedDeviceAdapter(Context context, ArrayList<ReceiveDeviceItem> deviceList) {
        mContext = context;
        mDeviceList = deviceList;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.received_device_item, parent, false);
        }

        TextView deviceName = (TextView) convertView.findViewById(R.id.deviceName);
        deviceName.setText(mDeviceList.get(position).getName());

        ImageButton mapButton = (ImageButton) convertView.findViewById(R.id.mapButton);
        mapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, MapActivity.class);
                intent.putExtra("name", mDeviceList.get(position).getName());
                intent.putExtra("latitude", mDeviceList.get(position).getLatitude());
                intent.putExtra("longitude", mDeviceList.get(position).getLongitude());
                mContext.startActivity(intent);
            }
        });

        return convertView;
    }

}
