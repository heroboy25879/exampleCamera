package com.hitachi.com.examplecamera;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.PropertyInfo;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {
    public static final int requestcode = 1;
    ImageView img;
    Button btnupload, btnchooseimage;
    EditText edtname;
    byte[] byteArray;

    String encodedImage;
    TextView txtmsg;

    ProgressBar pg;

    String serverresponse;
    String imgName;

    private static String NAMESPACE = "<a class=\"vglnk\" href=\"http://tempuri.org/\" rel=\"nofollow\"><span>http</span><span>://</span><span>tempuri</span><span>.</span><span>org</span><span>/</span></a>"; // namespace of the
    // webservice
    private static String URL = "<a class=\"vglnk\" href=\"http://203.154.103.42/WebServiceKL2/ByteArray2.asmx\" rel=\"nofollow\"><span>http</span><span>://</span><span>203</span><span>.</span><span>154</span><span>.</span><span>103</span><span>.</span><span>42</span><span>/</span><span>WebServiceKL2</span><span>/</span><span>ByteArray2</span><span>.</span><span>asmx</span></a>";
//    private static String URL = "http://203.154.103.42/WebServiceKL2/ByteArray2.asmx";

// url of the webservice hosted on localhost

    private static String SOAP_ACTION = "<a class=\"vglnk\" href=\"http://tempuri.org/\" rel=\"nofollow\"><span>http</span><span>://</span><span>tempuri</span><span>.</span><span>org</span><span>/</span></a>";
    String serverreponse = " ";
    SoapSerializationEnvelope envelope;
    SoapPrimitive response;
    SoapObject request;
    PropertyInfo ImgName, ImgValue;
    HttpTransportSE androidHttpTransport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkIfAlreadyhavePermission()) {
            requestForSpecificPermission();
        }


        img = (ImageView) findViewById(R.id.imageview);

        btnupload = (Button) findViewById(R.id.btnupload);
        // this will be used for sending the base64 value to the server.
        btnchooseimage = (Button) findViewById(R.id.btnchooseimage);
        // this will be used for choosing the image from gallery
        edtname = (EditText) findViewById(R.id.edtname);
        txtmsg = (TextView) findViewById(R.id.txtmsg);

        pg = (ProgressBar) findViewById(R.id.progressBar1);
        pg.setVisibility(View.GONE);

        btnchooseimage.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ChooseImage();
            }
        });

        btnupload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                imgName = edtname.getText().toString();
                AsyncCallWS task = new AsyncCallWS();
                task.execute();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    public void ChooseImage() {
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)
                && !Environment.getExternalStorageState().equals(
                Environment.MEDIA_CHECKING)) {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, 1);

        } else {
            Toast.makeText(MainActivity.this,
                    "No activity found to perform this task",
                    Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            Bitmap originBitmap = null;
            Uri selectedImage = data.getData();
            Toast.makeText(MainActivity.this, selectedImage.toString(),
                    Toast.LENGTH_LONG).show();
            txtmsg.setText(selectedImage.toString());
            InputStream imageStream;
            try {
                imageStream = getContentResolver().openInputStream(
                        selectedImage);
                originBitmap = BitmapFactory.decodeStream(imageStream);

            } catch (FileNotFoundException e) {
            }
            if (originBitmap != null) {
               // this.img.setImageBitmap(originBitmap);

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                originBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
                encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT);
            }
        } else {
            txtmsg.setText("There's an error if this code doesn't work, thats all I know");

        }
    }

    private class AsyncCallWS extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {

            request = new SoapObject(NAMESPACE, "Hello");
            // creating the SoapObject and "Hello" is the method name of the webservice which will called.

            ImgName = new PropertyInfo();
            ImgName.setName("ImgName"); // setting the variable name
            ImgName.setValue(imgName);// setting the value
            ImgName.setType(String.class); // setting the type
            request.addProperty(ImgName);// adding the property

            ImgValue = new PropertyInfo();
            ImgValue.setName("ImgValue");
            ImgValue.setValue(encodedImage);
            ImgValue.setType(String.class);
            request.addProperty(ImgValue);

            envelope = new SoapSerializationEnvelope(SoapEnvelope.VER11);
            // creating a serialized envelope which will be used to carry the
            // parameters for SOAP body and call the method
            envelope.dotNet = true;
            envelope.setOutputSoapObject(request);

            androidHttpTransport = new HttpTransportSE(URL);

            try {

                androidHttpTransport.call(SOAP_ACTION + "Hello", envelope); // Hello
                response = (SoapPrimitive) envelope.getResponse();
                serverreponse = response.toString();

            } catch (Exception e) {
                e.printStackTrace();
                serverreponse = "Error Occurred " + e.getCause() + e.getMessage();
            }

            return serverreponse;
        }

        @Override
        protected void onPostExecute(String result) {
            txtmsg.setText(result);
            pg.setVisibility(View.INVISIBLE);
        }

        @Override
        protected void onPreExecute() {
            pg.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }











    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 101:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //granted
                } else {
                    //not granted
                }

                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        }
    }

    //What is permission be request
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void requestForSpecificPermission() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.INTERNET,
                Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.READ_CONTACTS
        }, 101);

    }

    //Check the permission is already have
    private boolean checkIfAlreadyhavePermission() {

        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.GET_ACCOUNTS);
        return result == PackageManager.PERMISSION_GRANTED;

    }
}
