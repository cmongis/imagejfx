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
package ijfx.core.project.query.tree;

import ijfx.core.project.imageDBService.PlaneDB;
import ijfx.core.project.query.Selector;
import ijfx.core.project.query.SelectorFactory;

/**
 *
 * @author cyril
 */
public class SelectorNode implements Selector {

    private SelectorNode left;

    private SelectorNode right;

    private final SelectorFactory factory;

    private Selector selector;

    private static final String AND = " and ";

    private static final String OR = " or ";

    private static final char POPEN = '(';
    private static final char PCLOSE = ')';

    private String booleanOperation;

    public SelectorNode(SelectorFactory factory) {
        this.factory = factory;
    }
    public SelectorNode(SelectorFactory factory, String queryString) {
        this(factory);
        parse(queryString);
    }

    String queryString;

    @Override
    public void parse(String queryString) {

        // trimming and lowercasing 
        String lowercase = queryString.toLowerCase().trim();

        boolean openBrackets = false;
        
        // initializing the variable
        this.queryString = queryString;
        left = null;
        right = null;
        selector = null;
        String leftStr = null;
        String rightStr = null;
        
        // we go through the characters
        for (int i = 0; i != lowercase.length(); i++) {

            // if a bracket is open, we close and vis versa
            if ('"' == lowercase.charAt(i)) {
                // inverting the flag
                openBrackets = !openBrackets;
            }
            // we just pass until the brackets are closes
            if (openBrackets) {
                continue;
            }

            // if there is an parenthesis opening
            if (lowercase.charAt(i) == POPEN) {
                String inner = fetchInner(lowercase.substring(i));
                System.out.println(inner);

                selector = new SelectorNode(factory);
                selector.parse(inner);

                break;
            }

            if (isOperator(lowercase.substring(i))) {

               

                leftStr = queryString.substring(0, i);
                rightStr = queryString.substring(i + getOperator(lowercase.substring(i)).length());
                 System.out.println(String.format("Operator found: '%s' <--> '%s'",leftStr,rightStr));
                 booleanOperation = getOperator(lowercase.substring(i));
                 left = new SelectorNode(factory,leftStr);
                 right = new SelectorNode(factory,rightStr);
                 
                break;

            }

        }

        if (leftStr == null && selector == null) {
            selector = factory.create(queryString);
            selector.parse(queryString);
        }
    }

    public String fetchInner(String str) {

        int openParenthesis = 0;

        for (int i = 0; i != str.length(); i++) {
            char c = str.charAt(i);
            if (c == POPEN) {
                openParenthesis++;
            } else if (c == PCLOSE) {
                openParenthesis--;
            }

            if (openParenthesis == 0) {
                return str.substring(1, i);
            }
        }

        return str;

    }

    public boolean isOperator(String subStr) {
        return getOperator(subStr) != null;
    }

    public String getOperator(String subStr) {
        if (subStr.startsWith(OR)) {
            return OR;
        }
        if (subStr.startsWith(AND)) {
            return AND;
        }
        return null;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public boolean matches(PlaneDB planeDB, String metadataSetName) {
        
        
        
        if (left != null && right != null) {
            if (booleanOperation == AND) {
                return left.matches(planeDB, metadataSetName) && right.matches(planeDB, metadataSetName);
            } else {
                return left.matches(planeDB, metadataSetName) || right.matches(planeDB, metadataSetName);
            }

        } else {
            return selector.matches(planeDB, metadataSetName);
        }
    }

    public SelectorNode getLeft() {
        return left;
    }

    public void setLeft(SelectorNode left) {
        this.left = left;
    }

    public SelectorNode getRight() {
        return right;
    }

    public void setRight(SelectorNode right) {
        this.right = right;
    }

    public SelectorFactory getFactory() {
        return factory;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    public String getBooleanOperation() {
        return booleanOperation;
    }

    public void setBooleanOperation(String booleanOperation) {
        this.booleanOperation = booleanOperation;
    }

    public static void main(String... args) {

        SelectorNode node = new SelectorNode(new DummySelectorFactory());

        node.parse("(x = 2) and ((x > 3) OR x > 3)");
        node.parse("( x = 0 and y = 1) or x < = 3");
        node.parse("x = 3 and x>3 and x 1= 3");
        node.parse("x = 2 and (x < 3)");
        node.parse("((x> 2) AND x < 3)");
        node.parse("(\"I want candies and more or stuffs (hehe)\")");
    }

    @Override
    public boolean canParse(String queryString) {

        parse(queryString);
        return isValid();
    }
    
    public boolean isValid() {
        if(left != null && right != null) {
            
            return left.isValid() && right.isValid();
            
            
        }
        else {
            return selector != null;
        }
    }

}
