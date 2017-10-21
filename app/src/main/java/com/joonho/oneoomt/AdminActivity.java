package com.joonho.oneoomt;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.os.FileUriExposedException;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.joonho.oneoomt.db.DBGateway;
import com.joonho.oneoomt.file.myActivity;
import com.joonho.oneoomt.file.myPicture;
import com.joonho.oneoomt.util.FileUtils;
import com.joonho.oneoomt.util.PhotoUtil;
import com.joonho.oneoomt.util.modifiedDate;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;

import static com.joonho.oneoomt.util.PhotoUtil.myPictureList;

public class AdminActivity extends AppCompatActivity implements View.OnClickListener {
    private static String TAG = "AdminActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        Button bt1 = (Button)findViewById(R.id.bt_admin_back);
        bt1.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()) {
            case R.id.imageButton_copy:
                copyPICstoPIC_DIR();
                break;
            case R.id.imageButton_copy2:
                copyPIC_DIRtoPICS();
                break;
            case R.id.imageButton_rebuild_master:
                rebuildMaster(false); //do not copy all files
                break;
            case R.id.imageButton_copyPICstoGallery:
                copyPICstoGallery();
                break;
            case R.id.imageButton_manage_internal_activities:
                manage_internal_activities();
                break;
            case R.id.imageButton_manage_external_activities:
                manage_external_activities();
                break;
            case R.id.imageButton_validationcheck4pics:
                validation_check4pics();
                break;
            case R.id.bt_admin_back:
                finish();
                break;
            default:
                break;
        }
    }

    private void validation_check4pics() {
        PhotoUtil.validatePictures();
        PhotoUtil pu = new PhotoUtil();
        pu.saveMyPictueList();
    }

    public void copyPICstoGallery() {
        manageActivities(false);
    }

    public void manage_internal_activities() {
        manageActivities(false);
    }

    public void manage_external_activities() {
        manageActivities(true);
    }

    public void manageActivities(boolean external) {
        final File folderTarget;
        final File folder;

        if(!external) {
            folder = new File(getApplicationContext().getFilesDir(), "OneOOMT" );
            folderTarget = getExternalFilesDir("OneOOMT");
        } else  {
            folder = getExternalFilesDir("OneOOMT");
            folderTarget = new File(getApplicationContext().getFilesDir(), "OneOOMT" );
        }

        if(!folder.exists()) { folder.mkdir(); }
        if(!folderTarget.exists()) folderTarget.mkdirs();
        final File[] files = folder.listFiles();
        Arrays.sort(files, new modifiedDate());
        Log.e(TAG, "# of Activities: " + files.length);

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("Manage Activities");


        final CharSequence items[] = new CharSequence[files.length];
        final String filepath[] = new String[files.length];

        for(int i=0;i<files.length;i++) {
            items[i] = files[i].getName();
            filepath[i] = files[i].getAbsolutePath();
        }

        final boolean checkedItems[] = new boolean[files.length];
        //alertDialog.setItems(items, null);
        alertDialog.setMultiChoiceItems(items, checkedItems, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                Toast.makeText(AdminActivity.this, "" + i + " " + b, Toast.LENGTH_LONG).show();
            }
        });

        for(int i=files.length-1;i>=0;i--) {
            Log.e(TAG,"" + files[i].getAbsolutePath());
        }

        alertDialog.setPositiveButton("View", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int choice = 0;
                if(filepath.length ==0) return;

                // 선택된 첫번째 것만 뷰잉함
                for(;choice <checkedItems.length; choice++) {
                    if(checkedItems[choice]) break;
                }

                ArrayList<myActivity> list = deserializeActivities(filepath[choice]);
                if(list == null) {return;}
                if(list.size()==0) {return;}

                double lat[] = new double[list.size()];
                double lon[] = new double[list.size()];
                ArrayList<String> ado = new ArrayList<String>();

                for(int x=0;x<list.size();x++) {
                    lat[x] = ((myActivity)(list.get(x))).latitude;
                    lon[x] = ((myActivity)(list.get(x))).longitude;
                    ado.add(((myActivity)(list.get(x))).added_on);
                }

                Log.e(TAG, ">>>> AdminActivirtywill call HistoryGoogleActivity ! ");

                Intent intent = new Intent(AdminActivity.this, HistoryGoogleMapsActivity.class);
                intent.putExtra("latitudes", lat);
                intent.putExtra("longitudes", lon);
                intent.putStringArrayListExtra("added_ons", ado);
                intent.putExtra("fname",filepath[choice]);
                startActivity(intent);
            }
        });


        alertDialog.setNegativeButton("Delete",new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int x=0;x<checkedItems.length;x++) {
                    if(checkedItems[x]) {
                        File srcf = files[x];
                        srcf.delete();
                    }
                }
            }
        });

        String sync_str = (external==true)? "Sync" : "Sync";
        alertDialog.setNeutralButton(sync_str, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for(int x=0;x<checkedItems.length;x++) {
                    if(checkedItems[x]) {
                        File srcf = files[x];
                        File tarf = new File(folderTarget, srcf.getName());
                        try {
                            FileUtils.copyFileUsingFileStreams(srcf, tarf);
                        }catch(Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, e.toString());
                        }
                    }
                }
            }
        });

        AlertDialog alert = alertDialog.create();
        alert.show();
    }

    private void rebuildMaster(boolean copy_files) {
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        String fileName = "myPicture.master";
        File file = new File(storageDir, fileName);
        File bakFile = new File(storageDir, fileName +".Bak");
        try {
            FileUtils.copyFileUsingFileStreams(file, bakFile);
        }catch (Exception e) {
            Log.e(TAG, e.toString());
        }

        Log.e(TAG, "My Picture Master File:" + file.toString());

        try {
            FileOutputStream fos = new FileOutputStream(file);
            BufferedOutputStream bos = new BufferedOutputStream(fos);
            ObjectOutputStream out = new ObjectOutputStream(bos);

            int i=0;
            while(i < myPictureList.size()) {
                myPicture mp = myPictureList.get(i);
                File f2 = new File(mp.filepath);
                File f2_tar = new File(storageDir, f2.getName());

                if(copy_files) FileUtils.copyFileUsingFileStreams(f2, f2_tar);

                mp.filepath = f2_tar.getAbsolutePath();
                out.writeObject(mp);
                Log.e(TAG, "" + i + "th Picture(" + mp.toString() + ") OK!" );
                i++;
            }
            out.close();
            Log.e(TAG, "\n\n saveMyPictureList() called!!\n\n");
        }catch(Exception e) {
            e.printStackTrace();
            Log.e(TAG, e.toString());
        }
    }

    public void copyPICstoPIC_DIR() {
        Toast.makeText(getApplicationContext(),"copyPICstoPIC_DIR begin", Toast.LENGTH_LONG).show();

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
        File flist[] = mediaStorageDir.listFiles();
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        for(int i=0;i<flist.length;i++) {
            File targetFile = new File(storageDir, flist[i].getName());
            try {

                FileUtils.copyFileUsingFileStreams(flist[i], targetFile);
                String sinfo = "" + targetFile.toString() + "Copy done!";

                Toast.makeText(getApplicationContext(), sinfo, Toast.LENGTH_SHORT).show();
                Log.e(TAG, sinfo);
            }catch(Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

        }
        Toast.makeText(getApplicationContext(),"copyPICstoPIC_DIR end", Toast.LENGTH_LONG).show();
    }

    public void copyPIC_DIRtoPICS() {
        Toast.makeText(getApplicationContext(),"copyPIC_DIRtoPICs begin", Toast.LENGTH_LONG).show();

        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "MyCameraApp");
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File flist[] = storageDir.listFiles();

        for(int i=0;i<flist.length;i++) {
            File targetFile = new File(mediaStorageDir, flist[i].getName());
            try {

                FileUtils.copyFileUsingFileStreams(flist[i], targetFile);
                String sinfo = "" + targetFile.toString() + "Copy done!";

                Toast.makeText(getApplicationContext(), sinfo, Toast.LENGTH_SHORT).show();
                Log.e(TAG, sinfo);
            }catch(Exception e) {
                e.printStackTrace();
                Log.e(TAG, e.toString());
            }

        }
        Toast.makeText(getApplicationContext(),"copyPIC_DIRtoPICs end", Toast.LENGTH_LONG).show();
    }

    public ArrayList<myActivity> deserializeActivities(String filepath) {
        File file = new File(filepath);
        Log.e(TAG, "********* ActivityFileName to be read: " + file.getAbsolutePath());

        FileInputStream fis = null;
        BufferedInputStream bis = null;
        ObjectInputStream in = null;

        ArrayList list = null;
        try {
            fis = new FileInputStream(file);
            bis = new BufferedInputStream(fis);
            in = new ObjectInputStream(bis);

            list = new ArrayList<myActivity>();
            myActivity ma=null;

            do {
                try {
                    ma = (myActivity) in.readObject();
                    list.add(ma);
                }catch(Exception ex) {
                    if(list != null) return list;
                }
            } while(ma != null);
            return list;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) in.close();
                if (bis !=null) in.close();
                if (fis !=null) fis.close();

                if(list.size()==0) {
                    Log.e(TAG, "File ("+ filepath +") corrupted !!!!");
                    file.delete();
                    Log.e(TAG, "File ("+ filepath +") deleted  !!!!");

                }
            }catch(Exception e) {}
        }
        return null;
    }
}
