/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package burp.userinterface.menus;

import java.awt.event.ActionListener;
import javax.swing.JMenuItem;

/**
 *
 * @author Joaquin R. Martinez
 */
public abstract class MenuItem extends JMenuItem implements ActionListener{

    public MenuItem() {
        super("Add to CSRF Scanner List");
        addActionListener(this);
    }    
}
