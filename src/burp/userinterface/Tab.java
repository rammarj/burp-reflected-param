/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp.userinterface;

import burp.ITab;
import java.awt.Component;

/**
 *
 * @author Joaquin R. Martinez
 */
public class Tab implements ITab, Cloneable{

    private Component contentComponent;
    private String tabString;
    
    public Tab(String tabstring, Component ui) {
        this.contentComponent = ui;
        this.tabString = tabstring;
    }

    public Tab() {
        this.contentComponent = null;
        this.tabString = "";
    }    

    @Override
    public String getTabCaption() {
        return this.tabString;
    }

    @Override
    public Component getUiComponent() {
        return this.contentComponent;
    }    

}
