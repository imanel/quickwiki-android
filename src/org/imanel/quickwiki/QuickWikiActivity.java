package org.imanel.quickwiki;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;

public class QuickWikiActivity extends Activity {
	
	protected EditText searchText;
	protected Button searchButton;
	protected ListView resultList;
	protected ListAdapter adapter;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        
        searchText = (EditText) findViewById (R.id.searchText);
        searchButton = (Button) findViewById (R.id.searchButton);
        resultList = (ListView) findViewById (R.id.list);
        
        addListenerOnSearchButton();
    }
    
    public void addListenerOnSearchButton() {
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			  search();
			}
		});
	}
    
    public void search() {
    	String searchResults = getSearchResults();
    	try {
    		JSONObject jsonObject = new JSONObject(searchResults);
    		interpretData(jsonObject);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public String getSearchResults() {
    	String result = "";
    	String query = "";
		try {
			query = URLEncoder.encode(searchText.getText().toString(), "UTF-8");
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		}
    	String url = "http://api.quickwiki.info/json?q=" + query;
    	
    	try {
    		HttpClient hc = new DefaultHttpClient();
    		HttpGet get = new HttpGet(url);

    		HttpResponse rp = hc.execute(get);

    		if(rp.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
    		{
    			result = EntityUtils.toString(rp.getEntity());
    		}
    	} catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	return result;
    }
    
    public void interpretData(JSONObject data) {
    	String[] results = {};
    	try {
			Log.v("QuickWiki", data.getString("result"));
			if (data.getString("result").equals("missing")) {
				String[] tempResults = { "No article found" };
				results = tempResults;
			} else if (data.getString("result").equals("text")) {
				String[] tempResults = { data.getString("data") };
				results = tempResults;
			} else if (data.getString("result").equals("list")) {
				JSONArray jsonArray = data.getJSONArray("data");
				for (int i = 0; i < jsonArray.length(); i++) {
					JSONObject entry = jsonArray.getJSONObject(i);
					String[] tempResults = new String[i + 1];
					tempResults[i] = entry.getString("text");
					System.arraycopy(results, 0, tempResults, 0, results.length);
					results = tempResults;
				}
			} else {
				String[] tempResults = { "Unknown error occured. Please try again later" };
				results = tempResults;
			}
		} catch (JSONException e) {
			String[] tempResults = { "Unknown error occured. Please try again later" };
			results = tempResults;
			e.printStackTrace();
		}
    	setResults(results);
    }
    
    public void setResults(String[] results) {
    	adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, results);
        resultList.setAdapter(adapter);
    }
}