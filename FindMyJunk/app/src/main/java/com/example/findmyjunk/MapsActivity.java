package com.example.findmyjunk;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {


    private GoogleMap mMap;
    private static final int ACCESS_FINE_LOCATION = 1;
    static final int REQUEST_TAKE_PHOTO = 1;
    private static final String FILENAME = "junk_file";
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Location junk;
    private LocationRequest locationRequest;
    private ArrayList<Double> latArray;
    private ArrayList<Double> lonArray;
    private Button marker;
    private Button image;
    private Button track;
    private String markerText;
    private ImageView imageView;
    private Polyline polyline;
    private Circle circle;
    private PolylineOptions polylineOptions;
    private JunkObject junkObject;
    private boolean trackRoute;
    private String mCurrentPhotoPath;
    private Uri photoUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        init();

        setButtons();
    }

    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    protected void onPause() {
        super.onPause();
        if (junkObject != null) {
            junkObject.setMarkerLat(junk.getLatitude());
            junkObject.setMarkerLon(junk.getLongitude());
            junkObject.setMarkerString(markerText);
            //junkObject.setPhotoPath(mCurrentPhotoPath);
            if (polyline != null) {
                for (LatLng loc : polyline.getPoints()) {
                    latArray.add(loc.latitude);
                    lonArray.add(loc.longitude);
                }
            }
            junkObject.setPolyArrayLat(latArray);
            junkObject.setPolyArrayLon(lonArray);
            try {
                FileOutputStream fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(junkObject);
                fos.close();
                oos.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (mLastLocation == null) {
            LatLng current = new LatLng(0, 0);
            mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        } else {
            LatLng current = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            mMap.addMarker(new MarkerOptions().position(current).title("Current Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        }
        polylineOptions = new PolylineOptions()
                .width(5)
                .color(Color.RED)
                .zIndex(30);
        polyline = mMap.addPolyline(polylineOptions);

        initSavedData();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);
        if (mLastLocation != null) {
        } else {
            Toast.makeText(getApplicationContext(), "Location Not Found!", Toast.LENGTH_SHORT).show();
        }
        startLocationUpdates();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION);
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, locationRequest, this);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        LatLng lastLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (trackRoute) {
            polylineOptions.add(lastLatLng);
            polyline.remove();
        }
        polyline = mMap.addPolyline(polylineOptions);
        if (circle != null) {
            circle.remove();
        }
        circle = mMap.addCircle(new CircleOptions().center(lastLatLng).strokeColor(Color.LTGRAY).fillColor(Color.GRAY).radius(7).strokeWidth(15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLatLng));
    }

    @Override
    public void onConnectionSuspended(int cause) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case ACCESS_FINE_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates();
                } else {

                }
                return;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        //set parts of uri for saving.
        imageView.setImageURI(photoUri);
        galleryAddPic();
    }

    public void setButtons() {
        marker.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (mLastLocation == null) {
                    Toast.makeText(getApplicationContext(), "Location Not Found", Toast.LENGTH_SHORT).show();
                } else {

                    AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                    LayoutInflater inflater = MapsActivity.this.getLayoutInflater();

                    View markerTextView = inflater.inflate(R.layout.marker_text, null);

                    builder.setView(markerTextView);

                    final EditText userInput = (EditText) markerTextView
                            .findViewById(R.id.editTextDialogUserInput);

                    builder
                            .setPositiveButton("OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            polylineOptions = new PolylineOptions()
                                                    .width(5)
                                                    .color(Color.RED)
                                                    .zIndex(30);
                                            mMap.clear();
                                            markerText = userInput.getText().toString();
                                            junk = mLastLocation;
                                            latArray.clear();
                                            lonArray.clear();
                                            LatLng current = new LatLng(junk.getLatitude(), junk.getLongitude());
                                            mMap.addMarker(new MarkerOptions().position(current).title(markerText));
                                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(current, 17.0f));
                                        }
                                    })
                            .setNegativeButton("Cancel",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            dialog.cancel();
                                        }
                                    });

                    AlertDialog alertDialog = builder.create();

                    alertDialog.show();

                    startLocationUpdates();
                }
            }
        });

        image.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                dispatchTakeFullPictureIntent();
            }
        });

        track.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (trackRoute) {
                    trackRoute = false;
                    Toast.makeText(getApplicationContext(), "Tracking Stopped", Toast.LENGTH_SHORT).show();
                } else {
                    trackRoute = true;
                    Toast.makeText(getApplicationContext(), "Tracking Started", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    //https://developer.android.com/training/camera/photobasics.html
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        junkObject.setPhotoPath(image.getAbsolutePath());
        return image;
    }

    //https://developer.android.com/training/camera/photobasics.html
    private void dispatchTakeFullPictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.example.android.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
                photoUri = photoURI;
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }

    public void initSavedData() {
        try {
            FileInputStream fis = openFileInput(FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            junkObject = (JunkObject) ois.readObject();
            fis.close();
            ois.close();
            markerText = junkObject.getMarkerString();
            junk.setLatitude(junkObject.getMarkerLat());
            junk.setLongitude(junkObject.getMarkerLon());
            latArray = junkObject.getPolyArrayLat();
            lonArray = junkObject.getPolyArrayLon();
            if (junkObject.getPhotoPath() != null) {
                File imageFile = new File(junkObject.getPhotoPath());
                if (imageFile.exists()) {
                    Bitmap myBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath());
                    imageView.setImageBitmap(myBitmap);
                }
            }
            for (int i = 0; i < latArray.size(); i++) {
                polylineOptions.add(new LatLng(latArray.get(i), lonArray.get(i)));
            }
            mMap.addMarker(new MarkerOptions().position(new LatLng(junk.getLatitude(), junk.getLongitude())).title(markerText));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(junk.getLatitude(), junk.getLongitude()), 17.0f));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.out.println("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO Exception");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.out.println("Class Not Found");
        }

    }

    public void init() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        latArray = new ArrayList<Double>();
        lonArray = new ArrayList<Double>();
        marker = (Button) findViewById(R.id.markerButton);
        image = (Button) findViewById(R.id.captureImage);
        track = (Button) findViewById(R.id.trackButton);
        imageView = (ImageView) findViewById(R.id.mImageView);
        junkObject = new JunkObject();
        junkObject.setMarkerString("");
        junkObject.setMarkerLat(0);
        junkObject.setMarkerLon(0);
        junkObject.setPolyArrayLat(latArray);
        junkObject.setPolyArrayLon(lonArray);
        junkObject.setPhotoPath(null);
        junk = new Location("");
        trackRoute = false;
        mCurrentPhotoPath = null;
        photoUri = null;
    }


}
