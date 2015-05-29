package com.example.convertlanguage;

import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONObject;

import com.example.convertlanguage.RestClient.RequestMethod;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements OnClickListener 
{

	Toolbar toolbar;
	Button btn_submit,btn_speech;
	EditText et_text;
	TextView tv_converted_text,tv_locale_language;
	Spinner spnr_to;
	String[] languages,language_code;
	ArrayAdapter<String> adapter;
	Locale[] availableLocales;
	Locale currentLocale;
	TextToSpeech textToSpeech;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		currentLocale = Locale.getDefault();
		initViews();
		tv_locale_language.setText(currentLocale.getDisplayName()+" ,"+currentLocale.getDisplayLanguage());
		if(toolbar != null)
		{			
			setSupportActionBar(toolbar);
		}
		languages = getLanguagesList();
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, languages);
		spnr_to.setAdapter(adapter);		
	}

	private String[] getLanguagesList()
	{
		String[] tempFiller;		
		availableLocales=Locale.getAvailableLocales();
		tempFiller = new String[availableLocales.length];
		for(int i=0; i < availableLocales.length ; i++)
		{			
			tempFiller[i] = availableLocales[i].getDisplayName();
		}
		return tempFiller;
	}

	private void initViews()
	{
		toolbar = (Toolbar)findViewById(R.id.toolbar);
		btn_submit = (Button)findViewById(R.id.button1);
		btn_submit.setOnClickListener(this);
		btn_speech= (Button)findViewById(R.id.button2);
		btn_speech.setOnClickListener(this);
		et_text = (EditText)findViewById(R.id.editText1);
		tv_converted_text = (TextView)findViewById(R.id.textView1);
		tv_locale_language = (TextView)findViewById(R.id.tv_locale_language);
		spnr_to = (Spinner)findViewById(R.id.spinner2);
		spnr_to.setPrompt("Convert to..");
	}

	class convertAsynctask extends AsyncTask<String, Void, Integer>
	{
		String translatedText="";
		ProgressDialog dialog;
		@Override
		protected void onPreExecute() 
		{		
			super.onPreExecute();
			dialog = ProgressDialog.show(MainActivity.this, null, "Converting please wait....");
			tv_converted_text.setText("");
		}

		@Override
		protected Integer doInBackground(String... params) 
		{		
			String url ,response = "";			
			RestClient client ;
			try
			{
				//url = "https://www.googleapis.com/language/translate/v2?q=Hello+this+is+aman+singhal&target=es&source=en&key=AIzaSyCZHDH3FTet-H0MNcJg6ffmi5fkrqh9QHw";
				url = "http://api.mymemory.translated.net/get?q="+URLEncoder.encode(params[0], "UTF-8")+"&langpair="+URLEncoder.encode(params[1].substring(0,2)+"|"+params[2].substring(0,2), "UTF-8");				
				client  = new RestClient(url);
				client.Execute(RequestMethod.POST);
				response = client.getResponse();
				JSONObject tempObject = new JSONObject(response);
				if(!tempObject.getString("responseStatus").equals("200"))
				{
					tempObject = tempObject.getJSONObject("responseData");
					translatedText = tempObject.getString("translatedText");
					return -1;
				}
				tempObject = tempObject.getJSONObject("responseData");
				translatedText = tempObject.getString("translatedText");
				return 1;
			} 
			catch (Exception e) 
			{			
				e.printStackTrace();
				return 0;
			}						
		}

		@Override
		protected void onPostExecute(Integer result) 
		{		
			super.onPostExecute(result);
			dialog.dismiss();
			if(result == 1)
			{
				tv_converted_text.setText(Html.fromHtml("<b>Converted text</b> <br>"+translatedText));
			}
			else if(result == 0)
			{
				Config.alertDialogBox(MainActivity.this, null, null, "Problem while converting the text.", false);
			}
			else if(result == -1)
			{
				tv_converted_text.setText(Html.fromHtml("<b>Converted text:Error</b> <br>"+translatedText));
				Config.alertDialogBox(MainActivity.this, null, null, "Could not converted to the "+availableLocales[spnr_to.getSelectedItemPosition()].getDisplayCountry(), false);
			}
		}
	}

	@Override
	public void onClick(View v) 
	{
		if(v.getId() == R.id.button1)
		{
			if(!et_text.getText().toString().isEmpty())
				new convertAsynctask().execute(et_text.getText().toString(),currentLocale.getISO3Language(),availableLocales[spnr_to.getSelectedItemPosition()].getISO3Language());
			else
				Config.alertDialogBox(this, null, null, "Please enter text to convert", false);
		}
		if(v.getId() == R.id.button2)
		{
			if(tv_converted_text.getText().toString().isEmpty())
			{
				Config.alertDialogBox(this, null, null, "Empty text cannot be speaked.", false);
				return;
			}
			textToSpeech=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() 
			{
				@Override
				public void onInit(int status) 
				{
					if(status != TextToSpeech.ERROR)
					{
						textToSpeech.setLanguage(availableLocales[spnr_to.getSelectedItemPosition()]);
					}				
				}
			});			
			textToSpeech.speak(et_text.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
		}
	}
}
