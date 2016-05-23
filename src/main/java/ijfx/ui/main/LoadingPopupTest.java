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
package ijfx.ui.main;

import ijfx.ui.utils.BaseTester;
import javafx.scene.control.Label;
import mongis.utils.FakeTask;

/**
 *
 * @author cyril
 */
public class LoadingPopupTest extends BaseTester {

    Label helloWorld = new Label("hello");

    LoadingPopup loadingPopup;

    @Override
    public void initApp() {
        setContent(helloWorld);
        addAction("Show popup", this::showPopup);
        addAction("Reset", this::reset);
        reset();
    }

    public void showPopup() {
        if (loadingPopup.isShowing() == false) {
            loadingPopup
                    .showOnStart(helloWorld)
                    .bindTask(new FakeTask(2000).start())
                    
                    .setCanCancel(false)
                    .closeOnFinished(true)
                    ;//.closeOnFinished(false);
        } else {
            loadingPopup.hide();
        }

    }

    public void reset() {
        loadingPopup = new LoadingPopup();
        loadingPopup.showCloseButtonProperty().setValue(true);

    }

    public static void main(String[] args) {
        launch(args);
    }

}
