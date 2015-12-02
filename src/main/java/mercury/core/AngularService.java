/*
 * /*
 *     This file is part of ImageJ FX.
 *
 *     ImageJ FX is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     ImageJ FX is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with ImageJ FX.  If not, see <http://www.gnu.org/licenses/>. 
 *
 * 	Copyright 2015,2016 Cyril MONGIS, Michael Knop
 *
 */
package mercury.core;

import ijfx.ui.main.ImageJFX;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class AngularService {

    protected
    String name;
    protected Object object;
    protected ArrayList<String> asyncActionList = new ArrayList();
    protected ArrayList<String> syncActionList = new ArrayList();
    protected HashMap<String, AngularMethodDescription> methodDescriptions = new HashMap<>();

    protected Logger logger = ImageJFX.getLogger();
    
    public AngularService(String name, Object object) {

        this.name = name;
        this.object = object;
        AngularBinder.getMethodsAnnotatedWith(object.getClass(), AngularMethod.class).forEach((m) -> {

            if (checkMethod(m)) {
                logger.info("Action added : " + m.getName());

                AngularMethodDescription methodDescription = new AngularMethodDescription(m);
                methodDescriptions.put(m.getName(), methodDescription);

                if (methodDescription.isSync()) {
                    syncActionList.add(m.getName());
                } else {
                    asyncActionList.add(m.getName());
                }
            }
        });

    }

    public boolean checkMethod(Method m) {
        return true;
    }

    public String getName() {
        return name;
    }

    public void addAction(String action) {
        asyncActionList.add(action);
    }

    public List<String> getAsyncActionList() {
        return asyncActionList;
    }

    public List<String> getSyncActionList() {
        return syncActionList;
    }

    public Object getObject() {
        return object;
    }

    public AngularMethodDescription getMethodDescription(String methodName) {
        return methodDescriptions.get(methodName);
    }

    public List<AngularMethodDescription> getMethodDescriptionList() {
        ArrayList<AngularMethodDescription> list = new ArrayList<>();
        methodDescriptions.values().forEach(description -> list.add(description));
        return list;
    }

   
}
