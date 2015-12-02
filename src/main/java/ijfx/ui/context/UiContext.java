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
package ijfx.ui.context;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class UiContext implements Comparable<UiContext> {

    private String id;

    private ArrayList<String> incompatibles = new ArrayList<>();

    public static final String INCOMPATIBLE_WITH = "incompatibleWith";
    public static final String ID = "name";

    /**
     *
     * @param id
     */
    public UiContext() {

    }

    public UiContext(String id) {
        setId(id);
    }

    /**
     *
     * @return
     */
    @JsonGetter(INCOMPATIBLE_WITH)
    public List<String> getIncompatibles() {
        return incompatibles;
    }

    /**
     *
     * @return
     */
    @JsonGetter(ID)
    public String getId() {
        return id;
    }

    /**
     *
     * @param id
     */
    @JsonSetter(ID)
    public void setId(String id) {
        this.id = id;
    }

    /**
     *
     * @param incompatibles
     */
    @JsonSetter(INCOMPATIBLE_WITH)
    public void setIncompatibles(ArrayList<String> incompatibles) {
        this.incompatibles = incompatibles;
    }

    @Override
    public int compareTo(UiContext o) {
        return o.getId().compareTo(getId());
    }

    public UiContext addIncompatibleContext(String... incompatibleCtx) {
        for (String concurrent : incompatibleCtx) {
            incompatibles.add(concurrent);
        }

        return this;
    }

}
