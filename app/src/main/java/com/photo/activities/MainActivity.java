package com.photo.activities;

import android.Manifest;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.photo.Photo;
import com.photo.R;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity {

    private View photo_group;
    private View mobile_group;
    private View delete_group;
    private View cancel_group;
    private Dialog dialog;
    private ImageView ico;
    private ImageView ico_box;
    private ImageView ico_big;
    private final int CAMERA_CAPTURE = 1;
    private final int PIC_CROP = 2;
    static final int GALLERY_REQUEST = 3;
    private Uri picUri;
    private static long back_pressed;
    private File photoFile;
    private static final String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private boolean cropBig = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setCancelable(true);
        LinearLayout view = (LinearLayout) getLayoutInflater()
                .inflate(R.layout.dialog_photo, null);
        adb.setView(view);
        photo_group = view.findViewById(R.id.photo_group);
        mobile_group = view.findViewById(R.id.mobile_group);
        delete_group = view.findViewById(R.id.delete_group);
        cancel_group = view.findViewById(R.id.cancel_group);
        dialog = adb.create();

        ico = (ImageView) findViewById(R.id.ico);
        ico_box = (ImageView) findViewById(R.id.ico_box);
        ico_big = (ImageView) findViewById(R.id.ico_big);

        ico.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        ico_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        ico_big.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        View button_change = findViewById(R.id.button_change);
        button_change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.show();
            }
        });

        photo_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                        (checkSelfPermission(Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED ||
                                checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        != PackageManager.PERMISSION_GRANTED)) {
                    requestPermissions(PERMISSIONS, 100);
                } else {
                    cropBig = false;
                    try {
                        // Намерение для запуска камеры
                        startCameraIntent();
                    } catch (Exception e) {
                        Toast toast = Toast
                                .makeText(getApplicationContext(), getResources().getString(R.string.text_error_camera), Toast.LENGTH_LONG);
                        toast.show();
                    }
                }

                dialog.dismiss();
            }
        });

        mobile_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cropBig = false;
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                startActivityForResult(photoPickerIntent, GALLERY_REQUEST);

                dialog.dismiss();
            }
        });

        delete_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ico.setImageResource(R.drawable.user);
                ico_box.setImageResource(R.drawable.user);
                ico_big.setImageResource(R.drawable.user);

                dialog.dismiss();
            }
        });

        cancel_group.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == 100) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                try {
                    // Намерение для запуска камеры
                    startCameraIntent();
                } catch (Exception e) {
                    Toast toast = Toast
                            .makeText(getApplicationContext(), getResources().getString(R.string.text_error_camera), Toast.LENGTH_LONG);
                    toast.show();
                }

            } else {
                Toast toast = Toast
                        .makeText(getApplicationContext(), getResources().getString(R.string.text_error_camera), Toast.LENGTH_LONG);
                toast.show();
            }
            return;
        }
    }

    //Ответ от камеры
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            // Вернулись от приложения Камера
            if (requestCode == CAMERA_CAPTURE) {
                // Получим Uri снимка
                if (picUri == null) {
                    picUri = data.getData();

                    if (picUri == null) {
                        try {
                            picUri = (Uri) data.getExtras().get("data");
                        } catch (Exception e) {
                            Bitmap thumbnailBitmap = (Bitmap) data.getExtras().get("data");
                            ico.setImageBitmap(thumbnailBitmap);
                            ico_box.setImageBitmap(thumbnailBitmap);
                            ico_big.setImageBitmap(thumbnailBitmap);
                        }
                    }
                }
                if (picUri != null) {
                    try {
                        performCrop();
                    } catch (ActivityNotFoundException anfe) {
                        Bitmap bitmap = getDecodeBitmap(picUri);
                        if (bitmap != null) {
                            ico.setImageBitmap(bitmap);
                            ico_box.setImageBitmap(bitmap);
                            ico_big.setImageBitmap(bitmap);
                        } else {
                            ico.setImageResource(R.drawable.user);
                            ico_box.setImageResource(R.drawable.user);
                            ico_big.setImageResource(R.drawable.user);
                        }
                    }
                }
                // Вернулись из операции кадрирования
            } else if(requestCode == PIC_CROP){
                Bundle extras = data.getExtras();
                Bitmap bitmap;
                if(extras == null) {
                    bitmap = getDecodeBitmap(data.getData());
                }else {
                    try {
                        // Получим кадрированное изображение
                        bitmap = extras.getParcelable("data");
                    } catch(Exception anfe){
                        bitmap = (Bitmap) extras.get("data");
                    }
                }
                // передаём его в ImageView
                if (!cropBig) {
                    if (bitmap != null) {
                        ico.setImageBitmap(bitmap);
                        ico_box.setImageBitmap(bitmap);
                    } else {
                        ico.setImageResource(R.drawable.user);
                        ico_box.setImageResource(R.drawable.user);
                    }
                    ico_big.setImageResource(R.drawable.user);
                    cropBig = true;
                    performCropBig();
                } else {
                    if (bitmap != null) {
                        ico_big.setImageBitmap(bitmap);
                    } else {
                        ico_big.setImageResource(R.drawable.user);
                    }
                    if (photoFile != null) {
                        photoFile.delete();
                    }
                }
            } else if (requestCode == GALLERY_REQUEST) {
                Bitmap bitmap = null;
                // Получим Uri снимка
                picUri = data.getData();
                if (picUri != null) {
                    String realPath = getRealPathFromURI(picUri);

                    if(realPath != null) {
                        if (realPath.contains("http")) {
                            new getFile(realPath).execute();
                            return;
                        } else {
                            picUri = Uri.parse(realPath);
                            File in = new File(picUri.getPath());
                            try {
                                File out = createImageFile();
                                copy(in, out);
                                // кадрируем его
                                try {
                                    performCrop();
                                } catch(ActivityNotFoundException anfe){
                                    bitmap = getDecodeBitmap(picUri);
                                    if (bitmap != null) {
                                        ico.setImageBitmap(bitmap);
                                        ico_box.setImageBitmap(bitmap);
                                        ico_big.setImageBitmap(bitmap);
                                    } else {
                                        ico.setImageResource(R.drawable.user);
                                        ico_box.setImageResource(R.drawable.user);
                                        ico_big.setImageResource(R.drawable.user);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                picUri = Uri.parse(realPath);
                                try {
                                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                                if (bitmap != null) {
                                    try {
                                        File out = createImageFile();
                                        FileOutputStream fileOutputStream = new FileOutputStream(out);
                                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
                                        fileOutputStream.flush();
                                        fileOutputStream.close();
                                        // кадрируем его
                                        try {
                                            performCrop();
                                        } catch(ActivityNotFoundException anfe){
                                            if (bitmap != null) {
                                                ico.setImageBitmap(bitmap);
                                                ico_box.setImageBitmap(bitmap);
                                                ico_big.setImageBitmap(bitmap);
                                            } else {
                                                ico.setImageResource(R.drawable.user);
                                                ico_box.setImageResource(R.drawable.user);
                                                ico_big.setImageResource(R.drawable.user);
                                            }
                                        }
                                    } catch (Exception exe) {
                                        exe.printStackTrace();
                                        if (bitmap != null) {
                                            ico.setImageBitmap(bitmap);
                                            ico_box.setImageBitmap(bitmap);
                                            ico_big.setImageBitmap(bitmap);
                                        } else {
                                            ico.setImageResource(R.drawable.user);
                                            ico_box.setImageResource(R.drawable.user);
                                            ico_big.setImageResource(R.drawable.user);
                                        }
                                    }
                                } else {
                                    ico.setImageResource(R.drawable.user);
                                    ico_box.setImageResource(R.drawable.user);
                                    ico_big.setImageResource(R.drawable.user);
                                }
                            }
                        }
                    } else {
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), picUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (bitmap != null) {
                            ico.setImageBitmap(bitmap);
                            ico_box.setImageBitmap(bitmap);
                            ico_big.setImageBitmap(bitmap);
                        } else {
                            ico.setImageResource(R.drawable.user);
                            ico_box.setImageResource(R.drawable.user);
                            ico_big.setImageResource(R.drawable.user);
                        }
                    }
                }
            }
        }
    }

    // Download file from picasa
    private class getFile extends AsyncTask<Void, Void, Integer> {
        private String path;

        getFile(String path) {
            super();
            this.path = path;
        }

        @Override
        protected Integer doInBackground(Void... voids) {
            picUri = null;
            try {
                File imageFile = null;
                try {
                    imageFile = createImageFile();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    return 0;
                }
                URL aURL = new URL(path.toString());

                URLConnection conn = aURL.openConnection();
                conn.connect();
                InputStream is = conn.getInputStream();

                this.copyInputStreamToFile(is, imageFile);

                is.close();

                return HttpsURLConnection.HTTP_OK;
            } catch (Exception ex) {
                // something went wrong
                ex.printStackTrace();
                return 0;
            }
        }

        private void copyInputStreamToFile( InputStream in, File file ) {
            try {
                OutputStream out = new FileOutputStream(file);
                byte[] buf = new byte[1024];
                int len;
                while((len=in.read(buf))>0){
                    out.write(buf,0,len);
                }
                out.close();
                in.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            if(result == HttpsURLConnection.HTTP_OK) {
                // кадрируем его
                try {
                    performCrop();
                } catch(ActivityNotFoundException anfe){
                    Bitmap bitmap = getDecodeBitmap(picUri);
                    if (bitmap != null) {
                        ico.setImageBitmap(bitmap);
                        ico_box.setImageBitmap(bitmap);
                        ico_big.setImageBitmap(bitmap);
                    } else {
                        ico.setImageResource(R.drawable.user);
                        ico_box.setImageResource(R.drawable.user);
                        ico_big.setImageResource(R.drawable.user);
                    }
                }
            }
        }
    }

    // copy file
    public void copy(File src, File dst) throws IOException {
        InputStream in = new FileInputStream(src);
        OutputStream out = new FileOutputStream(dst);

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        in.close();
        out.close();
    }

    // get path from file URI
    public String getRealPathFromURI(Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } catch (Exception e) {
            return contentUri.getPath();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static Bitmap getDecodeBitmap(Uri uri) {
        // read image file
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        BitmapFactory.decodeFile(uri.getPath(), options);

        float width = options.outWidth;
        float height = options.outHeight;

        if(width > height) {
            if (width > 1024) {
                height = height / (width / 1024);
                width = 1024;
            }
        } else if(height > width) {
            if (height > 1024) {
                width = width / (height / 1024);
                height = 1024;
            }
        } else {
            if (height > 1024) {
                height = 1024;
                width = 1024;
            }
        }

        options.inSampleSize = calculateInSampleSize(options, (int)width, (int)height);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri.getPath(), options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // real image size
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private void performCrop(){
        // Намерение для кадрирования. Не все устройства поддерживают его
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(picUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 1);
        cropIntent.putExtra("outputX", 512);
        cropIntent.putExtra("outputY", 512);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, PIC_CROP);
    }

    private void performCropBig(){
        // Намерение для кадрирования. Не все устройства поддерживают его
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setDataAndType(picUri, "image/*");
        cropIntent.putExtra("crop", "true");
        cropIntent.putExtra("aspectX", 1);
        cropIntent.putExtra("aspectY", 2);
        cropIntent.putExtra("outputX", 512);
        cropIntent.putExtra("outputY", 1024);
        cropIntent.putExtra("return-data", true);
        startActivityForResult(cropIntent, PIC_CROP);
    }

    // create file
    private File createImageFile() throws IOException {
        // Create an image file name
        File storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        };
        File image = new File(storageDir.getAbsolutePath()+"/temp.jpg");
        picUri = Uri.fromFile(image);
        return image;
    }

    // start camera intent
    private void startCameraIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            // Continue only if the File was successfully created
            Uri photoUri = null;
            if (photoFile != null) {
                if(Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
                    photoUri = FileProvider.getUriForFile(MainActivity.this,
                            getApplicationContext().getPackageName() + ".provider",
                            photoFile);
                } else {
                    photoUri = Uri.fromFile(photoFile);
                }
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
            } else {
                picUri = null;
                startActivityForResult(takePictureIntent, CAMERA_CAPTURE);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (back_pressed + 2000 > System.currentTimeMillis()) {
            super.onBackPressed();

            Photo.exit(this);
        } else {
            Toast.makeText(getBaseContext(), R.string.text_again_exit,
                    Toast.LENGTH_SHORT).show();
        }
        back_pressed = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}
