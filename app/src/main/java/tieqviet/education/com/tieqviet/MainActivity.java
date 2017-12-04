package tieqviet.education.com.tieqviet;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    private EditText etInput;
    private ImageView btnDelete;
    private TextView txtKetQuaDich;
    private TextToSpeech mTTS;
    private AdView mAdView;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        etInput = findViewById(R.id.et_Input);
        Button btnDich =  findViewById(R.id.btnDich);
        Button btnDoc = findViewById(R.id.btnDoc);
        Button btnChiaSe = findViewById(R.id.btnChiaSe);
        txtKetQuaDich = findViewById(R.id.txt_KetQuaDich);
        btnDelete = findViewById(R.id.btnDelete);
        ImageView btnSpeech = findViewById(R.id.btnSpeech);
        Button btnCopy = findViewById(R.id.btnCopy);



        mTTS = new TextToSpeech(this, this,
                "com.google.android.tts");
        mTTS.setLanguage(new Locale("vi-VN"));
        mTTS.setSpeechRate((float) 0.8);
        mTTS.setPitch((float) 0.9);

        mAdView = findViewById(R.id.adView);
        AdRequest.Builder builder = new AdRequest.Builder();
        AdRequest adRequest = builder.build();
        mAdView.loadAd(adRequest);
        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mAdView.setVisibility(View.VISIBLE);
            }
        });
        btnDich.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mStringIn = etInput.getText().toString();
                mStringIn = getTextDichOldToNew(mStringIn);
                txtKetQuaDich.setText(mStringIn);
            }
        });
        btnDoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!mTTS.isSpeaking()){
                    mTTS.speak(txtKetQuaDich.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                }else{
                    mTTS.stop();
                }
            }
        });
        btnChiaSe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent sharingIntent = new Intent(
                        Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent
                        .putExtra(Intent.EXTRA_TEXT,
                                txtKetQuaDich.getText().toString());
                startActivity(Intent.createChooser(sharingIntent,
                        "Share To Friends"));
            }
        });
        etInput.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if(etInput.getText().toString().length()>15){
                    btnDelete.setVisibility(View.VISIBLE);
                }else{
                    btnDelete.setVisibility(View.GONE);
                }
            }
        });
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etInput.setText("");
                btnDelete.setVisibility(View.GONE);
            }
        });

        btnCopy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!etInput.getText().toString().isEmpty()) {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(MainActivity.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(etInput.getText().toString(), etInput.getText().toString());
                    if (clipboard != null) {
                        clipboard.setPrimaryClip(clip);
                    }
                    Toast.makeText(getApplicationContext(),"Đã Copy",Toast.LENGTH_LONG).show();
                }
            }
        });
        btnSpeech.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setSpeechInput();
            }
        });

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (Intent.ACTION_SEND.equals(action) && type != null) {
            if ("text/plain".equals(type)) {
                String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
                if(sharedText!=null){
                    etInput.setText(sharedText);
                    String strDich = getTextDichOldToNew(sharedText);
                    txtKetQuaDich.setText(strDich);
                }

            }
        }
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M)
            if (Intent.ACTION_PROCESS_TEXT.equals(action) && type != null) {
                if ("text/plain".equals(type)) {
                    String sharedText = intent.getStringExtra(Intent.EXTRA_PROCESS_TEXT);
                    if(sharedText!=null){
                        etInput.setText(sharedText);
                        String strDich = getTextDichOldToNew(sharedText);
                        txtKetQuaDich.setText(strDich);
                    }


                }
            }


    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            String mStringIn = etInput.getText().toString();
            mStringIn = getTextDichOldToNew(mStringIn);
            txtKetQuaDich.setText(mStringIn);
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onStop() {
        mTTS.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mTTS.shutdown();
        super.onDestroy();
    }


    @Override
    public void onInit(int status) {
        if(status == TextToSpeech.SUCCESS){
            int result=mTTS.setLanguage(new Locale("vi-VN"));
            if(result==TextToSpeech.LANG_MISSING_DATA ||
                    result==TextToSpeech.LANG_NOT_SUPPORTED){
                Log.e("error", "This Language is not supported");
            }
            else{
                Log.e("Text to Speech", "supported");
            }
        }
        else
            Log.e("error", "Initilization Failed!");
    }
    String[] arrDauCau = {".",",","(",")","\"","'","<",">","@","!","/",";",":"};

    String[] arrAmDau3={"ngh","Ngh","NGH"};
    String[] arrThayAmDau3={"q","Q","Q"};

    String[] arrAmDau2={"ch","Ch","CH","gh","Gh","GH","gi","Gi","GI","kh","Kh","KH"
            ,"ng","Ng","NG","nh","Nh","NH","ph","Ph","PH","th","Th","TH","tr","Tr","TR"};
    String[] arrThayAmDau2={"c","C","C","g","G","G","z","Z","Z","x","X","X"
            ,"q","Q","Q","n'","N'","N'","f","F","F","w","W","W","c","C","C"};

    String[] arrAmDau1={"c","C","d","D","đ","Đ","k","K","q","Q","r","R"};
    String[] arrThayAmDau1={"k","K","z","Z","d","D","k","K","k","K","z","Z"};

    ArrayList<String[]> arrayListAmDau = new ArrayList<>();
    ArrayList<String[]> arrayListThayAmDau = new ArrayList<>();

    /*String[] arrThanhDieu2={"ch","nh"};
    String[] arrThayThanhDieu2={"c","n'"};*/

    String[] arrThanhDieu2={"ch","CH","nh","NH","ng","NG"};
    String[] arrThayThanhDieu2={"c","C","n'","N'","q","Q"};

    String[] arrThanhDieu1={"c","C"};
    String[] arrThayThanhDieu1={"k","K"};

    ArrayList<String[]> arrayListThanhDieu = new ArrayList<>();
    ArrayList<String[]> arrayListThayThanhDieu = new ArrayList<>();
    private String getTextDichOldToNew(String in) {
        arrayListAmDau.add(arrAmDau1);
        arrayListAmDau.add(arrAmDau2);
        arrayListAmDau.add(arrAmDau3);
        arrayListThayAmDau.add(arrThayAmDau1);
        arrayListThayAmDau.add(arrThayAmDau2);
        arrayListThayAmDau.add(arrThayAmDau3);

        arrayListThanhDieu.add(arrThanhDieu1);
        arrayListThanhDieu.add(arrThanhDieu2);
        arrayListThayThanhDieu.add(arrThayThanhDieu1);
        arrayListThayThanhDieu.add(arrThayThanhDieu2);


        // Tach String to Array
        String[] arrAmTiet = in.split(" ");
        StringBuilder amTietBuilder = new StringBuilder();
        for (String amTiet : arrAmTiet) {
            //Tách dấu cau
            String mStrTruoc = getDauCauTruoc(amTiet);
            //Log.d("Trước",mStrTruoc);
            String mStrSau = getDauCauSau(amTiet);
            //Log.d("Sau",mStrSau);
            amTiet = amTiet.substring(mStrTruoc.length());
            amTiet = amTiet.substring(0,amTiet.length()-mStrSau.length());
            //Log.d("Âm Tiết",amTiet);
            if (amTiet.length() > 3) {
                kloop:
                for(int k = 2; k>=0;k--){
                    String amDau = amTiet.substring(0, k+1);
                    for (int i = 0; i < arrayListAmDau.get(k).length; i++) {
                        //So sánh 3
                        if (amDau.equals(arrayListAmDau.get(k)[i])) {
                            amDau = arrayListThayAmDau.get(k)[i];
                            amTiet = amDau + amTiet.substring(k+1);
                            break kloop;
                        }
                    }
                }

                eloop:
                for(int e = 2;e>0;e--){
                    String amSau = amTiet.substring(amTiet.length() - e);
                    for (int i = 0; i < arrayListThanhDieu.get(e-1).length; i++) {
                        //So sánh 2
                        if (amSau.equals(arrayListThanhDieu.get(e-1)[i])) {
                            amSau = arrayListThayThanhDieu.get(e-1)[i];
                            amTiet = amTiet.substring(0, amTiet.length() - e) + amSau;
                            break eloop;
                        }
                    }
                }
                //amTietBuilder.append(" " + amTiet);
            }else if(amTiet.length()>2){
                kloop:
                for(int k = 1; k>=0;k--){
                    String amDau = amTiet.substring(0, k+1);
                    for (int i = 0; i < arrayListAmDau.get(k).length; i++) {
                        //So sánh 3
                        if (amDau.equals(arrayListAmDau.get(k)[i])) {
                            amDau = arrayListThayAmDau.get(k)[i];
                            amTiet = amDau + amTiet.substring(k+1);
                            break kloop;
                        }
                    }
                }
                eloop:
                for(int e = 2;e>0;e--){
                    String amSau = amTiet.substring(amTiet.length() - e);
                    for (int i = 0; i < arrayListThanhDieu.get(e-1).length; i++) {
                        //So sánh 2
                        if (amSau.equals(arrayListThanhDieu.get(e-1)[i])) {
                            amSau = arrayListThayThanhDieu.get(e-1)[i];
                            amTiet = amTiet.substring(0, amTiet.length() - e) + amSau;
                            break eloop;
                        }
                    }
                }
                //amTietBuilder.append(" " + amTiet);
            }else if(amTiet.length()>1){
                kloop:
                for(int k = 0; k>=0;k--){
                    String amDau = amTiet.substring(0, k+1);
                    for (int i = 0; i < arrayListAmDau.get(k).length; i++) {
                        //So sánh 3
                        if (amDau.equals(arrayListAmDau.get(k)[i])) {
                            amDau = arrayListThayAmDau.get(k)[i];
                            amTiet = amDau + amTiet.substring(k+1);
                            break kloop;
                        }
                    }
                }
                //amTietBuilder.append(" " + amTiet);
            }
            String strNewAmTiet = " " + mStrTruoc + amTiet + mStrSau;
            amTietBuilder.append(strNewAmTiet);

        }
        return amTietBuilder.toString();
    }
    private String getDauCauTruoc(String strIn){
        StringBuilder strBuilder = new StringBuilder();
        for(int i=0;i<strIn.length();i++){
            String mStr = strIn.substring(i,i+1);
            if(Arrays.asList(arrDauCau).contains(mStr))
                strBuilder.append(mStr);
            else
                break;
        }
        return strBuilder.toString();
    }
    private String getDauCauSau(String strIn){
        StringBuilder trave = new StringBuilder();
        for(int i=strIn.length();i>0;i--){
            String mStr = strIn.substring(i-1,i);
            if(Arrays.asList(arrDauCau).contains(mStr))
                trave.append(mStr);
            else
                break;
        }
        String mStrSau = trave.reverse().toString();
        Log.d("Sau",mStrSau);
        return mStrSau;
    }

    private void setSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi");
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "vi");
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),"Không hỗ trợ nhận diện giọng nói",
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Receiving speech input
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    etInput.setText(result.get(0));
                }
                break;
            }

        }
    }
}
