package com.codelouis.catherine;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Luis Hernandez on 27/April/2018
 */
public class DataFragment extends Fragment {

    private String TAG = DataFragment.class.getSimpleName();

    private static final String ARG_SECTION_NUMBER = "section_number";

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON Endpoint
    private String url = "https://firebasestorage.googleapis.com/v0/b/polyfireapp2.appspot.com/o/pruebajson.json?alt=media&token=d5c47f6c-2eb1-433a-8cdd-060f9df93ab0";

    ArrayList<HashMap<String, String>> contactList;

    private AnimationDialogFragment mLoadingFragment;

    private TextView mPeopleNumber;

    private GraphView graph;

    public DataFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static DataFragment newInstance(int sectionNumber) {
        DataFragment fragment = new DataFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_datafragment, container, false);
        TextView textView = (TextView) rootView.findViewById(R.id.section_label);
        mPeopleNumber = (TextView) rootView.findViewById(R.id.person_count);
        //textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

        contactList = new ArrayList<>();
        lv = (ListView) rootView.findViewById(R.id.list3);


        //dumy values
        graph = (GraphView) rootView.findViewById(R.id.graph);

        SimpleDateFormat mdformat = new SimpleDateFormat("yyyy-M-d ");

        switch (getArguments().getInt(ARG_SECTION_NUMBER)){
            case 1:
                textView.setText("Today");
                if(BuildConfig.FLAVOR.equals("firebase")) {
                    url = "https://firebasestorage.googleapis.com/v0/b/polyfireapp2.appspot.com/o/pruebajson.json?alt=media&token=d5c47f6c-2eb1-433a-8cdd-060f9df93ab0";
                }else if (BuildConfig.FLAVOR.equals("azure")){
                    Calendar calendar = Calendar.getInstance();
                    String toDate = mdformat.format(calendar.getTime());
                    String fromDate = "";
                    if(calendar.get(Calendar.MONTH)==0 && calendar.get(Calendar.DAY_OF_MONTH)==1){
                        fromDate = fromDate + (calendar.get(Calendar.YEAR)-1) + "-12-31";
                    }else if(calendar.get(Calendar.DAY_OF_MONTH)==1){
                        fromDate = fromDate + calendar.get(Calendar.YEAR) +"-"+ calendar.get(Calendar.MONTH) + "-30";
                    }else{
                        fromDate = fromDate + calendar.get(Calendar.YEAR) +"-"+ (calendar.get(Calendar.MONTH)+1) +"-"+ (calendar.get(Calendar.DAY_OF_MONTH)-1);
                    }
                    Log.d(TAG, "day "+fromDate + " " +toDate);
                    url = "http://imi359.azurewebsites.net/api/v2/humanos/obtenerhumanoporrangofecha?desde="+fromDate+"&hasta="+toDate;
                }
                new GetContacts().execute();
                break;
            case 2:
                textView.setText("Last Month");
                if(BuildConfig.FLAVOR.equals("firebase")) {
                    url = "https://firebasestorage.googleapis.com/v0/b/polyfireapp2.appspot.com/o/pruebaano.json?alt=media&token=9919f0eb-4022-4ef2-80e2-c939a246f4d2";
                }if (BuildConfig.FLAVOR.equals("azure")){
                    Calendar calendar = Calendar.getInstance();
                    String toDate = mdformat.format(calendar.getTime());
                    String fromDate = "";
                    if(calendar.get(Calendar.MONTH)==0){
                        fromDate = fromDate + (calendar.get(Calendar.YEAR)-1) + "-12-" +calendar.get(Calendar.DAY_OF_MONTH);
                    }
                    fromDate = fromDate + calendar.get(Calendar.YEAR) + "-" + calendar.get(Calendar.MONTH) + "-" + calendar.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "month "+fromDate + " " +toDate);
                    url = "http://imi359.azurewebsites.net/api/v2/humanos/obtenerhumanoporrangofecha?desde="+fromDate+"&hasta="+toDate;
                }
                new GetContacts().execute();
                break;
            case 3:
                textView.setText("Last Year");
                if(BuildConfig.FLAVOR.equals("firebase")) {
                    url = "https://firebasestorage.googleapis.com/v0/b/polyfireapp2.appspot.com/o/pruebames.json?alt=media&token=92a5465a-bcbe-4dc8-9814-6388ee46b34c";
                }if (BuildConfig.FLAVOR.equals("azure")){
                    Calendar calendar = Calendar.getInstance();
                    String toDate = mdformat.format(calendar.getTime());
                    String fromDate = (calendar.get(Calendar.YEAR)-1)+"-"+(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH);
                    Log.d(TAG, "year "+fromDate + " " +toDate);
                    url = "http://imi359.azurewebsites.net/api/v2/humanos/obtenerhumanoporrangofecha?desde="+fromDate+"&hasta="+toDate;
                }
                new GetContacts().execute();
                break;
        }

        //graph.addSeries(series);

        //run Async server call
        //new GetContacts().execute();

        return rootView;
    }

    public void refresh(){
        new GetContacts().execute();
    }


    /**
     * Async task class to get json by making HTTP call
     */
     private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingFragment = new AnimationDialogFragment ();
            mLoadingFragment.show(getFragmentManager(), "Loading");
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            //Log.e(TAG, "Response from url: " + jsonStr);

            if (jsonStr != null) {
                try {
                    if(BuildConfig.FLAVOR.equals("firebase")) {
                        contactList.clear();
                        JSONObject jsonObj = new JSONObject(jsonStr);

                        // Getting JSON Array node
                        JSONArray serverData = jsonObj.getJSONArray("lecturas");

                        // looping through All Contacts
                        for (int i = 0; i < serverData.length(); i++) {
                            JSONObject c = serverData.getJSONObject(i);

                            String date = c.getString("date");
                            String time = c.getString("time");
                            String count = c.getString("count");

                            // Phone node is JSON Object
                        /*JSONObject phone = c.getJSONObject("phone");
                        String mobile = phone.getString("mobile");
                        String home = phone.getString("home");
                        String office = phone.getString("office");*/

                            // tmp hash map for single contact
                            //HashMap<String, String> contact = new HashMap<>();
                            HashMap<String, String> data = new HashMap<>();

                            // adding each child node to HashMap key => value
                        /*contact.put("id", id);
                        contact.put("name", name);
                        contact.put("email", email);
                        contact.put("mobile", mobile);*/
                            data.put("time", time);
                            data.put("date", date);
                            data.put("count", count);

                            // adding contact to contact list
                            contactList.add(data);
                        }
                    }else if(BuildConfig.FLAVOR.equals("azure")) {
                        contactList.clear();
                        JSONArray serverData = new JSONArray(jsonStr);
                        for (int i = 0; i < serverData.length(); i++) {
                            JSONObject c = serverData.getJSONObject(i);
                            String date = c.getString("fecha");
                            String time = c.getString("tiempo");
                            String count = c.getString("total");

                            HashMap<String, String> data = new HashMap<>();

                            data.put("time", time);
                            data.put("date", date);
                            data.put("count", count);

                            // adding contact to contact list
                            contactList.add(data);
                        }
                    }
                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });

                }
            } else {
                Log.e(TAG, "Couldn't get json from server.");
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),
                                "Couldn't get json from server. Check LogCat for possible errors!",
                                Toast.LENGTH_LONG)
                                .show();
                    }
                });

            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (mLoadingFragment.isVisible())
                mLoadingFragment.dismiss();

            ///render data
            int counter = 0;
            LineGraphSeries<DataPoint> serie = new LineGraphSeries<>();
            graph.removeAllSeries();

            for (int i = 0; i < contactList.size(); i++) {
                counter = counter + Integer.parseInt(contactList.get(i).get("count"));
                serie.appendData(new DataPoint(i,Double.parseDouble(contactList.get(i).get("count"))),true,contactList.size());
            }

            mPeopleNumber.setText(Integer.toString(counter));
            graph.addSeries(serie);

            /**
             * Updating parsed JSON data into ListView
             * */
            /*ListAdapter adapter = new SimpleAdapter(
                    getContext(), contactList,
                    R.layout.list_item, new String[]{"date", "time",
                    "count"}, new int[]{R.id.name,
                    R.id.email, R.id.mobile});

            lv.setAdapter(adapter);*/
        }

    }
}
