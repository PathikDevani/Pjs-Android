package com.pjs;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Pathik on 12/10/2015.
 */
public class AndroidUtils {

    public Context context;
    public AssetManager manager;


    private String ROOT_DIR = "pjs";

    public  AndroidUtils(Context context){
        this.context = context;
        manager = context.getAssets();
    }

    public void setRootDir(String dir){
        ROOT_DIR = dir;
    }


    public InputStream getFileInputStream(String path){
        try {
            return manager.open(ROOT_DIR + "/"+ path);
        } catch (IOException e) {
            return null;
        }
    }



    public boolean isFileExist(String path){
        try {
            manager.open(ROOT_DIR +"/"+path);
            return true;
        } catch (IOException e) {
            return  false;
        }
    }



}
