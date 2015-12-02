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
package ijfx.ui.project_manager.project;

import ijfx.core.project.WordPosition;
import ijfx.core.project.WordPosition.WordType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.imagej.ImageJService;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.StyleSpansBuilder;
import org.scijava.Priority;
import org.scijava.plugin.Plugin;
import org.scijava.service.AbstractService;
import org.scijava.service.Service;

/**
 *
 * @author Cyril Quinton
 */
@Plugin(type = Service.class, priority = Priority.LOW_PRIORITY)
@Deprecated
public class CodeHighlighterService extends AbstractService implements ImageJService{

    private final String styleSheet;

    public CodeHighlighterService() {
        styleSheet = getClass().getResource("query-code.css").toExternalForm();
    }

    public StyleClassedTextArea highLight(StyleClassedTextArea textArea, List<WordPosition> positions) {
        // textArea.getScene().getStylesheets().add(styleSheet);
        //textArea.setParagraphGraphicFactory(LineNumberFactory.get(textArea));
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int lastKwEnd = 0;
        for (WordPosition pos : positions) {
            WordType wt = pos.getWordType();
            String className = wt == WordType.LOGICAL ? "logical-operator"
                    : wt == WordType.METADATA ? "metadata"
                            : wt == WordType.TAG ? "tag"
                                    : "";
            //spansBuilder.add(Collections.emptyList(), 2);
            //spansBuilder.add(new StyleSpan<Collection<String>>(Collections.singleton(className), 3));
            //spansBuilder.add(Collections.singleton(className), 3);
            spansBuilder.add(Collections.emptyList(), pos.startPos() - lastKwEnd);
            spansBuilder.add(Collections.singleton(className), pos.endPos() - pos.startPos());
            lastKwEnd = pos.endPos();
            //textArea.setStyleClass(pos.startPos(), pos.endPos(), className);
        }
        spansBuilder.add(Collections.emptyList(), textArea.getText().length() - lastKwEnd);
        textArea.setStyleSpans(0, spansBuilder.create());
        return textArea;

    }

}
