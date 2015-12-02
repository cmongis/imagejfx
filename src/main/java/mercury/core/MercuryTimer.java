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
import java.util.logging.Logger;

/**
 *
 * @author Cyril MONGIS, 2015
 */
public class MercuryTimer {

    String id;

    Logger logger;

    public MercuryTimer(String id) {
        this.id = id;
        start();
    }

    public Logger getLogger() {
        if (logger == null) {
            logger = ImageJFX.getLogger();
        }
        return logger;
    }

    public void setLogger(Logger logger) {
        this.logger = logger;
    }

    long start;
    long last;

    public void start() {
        start = System.currentTimeMillis();
        last = System.currentTimeMillis();
    }

    public long now() {
        return System.currentTimeMillis();
    }

    public String getStringId() {
        return String.format("[%s] ", id);
    }

    public void log(String text) {
        if (getLogger() != null) {
            logger.info(text);
        } else {

        }
    }

    public long elapsed(String text) {
        long now = System.currentTimeMillis();
        long elapsed = (now - last);

        log(getStringId() + text + " : " + elapsed + "ms");
        last = now;
        return elapsed;
    }

    public long total(String text) {
        log(getStringId() + (now() - start) + "ms");
        return now() - start;
    }

}
