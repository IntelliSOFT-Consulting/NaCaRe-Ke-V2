package com.intellisoft.hai.navigation_drawer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;
import com.intellisoft.hai.auth.Login;
import com.intellisoft.hai.helper_class.DbSaveDataEntry;
import com.intellisoft.hai.helper_class.FileUpload;
import com.intellisoft.hai.helper_class.FormatterClass;
import com.intellisoft.hai.helper_class.NavigationValues;
import com.intellisoft.hai.R;
import com.intellisoft.hai.helper_class.SettingsQueue;
import com.intellisoft.hai.helper_class.SubmissionsStatus;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentAbout;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentDataEntry;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentHelpDesk;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentHome;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentSetPin;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentSettings;
import com.intellisoft.hai.navigation_drawer.fragments.FragmentSubmission;
import com.intellisoft.hai.network_request.RetrofitCalls;
import com.intellisoft.hai.room.MainViewModel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPOutputStream;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_IMAGE_PICKER = 1001;
    private static final int REQUEST_CAMERA_AND_STORAGE_PERMISSION_CODE = 1200;
    private static final int REQUEST_IMAGE_CAPTURE = 1002;
    private static final int FILE_SELECT_CODE = 1003;
    private String[] mNavigationDrawerItemTitles;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private Toolbar toolbar;
    private CharSequence mTitle;
    private RetrofitCalls retrofitCalls = new RetrofitCalls();
    ActionBarDrawerToggle mDrawerToggle;
    private FormatterClass formatterClass = new FormatterClass();
    private MainViewModel myViewModel;
//    private List<Image> imageList;
    private Boolean isCamera = false;
    private Boolean isPdf = false;


    private BroadcastReceiver myBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("dhis2")) {
                // Call your function here
                autoSyncSubmissions(true);
                updateSubmittedSynced();
            }
        }
    };

    private void updateSubmittedSynced() {

        if (activeInternetConnection()) {
            try {
//                List<Submissions> submissionList = myViewModel.getSyncedSubmissions(this, SubmissionsStatus.SUBMITTED.name());
//                for (Submissions sm : submissionList) {
//                    DbSaveDataEntry dataEntry = myViewModel.getSubmitSync(this, sm);
//                    if (dataEntry != null) {
//                        retrofitCalls.submitSyncData(this, dataEntry, sm, myViewModel);
//
//                    }
//                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    private void autoSyncSubmissions(Boolean confirm) {

        if (activeInternetConnection()) {
            loadAllImages();
            Calendar calendar = Calendar.getInstance();
            int hour = calendar.get(Calendar.HOUR_OF_DAY);

            if (confirm) {
                String code = formatterClass.getSharedPref(SettingsQueue.SYNC.name(), MainActivity.this);
                try {
                    if (code != null && code.equalsIgnoreCase("Manual")) {
                        Log.e("TAG", "Setting updated to manual.....");
                    } else {
//                        List<Submissions> submissionList = myViewModel.getUnsyncedSubmissions(this, SubmissionsStatus.SUBMITTED.name());
//                        for (Submissions sm : submissionList) {
//                            DbSaveDataEntry dataEntry = myViewModel.getSubmitSync(this, sm);
//                            if (dataEntry != null) {
//                                retrofitCalls.submitSyncData(this, dataEntry, sm, myViewModel);
//
//                            }
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e(TAG, "Error.... Exception" + e.getMessage());
                }
            } else {
//                List<Submissions> submissionList = myViewModel.getUnsyncedSubmissions(this, SubmissionsStatus.SUBMITTED.name());
//                for (Submissions sm : submissionList) {
//                    DbSaveDataEntry dataEntry = myViewModel.getSubmitSync(this, sm);
//                    if (dataEntry != null) {
//                        retrofitCalls.submitSyncData(this, dataEntry, sm, myViewModel);
//                    }
//                }
            }
        } else {
            Log.e(TAG, "Sync paused.... No active connection");
        }
    }

    private boolean activeInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(myBroadcastReceiver);
        } catch (Exception e) {

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        imageList = new ArrayList<>();
        IntentFilter intentFilter = new IntentFilter("dhis2");
        registerReceiver(myBroadcastReceiver, intentFilter);
        myViewModel = new MainViewModel(((Application) this.getApplicationContext()));
        mDrawerLayout = findViewById(R.id.drawer_layout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                // Do something when the drawer is opened
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        NavigationView navigationView = findViewById(R.id.nav_view);
        // Set the listener for menu item clicks
        navigationView.setNavigationItemSelectedListener(this);
        loadAllImages();
//        throw new RuntimeException("Test Crash");
    }


    private void loadAllImages() {
        try {
//            imageList = myViewModel.getAllImages(MainActivity.this, false);
//            for (Image img : imageList) {
//                retrofitCalls.submitFileData(MainActivity.this, img, myViewModel);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        String navigation = formatterClass.getSharedPref(NavigationValues.NAVIGATION.name(), this);
        if (navigation == null) {
            selectItem(0);
        } else {
            if (navigation.equals(NavigationValues.HOME.name())) {
                selectItem(0);
            } else if (navigation.equals(NavigationValues.SUBMISSION.name())) {
                selectItem(7);
            } else if (navigation.equals(NavigationValues.DATA_ENTRY.name())) {
                selectItem(8);
            } else {
                selectItem(0);
            }
        }


    }

    public void selectImage(String userId, String indicatorId, String submissionId) {

        formatterClass.saveSharedPref(FileUpload.USER.name(), userId, MainActivity.this);
        formatterClass.saveSharedPref(FileUpload.INDICATOR.name(), indicatorId, MainActivity.this);
        formatterClass.saveSharedPref(FileUpload.SUBMISSION.name(), submissionId, MainActivity.this);

        CharSequence[] options = new CharSequence[]{"Take Photo", "Choose from Gallery", "Cancel"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Option");
        builder.setItems(options, (dialog, item) -> {
            switch (item) {
                case 0:
                    // Handle take photo option
                    isCamera = true;
                    isPdf = false;
                    launchCamera();
                    break;
                case 1:
                    // Handle choose from gallery option
                    isCamera = false;
                    isPdf = false;
                    launchCamera();
                    break;
//                case 2:
//                    isCamera = false;
//                    isPdf = true;
//                    launchCamera();
//                    break;
                case 2:
                    dialog.dismiss();
                    break;
            }
        });
        builder.show();
//
    }

    private void launchCamera() {
        String cameraPermission = Manifest.permission.CAMERA;
        String storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
        String readStoragePermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        List<String> permissionsList = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, cameraPermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(cameraPermission);
        }
        if (ContextCompat.checkSelfPermission(this, storagePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(storagePermission);
        }
        if (ContextCompat.checkSelfPermission(this, readStoragePermission) != PackageManager.PERMISSION_GRANTED) {
            permissionsList.add(readStoragePermission);
        }
        if (!permissionsList.isEmpty()) {
            String[] permissionsArray = permissionsList.toArray(new String[permissionsList.size()]);
            ActivityCompat.requestPermissions(this, permissionsArray, REQUEST_CAMERA_AND_STORAGE_PERMISSION_CODE);
        } else {
            // Permissions have already been granted
            openLauncher(isCamera, isPdf);
        }

    }

    private void openLauncher(boolean isCamera, boolean isPdf) {
        if (isPdf) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("application/pdf");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            // Start the file picker activity
            startActivityForResult(Intent.createChooser(intent, "Select a PDF file"), FILE_SELECT_CODE);

        } else {
            if (isCamera) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            } else {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE_PICKER);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_AND_STORAGE_PERMISSION_CODE) {
            Map<String, Integer> permissionResults = new HashMap<>();
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    // Add the permission to the map with a value of PERMISSION_DENIED
                    permissionResults.put(permission, PackageManager.PERMISSION_DENIED);
                } else {
                    // Add the permission to the map with a value of PERMISSION_GRANTED
                    permissionResults.put(permission, PackageManager.PERMISSION_GRANTED);
                }
            }

            // Check if all permissions were granted
            if (permissionResults.containsValue(PackageManager.PERMISSION_DENIED)) {
                // At least one permission was denied
                // Handle the denied permission(s) as appropriate
            } else {
                // All permissions were granted
                // Proceed with the action that requires the permissions
                launchCamera();
            }
        }
    }

    private File getFileFromUri(Uri uri) throws IOException {
        File file = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);
            if (cursor.moveToFirst()) {
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
                String fileName = cursor.getString(column_index);
                InputStream inputStream = getContentResolver().openInputStream(uri);
                file = new File(getCacheDir(), fileName);
                FileOutputStream outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[4 * 1024];
                int read;
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();
            }
            cursor.close();
        } else if ("file".equals(uri.getScheme())) {
            String filePath = uri.getPath();
            file = new File(filePath);
        }

        return file;
    }


    private byte[] compressFile(File file) {
        byte[] bytes = null;

        try {
            // Create a FileInputStream to read the file
            FileInputStream inputStream = new FileInputStream(file);

            // Create a ByteArrayOutputStream to hold the compressed data
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            // Create a GZIPOutputStream to compress the data
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(outputStream);

            // Create a buffer to hold the data read from the file
            byte[] buffer = new byte[4096];
            int bytesRead = -1;

            // Read the file into the buffer and write it to the GZIPOutputStream
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                gzipOutputStream.write(buffer, 0, bytesRead);
            }

            // Close the streams
            gzipOutputStream.close();
            outputStream.close();
            inputStream.close();

            // Get the compressed data as a byte array
            bytes = outputStream.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return bytes;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            try {
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                imageBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // specify the compression quality
                byte[] imageBytes = byteArrayOutputStream.toByteArray();
                uploadImage(imageBytes, "Cam" + randomNumber() + ".jpg", true);

            } catch (Exception e) {
                e.printStackTrace();
                Log.e("Sample Tag", "Error Image::::" + e.getMessage());
                Toast.makeText(this, "Error while processing image", Toast.LENGTH_LONG).show();
            }

        }
        if (requestCode == FILE_SELECT_CODE && resultCode == RESULT_OK && data != null) {
            Log.e(TAG, "onActivityResult");
            try {
                Uri uri = data.getData();

                File file = getFileFromUri(uri);
                String fileName = file.getName();
                byte[] bytes = compressFile(file);
                uploadImage(bytes, fileName, false);
            } catch (Exception e) {
                Log.e(TAG, "onActivityResult::::" + e.getMessage());
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if (requestCode == REQUEST_IMAGE_PICKER && resultCode == Activity.RESULT_OK && data != null) {
            // Get the selected image URI
            Uri selectedImageUri = data.getData();
            String fileName = "";

            try {
                ContentResolver cr = getContentResolver();
                InputStream inputStream = cr.openInputStream(selectedImageUri);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 4; // specify the image compression ratio, higher the ratio smaller the image size
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);

                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream); // specify the compression quality
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                String[] projection = {MediaStore.Images.Media.DISPLAY_NAME};
                Cursor cursor = cr.query(selectedImageUri, projection, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME));
                    // Use the file name as needed
                    cursor.close();
                }
                Log.e(TAG, "Received image from camera" + imageBytes);
                uploadImage(imageBytes, fileName, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String randomNumber() {
        Random random = new Random();
        int min = 10;
        int max = 1000;
        int randomNumber = random.nextInt(max - min + 1) + min; // generate a random number between 10 and 1000
        return "" + randomNumber;
    }

    private void uploadImage(byte[] imageBytes, String fileName, boolean isImage) {

        String userId = formatterClass.getSharedPref(FileUpload.USER.name(), this);
        String indicatorId = formatterClass.getSharedPref(FileUpload.INDICATOR.name(), this);
        String submissionId = formatterClass.getSharedPref(FileUpload.SUBMISSION.name(), this);
//        Image image = new Image(
//                userId,
//                submissionId,
//                indicatorId,
//                imageBytes, fileName, "", false, isImage);
//        myViewModel.uploadImage(MainActivity.this, image);
    }

    private void selectItem(int position) {

        formatterClass.deleteSharedPref(NavigationValues.NAVIGATION.name(), this);
        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new FragmentHome();
                break;
            case 1:
                fragment = new FragmentSettings();
                break;
            case 2:
                fragment = new FragmentSetPin();
                break;
            case 3:
                //Log out
                formatterClass.deleteSharedPref("isLoggedIn", this);
                Intent intent = new Intent(this, Login.class);
                startActivity(intent);
                finish();
                break;
            case 4:
                autoSyncSubmissions(false);
                break;
            case 5:
                fragment = new FragmentAbout();
                break;
            case 6:
                fragment = new FragmentHelpDesk();
                break;
            case 7:
                fragment = new FragmentSubmission();
                break;
            case 8:
                fragment = new FragmentDataEntry();
                break;

            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment)
                    .addToBackStack(null)
                    .commit();
            mDrawerLayout.closeDrawer(GravityCompat.START);

        } else {
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle menu item clicks here
        int itemId = item.getItemId();
        if (itemId == R.id.nav_home) {
            // Handle Home click
            selectItem(0);
        } else if (itemId == R.id.nav_settings) {
            // Handle Gallery click
            selectItem(1);
        } else if (itemId == R.id.nav_set_pin) {
            // Handle Settings click
            selectItem(2);
        } else if (itemId == R.id.nav_logout) {
            // Handle Settings click
            selectItem(3);
        } else if (itemId == R.id.nav_about) {
            // Handle Settings click
            selectItem(5);
        } else if (itemId == R.id.nav_help) {
            // Handle Settings click
            selectItem(6);
        } else if (itemId == R.id.nav_sync) {
            // Handle Settings click
            selectItem(4);
        }

        // Close the navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }
}