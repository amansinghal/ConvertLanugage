package com.example.convertlanguage;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import android.util.Log;


public class RestClient {
	public static enum RequestMethod {
		GET, POST;
	}

	private ArrayList<NameValuePair> params;
	private ArrayList<NameValuePair> headers;

	private String url;

	private int responseCode;
	private String message;

	private String response;


	public String getErrorMessage() {
		return message;
	}

	public int getResponseCode() {
		return responseCode;
	}
	
	public RestClient(String url) {
		this.url = url;
		// this.url = this.url + "/sid:"+ Config.SESSION_ID;

		params = new ArrayList<NameValuePair>();
		headers = new ArrayList<NameValuePair>();
	}

	public void AddParam(String name, String value)
	{
		params.add(new BasicNameValuePair(name, value));
	}

	
	public void AddHeader(String name, String value) {
		headers.add(new BasicNameValuePair(name, value));
	}

	public void Execute(RequestMethod method) throws Exception {
		switch (method) {
		case GET: {
			// add parameters
			String combinedParams = "";
			if (!params.isEmpty()) {
				combinedParams += "/";
				for (NameValuePair p : params) {
					Log.i("paramstring", p.getValue().replace(" ", "%20"));
					if (combinedParams.length() > 1) {
						combinedParams += "/"
								+ p.getValue().replace(" ", "%20");
					} else {
						combinedParams += p.getValue().replace(" ", "%20");
					}
				}
			}

			HttpGet request = new HttpGet(url + combinedParams);			
			Log.e("))))", "" + url);
			Log.e("((((", "" + url + combinedParams);
			
			// add headers
			for (NameValuePair h : headers) {
				request.addHeader(h.getName(), h.getValue());
			}

			executeRequest(request, url);
			break;
		}
		case POST: {
			HttpPost request = new HttpPost(url);
	
			// add headers
			for (NameValuePair h : headers) {
				
				request.addHeader(h.getName(), h.getValue());
			}

			if (!params.isEmpty()) {
				ArrayList<String> parameters = new ArrayList<String>();
				for (NameValuePair p : params) 
				{
					Log.v("Parameters>>>>   Name ", p.getName());
					Log.v("Parameters>>>>   Value ", p.getValue());
					parameters.add(p.getValue());
				}
				request.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
			}

			executeRequest(request, url);
			break;
		}
		}
	}

	static public String postHTPPRequest(String URL, String paramenter) {

		System.out.print("hello" + paramenter);
		HttpPost httppost = new HttpPost(URL);
		httppost.setHeader("Content-Type", "application/json");

		try {
			if (paramenter != null) {
				StringEntity tmp = null;

				tmp = new StringEntity(paramenter, "UTF-8");
				httppost.setEntity(tmp);
			}
			HttpResponse httpResponse = null;

			httpResponse = Config.CLIENT.execute(httppost);

			int code = httpResponse.getStatusLine().getStatusCode();
			System.out.print("hello===========" + code);
			HttpEntity entity = httpResponse.getEntity();
			if (entity != null) {
				InputStream input = null;

				input = entity.getContent();
				String res = convertStreamToString(input);
				input.close();

				return res;
			}
		} catch (Exception e) {
			System.out.print("exc" + e.toString());
		}

		return null;
	}

	private void executeRequest(HttpUriRequest request, String url) {

		Log.e("***URL***", "" + url);

		HttpResponse httpResponse;

		try {
			httpResponse = Config.CLIENT.execute(request);
			responseCode = httpResponse.getStatusLine().getStatusCode();
			message = httpResponse.getStatusLine().getReasonPhrase();

			HttpEntity entity = httpResponse.getEntity();

			if (entity != null) {

				InputStream instream = entity.getContent();
				response = convertStreamToString(instream);
				Log.i("Response ", response);
				// Closing the input stream will trigger connection release
				instream.close();
			}

		} catch (ClientProtocolException e) {
			Config.CLIENT.getConnectionManager().shutdown();
			Config.CLIENT = Config.getThreadSafeClient();
			e.printStackTrace();
		} catch (IOException e) {
			Config.CLIENT.getConnectionManager().shutdown();
			Config.CLIENT = Config.getThreadSafeClient();
			e.printStackTrace();
		} catch (Exception e) {
			Config.CLIENT.getConnectionManager().shutdown();
			Config.CLIENT = Config.getThreadSafeClient();
			e.printStackTrace();
		}
	}
	
	public String getResponse()
	{
		return response;
	}

	private static String convertStreamToString(InputStream is) {
		Log.i("Inside convertstream method", "hello");
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();
	}
}
