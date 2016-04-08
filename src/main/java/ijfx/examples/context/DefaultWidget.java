/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.examples.context;

import java.util.Map;

/**
 *
 * @author Tuan anh TRINH
 */
public class DefaultWidget implements ItemWidget{
private String type;
private String label;
private String action;
private String icon;
private String context ;
private Map<String,Object> parameters;  

    public DefaultWidget() {
    }

    public DefaultWidget(String type, String label, String action, String icon, String context, Map<String,Object> parameters) {
        this.type = type;
        this.label = label;
        this.action = action;
        this.icon = icon;
        this.context = context;
        this.parameters = parameters;
    }

    public DefaultWidget(String type, String label, String action, String icon, String context) {
        this.type = type;
        this.label = label;
        this.action = action;
        this.icon = icon;
        this.context = context;
        this.parameters = null;


    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public String getAction() {
        return action;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public String getIcon() {
        return icon;
    }

    @Override
    public Map<String,Object> getParameters() {
        return parameters;
    }

    @Override
    public String getContext() {
        return context;
    }
    
    @Override
    public void addContext(String s){
        context = context +s;
    }

    @Override
    public void removeSpaceContext() {
        this.context = this.context.replaceAll("\\s", "+");
    }

    @Override
    public Item getValue() {
        return this;
    }
    
    
}
