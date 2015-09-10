package com.filemanager;

import com.launcher.Launcher;
import com.launcher.NetworkParameters;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Class to save filemanager in a text file
 *
 * @author Fran Carpio
 */
public class WriteFile {

    protected File file;
    protected FileWriter writer;

    public WriteFile(String fileName, boolean withDate)
            throws IOException {

        SimpleDateFormat MY_FORMAT = new SimpleDateFormat(
                "dd-MM-yyyy_HH-mm-ss", Locale.getDefault());
//		Date date = new Date();

        String path = WriteFile.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String folder = path + "/../" + Launcher.getDistributionTypeForHT() + "-" + Launcher.getAlgorithm() + "-" + Launcher.isWithGuardBands() + "-" + Launcher.getNumberOfCarriersPerLink() + "-" + Launcher.getMeanHoldingTime() + "_" + MY_FORMAT.format(Launcher.getDate());
        new File(folder).mkdir();

        file = new File(folder + "/" + fileName + ".txt");
        writer = new FileWriter(file, false);
    }

    public void write(String content) {
        try {
            writer = new FileWriter(file, true);
            PrintWriter printer = new PrintWriter(writer);
            printer.write(content);
            printer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
