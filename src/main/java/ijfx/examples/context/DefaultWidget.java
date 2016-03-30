/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ijfx.examples.context;

/**
 *
 * @author tuananh
 */
public class DefaultWidget implements ItemWidget{
public String type;
public String label;
public String action;
public String icon;
public String context ;
public Parameters parameters;  

    public DefaultWidget() {
    }

    public DefaultWidget(String type, String label, String action, String icon, String context, Parameters parameters) {
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
    public Parameters getParameters() {
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

        System.out.println(this.context);
    }
    
    
}
