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
package ijfx.service.ui;

/**
 *
 * @author cyril
 */
public interface RichTextDialog {

    public enum AnswerType {
        VALIDATE,
        CANCEL
    }

    public enum ContentType {
        HTML,
        TEXT,
        MARKDOWN
    }
    
    public class Answer {

        final private AnswerType type;

        final private String text;

        public Answer(AnswerType type, String text) {
            this.type = type;
            this.text = text;
        }
        
        public boolean isPositive() {
            return type == AnswerType.VALIDATE;
        }
        
        public String getText() {
            return text;
        }
        public boolean contains(String string) {
            return getText().toLowerCase().contains(string.toLowerCase());
        }
        
    }

    public RichTextDialog setDialogTitle(String title);

    public RichTextDialog setDialogContent(String context);

    public RichTextDialog setContentType(ContentType contentType);
    
    public RichTextDialog loadContent(Class<?> clazz, String path);

    public RichTextDialog addAnswerButton(AnswerType buttonType, String text);

    public Answer showDialog();
    
    
}
