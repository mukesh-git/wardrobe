package com.mukeshteckwani.crowdfire.wardrobe.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;

import com.mukeshteckwani.crowdfire.wardrobe.R;
import com.mukeshteckwani.crowdfire.wardrobe.databinding.ActivityMainBinding;
import com.mukeshteckwani.crowdfire.wardrobe.util.PermissionRequestHandler;
import com.mukeshteckwani.crowdfire.wardrobe.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;

import static com.mukeshteckwani.crowdfire.wardrobe.util.Utils.IMAGE_FILE_PATH;

public class MainActivity extends AppCompatActivity implements DialogInterface.OnClickListener {

    private ActivityMainBinding binding;
    public static final int PANT = 0;
    public static final int SHIRT = 1;
    public static final int GALLERY_CAPTURE = 2;
    public static final int CAMERA_CAPTURE = 3;
    private int sourceClicked;
    private Uri imageUri;
    private ImagePagerAdapter adapterPant;
    private ImagePagerAdapter adapterShirt;
    private ArrayList<String> pantImages = new ArrayList<>();
    private ArrayList<String> shirtImages = new ArrayList<>();
    private static String mOption;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setCallback(this);
        getImagesFromStorage();
        initPager();
    }

    private void getImagesFromStorage() {
        File shirtImagesDir = new File(IMAGE_FILE_PATH + "/SHIRT");
        File pantImagesDir = new File(IMAGE_FILE_PATH + "/PANT");
        File[] pantImgList = pantImagesDir.listFiles();
        File[] shirtImgList = shirtImagesDir.listFiles();

        if(pantImgList != null && pantImgList.length != 0)
        addImagesToList(pantImages,pantImgList);

        if(shirtImgList != null && shirtImgList.length != 0)
        addImagesToList(shirtImages,shirtImgList);
    }

    private void addImagesToList(ArrayList<String> images, File[] imageFiles) {
        if(images == null)
            images = new ArrayList<>();
        for (File imagePath : imageFiles) {
            images.add(imagePath.getAbsolutePath());
        }
    }

    private void initPager() {
        adapterPant = new ImagePagerAdapter(this, pantImages);
        adapterShirt = new ImagePagerAdapter(this, shirtImages);
        binding.vpPant.setAdapter(adapterPant);
        binding.vpShirt.setAdapter(adapterShirt);
    }

    public void onClickShuffle(View v) {
        File shirtImagesDir = new File(IMAGE_FILE_PATH + "/SHIRT");
        File pantImagesDir = new File(IMAGE_FILE_PATH + "/PANT");
        File[] pantImgList = pantImagesDir.listFiles();
        File[] shirtImgList = shirtImagesDir.listFiles();
        Random rand = new Random();
        if(pantImgList.length != 0 && shirtImgList.length != 0) {
            binding.vpPant.setCurrentItem(rand.nextInt(pantImgList.length),true);
            binding.vpShirt.setCurrentItem(rand.nextInt(shirtImgList.length),true);
        }
    }

    public void onClickAddPant(View v) {
        showAddImagesPopup(PANT);
    }

    public void onClickAddShirt(View v) {
        showAddImagesPopup(SHIRT);
    }

    private void showAddImagesPopup(int option) {
        AlertDialog.Builder builderSingle = new AlertDialog.Builder(this);
        builderSingle.setTitle("Upload Photo");
        final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_item);
        arrayAdapter.add("  Camera");
        arrayAdapter.add("  Gallery");
        if(option == PANT)
            mOption = "PANT";
        else
            mOption = "SHIRT";

        builderSingle.setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (arrayAdapter.getItem(i).trim().equals("Gallery")) {
                    sourceClicked = GALLERY_CAPTURE;
                } else {
                    sourceClicked = CAMERA_CAPTURE;
                }
                checkForWriteStoragePermission();

            }
        }).show();
    }

    private void checkForWriteStoragePermission() {
        int storagePermission = PermissionRequestHandler.checkRequestPermission(this, PermissionRequestHandler.WRITE_EXTERNAL_STORAGE_PERMISSION);
        int cameraPermission = PermissionRequestHandler.checkRequestPermission(this, PermissionRequestHandler.CAMERA_PERMISSION);
        if (storagePermission == PackageManager.PERMISSION_GRANTED && cameraPermission == PackageManager.PERMISSION_GRANTED) {
            openImageSource();
        } else {
            if (storagePermission == PackageManager.PERMISSION_DENIED && cameraPermission == PackageManager.PERMISSION_DENIED) { // ask for both permissions

                PermissionRequestHandler.openPermissionRequestDialog(this,
                        new String[]{PermissionRequestHandler.CAMERA_PERMISSION, PermissionRequestHandler.WRITE_EXTERNAL_STORAGE_PERMISSION},
                        PermissionRequestHandler.PERMISSIONS_REQUEST_CAMERA_STORAGE);

            } else if (cameraPermission == PackageManager.PERMISSION_DENIED) {  // camera permissions

                PermissionRequestHandler.openPermissionRequestDialog(this,
                        new String[]{PermissionRequestHandler.CAMERA_PERMISSION},
                        PermissionRequestHandler.PERMISSIONS_REQUEST_CODE_CAMERA);

            } else if (storagePermission == PackageManager.PERMISSION_DENIED) {   // storage permissions

                PermissionRequestHandler.openPermissionRequestDialog(this,
                        new String[]{PermissionRequestHandler.WRITE_EXTERNAL_STORAGE_PERMISSION},
                        PermissionRequestHandler.PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE);
            }
        }
    }

    public void openImageSource() {
        if (sourceClicked == CAMERA_CAPTURE) {
            imageUri = Utils.openCamera(this,mOption);
        } else {
            Utils.openGallery(this,mOption);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (sourceClicked == GALLERY_CAPTURE) {
            showImagesFromGallery(resultCode, data);
        } else if (sourceClicked == CAMERA_CAPTURE) {
            showImagesFromCamera();
        }
    }

    private void showImagesFromCamera() {
        if (mOption.equals("PANT")) {
            pantImages.add(Utils.getCapturedImage(this, imageUri, sourceClicked,mOption));
            adapterPant.setImages(pantImages);
            adapterPant.notifyDataSetChanged();
        }
        else {
            shirtImages.add(Utils.getCapturedImage(this, imageUri, sourceClicked, mOption));
            adapterShirt.setImages(shirtImages);
            adapterShirt.notifyDataSetChanged();
        }
    }

    private void showImagesFromGallery(int resultCode, Intent data) {
        if (resultCode == RESULT_OK
                && null != data) {
            if (mOption.equals("PANT")) {
                pantImages.add(Utils.getCapturedImage(this, data.getData(), sourceClicked,mOption));
                adapterPant.setImages(pantImages);
                adapterPant.notifyDataSetChanged();
            }
            else {
                shirtImages.add(Utils.getCapturedImage(this, data.getData(), sourceClicked,mOption));
                adapterShirt.setImages(shirtImages);
                adapterShirt.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case PermissionRequestHandler.PERMISSIONS_REQUEST_CAMERA_STORAGE: // both permissions are granted

                // both permissions are granted
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    openImageSource();

                    // only  camera granted
                } else if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_DENIED) {
                    Utils.createPermissionDialog(this, getString(R.string.dialog_permissions), getString(R.string.dialog_permission_storage), getString(R.string.dialog_got_sto_settings), getString(R.string.cancel), this);

                    // only storage granted
                } else if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_DENIED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    Utils.createPermissionDialog(this, getString(R.string.dialog_permissions), getString(R.string.dialog_permission_camera), getString(R.string.dialog_got_sto_settings), getString(R.string.cancel), this);
                } else {
                    Utils.createPermissionDialog(this, getString(R.string.dialog_permissions), getString(R.string.dialog_permission_camera_storage), getString(R.string.dialog_got_sto_settings), getString(R.string.cancel), this);
                }

                break;
            case PermissionRequestHandler.PERMISSIONS_REQUEST_CODE_CAMERA:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSource();

                } else {

                    Utils.createPermissionDialog(this, getString(R.string.dialog_permissions), getString(R.string.dialog_permission_camera), getString(R.string.dialog_got_sto_settings), getString(R.string.cancel), this);
                }
                break;
            case PermissionRequestHandler.PERMISSIONS_REQUEST_CODE_WRITE_EXTERNAL_STORAGE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openImageSource();

                } else {
                    Utils.createPermissionDialog(this, getString(R.string.dialog_permissions), getString(R.string.dialog_permission_storage), getString(R.string.dialog_got_sto_settings), getString(R.string.cancel), this);
                }
                break;
        }
    }


    public void onClickFavourite(View v) {

    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == android.app.Dialog.BUTTON_POSITIVE) {
            PermissionRequestHandler.openPermissionSettingsScreen(this);
        }
        dialog.dismiss();
    }
}
