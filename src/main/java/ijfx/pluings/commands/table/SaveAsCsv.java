/*
    This file is part of ImageJ FX.

    ImageJ FX is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    ImageJ FX is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
    
     Copyright 2015,2016 Cyril MONGIS, Michael Knop
	
 */
package ijfx.pluings.commands.table;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import net.imagej.table.Table;
import net.imagej.table.TableDisplay;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.scijava.command.Command;
import org.scijava.display.DisplayService;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;
import org.scijava.ui.DialogPrompt;
import org.scijava.ui.UIService;

/**
 *
 * @author Tuan anh TRINH
 */
@Plugin(type = Command.class, menuPath = "Plugins > Table >  Save as CSV", attrs = {
    @Attr(name = "no-legacy")})
public class SaveAsCsv implements Command {

    @Parameter
    UIService uIService;
    @Parameter
    DisplayService displayService;

    @Parameter(label = "Delimiter")
    String delimiter = ",";

    @Parameter(label = "Save as")
    File file;

    @Parameter(label = "Header")
    String header;
    TableDisplay tableDisplay;
    FileWriter fileWriter = null;

    CSVPrinter csvFilePrinter = null;

    CSVFormat csvFileFormat = CSVFormat.DEFAULT.withRecordSeparator("\n");

    @Override
    public void run() {
        if (displayService.getActiveDisplay() instanceof TableDisplay) {

            try {
                csvFileFormat = csvFileFormat.withDelimiter(delimiter.charAt(0));
                tableDisplay = (TableDisplay) displayService.getActiveDisplay();
                fileWriter = new FileWriter(file.getAbsolutePath());
                csvFilePrinter = new CSVPrinter(fileWriter, csvFileFormat);
                String[] headers;
                if (header == null || header.trim().equals("")) {

                    headers = getColumnHeader(tableDisplay);

                }
                else {
                    headers = header.split(delimiter);
                }
                if (headers.length != tableDisplay.get(0).size() && !header.equals("")) {
                    String message = "The number of header doesn't match with the number of columns";
                    uIService.showDialog(message, DialogPrompt.MessageType.ERROR_MESSAGE);
                    return;
                } else {
                    csvFilePrinter.printRecord(Arrays.stream(headers).collect(Collectors.toList()));
                }

                for (int i = 0; i < tableDisplay.get(0).get(0).size(); i++) {
                    List line = new ArrayList();
                    for (int j = 0; j < tableDisplay.get(0).size(); j++) {
                        line.add(tableDisplay.get(0).get(j, i).toString());
                    }
                    csvFilePrinter.printRecord(line);
                }

                fileWriter.flush();
                fileWriter.close();
                csvFilePrinter.close();

            } catch (IOException ex) {
                Logger.getLogger(SaveAsCsv.class.getName()).log(Level.SEVERE, null, ex);

            }

        }
    }

    public String[] getColumnHeader(TableDisplay display) {

        Table table = display.get(0);

        return IntStream.range(0, table.getColumnCount())
                .mapToObj(i -> table.getColumnHeader(i))
                .toArray(size -> new String[size]);

    }

}
