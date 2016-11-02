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
package mongis.utils;

import ijfx.ui.utils.BaseTester;
import javafx.concurrent.Task;
import javafx.scene.control.Button;

/**
 *
 * @author Cyril MONGIS, 2016
 */
public class TaskButtonBindingTester extends BaseTester{

    Button button;
    
    @Override
    public void initApp() {
        
        
        button = new Button("Click me please :-)");
        new TaskButtonBinding(button)
                .setTextWhenRunning("I'm running man !")
                .setTextWhenSucceed("I did it !")
                .setTextWhenError("Damn, something happends")
                .setTaskFactory(this::generateMe);
        
        
        setContent(button);
        
        
    }
    
    public static void main(String... args) {
        launch(args);
    }
    
    
    private Task<Boolean> generateMe(TaskButtonBinding binding) {
        return new FakeTask(4000);
    }
}
