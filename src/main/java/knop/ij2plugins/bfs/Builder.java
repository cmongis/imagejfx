/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knop.ij2plugins.bfs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 *
 * @author alex
 */
public class Builder {
    
    public static void main(String [] args) {
        Builder.ijPlugin();
    }
    
    public static void ijPlugin() {
      
        System.out.println("\n**********************\n" +	
                        "Handling plugin files...\n" +
                        "*****************************\n");
        String command = new String("./setFiles.sh");

        try {	
            Runtime runtime = Runtime.getRuntime();
            Process process = runtime.exec(command);
            process.waitFor();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line;

            while ((line = br.readLine()) != null) {
              System.out.println(line);
            }
        } catch (IOException ioe) {
            
        } catch (InterruptedException ie) {
            
        }

        System.out.println("\n**********************\n" +	
                            "Plugin files processed!!\n" +
                            "*****************************\n");
    }
}
