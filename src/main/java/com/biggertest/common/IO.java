package com.biggertest.common;

import org.apache.tools.ant.DirectoryScanner;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class IO {

    /**
     * Find files by a glob path
     * @param baseDir the root path to search
     * @param glob glob statement
     * @return file list
     */
    public static List<String> glob(String baseDir, String glob) {
        DirectoryScanner scanner = new DirectoryScanner();
        scanner.setIncludes(new String[]{glob});
        scanner.setBasedir(baseDir);
        scanner.setCaseSensitive(false);
        scanner.scan();
        String[] paths = scanner.getIncludedFiles();
        List<String> ret = new ArrayList<>();
        for(String path : paths) {
            ret.add(baseDir + File.separator + path);
        }
        return ret;
    }

    /**
     * Find files by one path
     * @param path full path with glob
     * @return file list
     */
    public static List<String> glob(String path) {
        String[] keys = {"*", "?", "["};
        String[] params = path.split(File.separator);
        int index = -1;
        for(int i=0; i<params.length; i++) {
            String param = params[i];
            boolean bFounded = false;
            for(String key: keys) {
                if (param.contains(key)) {
                    bFounded = true;
                    break;
                }
            }
            if(bFounded) {
                index = i;
                break;
            }
        }
        if (index > 0) {
            String baseDir = params[0];
            for(int i=1; i<index; i++) {
                baseDir = baseDir + File.separator + params[i];
            }
            String glob = params[index];
            for(int i=index+1; i<params.length; i++) {
                glob = glob + File.separator + params[i];
            }
            return glob(baseDir, glob);
        } else {
            return glob(path, "*");
        }
    }

    /**
     * Read all string data from the file.
     * @param file the file need to read
     * @return the string content of the file
     */
    public static String readAll(File file) throws FileNotFoundException {
        Scanner s = new Scanner(file);
        String body = "";
        while(s.hasNextLine()) body += "\n" + s.nextLine();
        s.close();
        return body;
    }
}
