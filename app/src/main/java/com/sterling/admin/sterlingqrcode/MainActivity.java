package com.sterling.admin.sterlingqrcode;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blikoon.qrcodescanner.QrCodeActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_QR_SCAN = 101;
    private int CAMERA_PERMISSION_CODE = 24;
    public static String URL_LOGIN = "";
    private Button btn_ok;
    private EditText ipAddress;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btn_ok = findViewById(R.id.btn_ok);
        ipAddress = findViewById(R.id.ipAddress);
        btn_ok.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String mIPadd = ipAddress.getText().toString().trim();
                URL_LOGIN = "http://"+mIPadd+"/raffle/data.php";
                if (canOpenCamera()) {
                    openCamera();
                } else {
                    requestCameraPermission();
                }
            }
        });
    }
    private void openCamera() {
        //Open Camera
        Intent intent = new Intent(MainActivity.this, QrCodeActivity.class);
        /*start the QrCodeScanner Activity*/
        startActivityForResult(intent, REQUEST_CODE_QR_SCAN);

    }

    private boolean canOpenCamera() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestCameraPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
            Toast.makeText(getApplicationContext(), "Permission Required to Open Camera", Toast.LENGTH_SHORT).show();
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == CAMERA_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the Camera permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

            if (resultCode != Activity.RESULT_OK) {
                if (data == null)
                    return;
                //Getting the passed result
                String result = data.getStringExtra("com.blikoon.qrcodescanner.error_decoding_image");
                if (result != null) {
                    //We fall here if the image the user chose to scan did not contain any QR Code or just failed to be scaned for some other reason
                }
                return;

            }
            if (requestCode == REQUEST_CODE_QR_SCAN) {
                if (data == null)
                    return;
                final String result = data.getStringExtra("com.blikoon.qrcodescanner.got_qr_scan_relult");
                Login("#Sterling2018",result,"Check");
            }
    }
    private void Login(final String key, final String id,final String desc){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        String name="",account="",empid="";
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            Integer status = jsonObject.getInt("success");
                            JSONArray jsonArray = jsonObject.getJSONArray("login");
                            if(status == 1){
                                for(int i=0; i<jsonArray.length();i++){
                                    JSONObject object = jsonArray.getJSONObject(i);
                                    name = "Name: "+object.getString("name");
                                    account = "Department: "+object.getString("account");
                                    empid = "Employee ID: "+object.getString("empid");

                                }
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setTitle("Employee Details");
                                alertDialog.setMessage(empid+"\n"+name+"\n"+account);
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                setLogin(key, id,"Done");
                                            }
                                        });
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Cancel",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                                openCamera();
                                            }
                                        });
                                alertDialog.show();
                            }else{
                                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                                alertDialog.setMessage("Cannot find this record.");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                        new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                openCamera();
                                            }
                                        });
                                alertDialog.show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                            alertDialog.setMessage("Error while retrieving data.");
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        openCamera();
                                    }
                                });
                            alertDialog.show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Cannot connect to server!"+URL_LOGIN , Toast.LENGTH_SHORT).show();
                        openCamera();
                    }
                })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("key",key);
                params.put("id", id);
                params.put("desc", desc);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }

    private void setLogin(final String key, final String id,final String desc){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_LOGIN,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            Integer status = jsonObject.getInt("success");
                            if(status == 1){
                                Toast.makeText(MainActivity.this,"Done",Toast.LENGTH_SHORT).show();
                                openCamera();
                            }else{
                                Toast.makeText(MainActivity.this,"Cannot find this record.",Toast.LENGTH_SHORT).show();
                                openCamera();
                            }
                        } catch (JSONException e){
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,"Error while parsing the data.",Toast.LENGTH_SHORT).show();
                            openCamera();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(MainActivity.this, "Cannot connect to server!" , Toast.LENGTH_SHORT).show();
                    }
                })
        {
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("key",key);
                params.put("id", id);
                params.put("desc", desc);
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
}