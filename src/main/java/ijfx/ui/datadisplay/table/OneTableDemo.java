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
package ijfx.ui.datadisplay.table;

import net.imagej.table.DefaultResultsTable;
import net.imagej.table.GenericTable;
import net.imagej.table.ResultsTable;
import org.scijava.ItemIO;
import org.scijava.app.StatusService;
import org.scijava.command.Command;
import org.scijava.plugin.Attr;
import org.scijava.plugin.Parameter;
import org.scijava.plugin.Plugin;

/**
 *
 * @author Tuan anh TRINH
 */

//Create only one Table
@Plugin(type = Command.class, menuPath = "Plugins>Sandbox>OneTableDemo", headless = true, attrs = { @Attr(name = "no-legacy") })
public class OneTableDemo implements Command {

	

	@Parameter
	private StatusService statusService;

	@Parameter(label = "Paul Molitor Baseball Statistics", type = ItemIO.OUTPUT)
	private ResultsTable baseball;

	@Parameter(label = "Big Table", type = ItemIO.OUTPUT)
	private ResultsTable big;

	@Parameter(label = "Spreadsheet", type = ItemIO.OUTPUT)
	private GenericTable spreadsheet;

	@Override
	public void run() {
		statusService.showStatus("Creating a small table...");
		createBaseballTable();

		statusService.clearStatus();
	}

	private void createBaseballTable() {
		final double[][] data = {
			{1978, 21, .273},
			{1979, 22, .322},
			{1980, 23, .304},
			{1981, 24, .267},
			{1982, 25, .302},
			{1983, 26, .270},
			{1984, 27, .217},
			{1985, 28, .297},
			{1986, 29, .281},
			{1987, 30, .353},
			{1988, 31, .312},
			{1989, 32, .315},
			{1990, 33, .285},
			{1991, 34, .325},
			{1992, 35, .320},
			{1993, 36, .332},
			{1994, 37, .341},
			{1995, 38, .270},
			{1996, 39, .341},
			{1997, 40, .305},
			{1998, 41, .281},
		};
		baseball = new DefaultResultsTable(data[0].length, data.length);
		baseball.setColumnHeader(0, "Year");
		baseball.setColumnHeader(1, "Age");
		baseball.setColumnHeader(2, "BA");
		baseball.setRowHeader(9, "Best");
		for (int row = 0; row < data.length; row++) {
			for (int col = 0; col < data[row].length; col++) {
				baseball.setValue(col, row, data[row][col]);
			}
		}
	}
}