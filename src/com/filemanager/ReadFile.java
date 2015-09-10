package com.filemanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by Fran on 4/2/2015.
 */
public class ReadFile {

    private static Scanner input;
    public ReadFile(String filename){

        String path = ReadFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(path+"/../" + filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        input = new Scanner (stream);
    }

    public static String readLine(){

        if(input.hasNext())
            return input.nextLine();
        else
            return null;
    }
}
