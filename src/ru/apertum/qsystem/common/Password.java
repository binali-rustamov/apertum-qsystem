/*
 *  Copyright (C) 2010 Apertum project. web: www.apertum.ru email: info@apertum.ru
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.common;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

/**
 *
 * @author Evgeniy Egorov
 */
public class Password extends JDialog {

    private JPasswordField pass;
    private JButton okButton;
    private boolean ok;
    /**
     * Храним введеный пароль.
     */
    private static String password;

    private Password() {
        super((Frame) null, "Регистрация", true);
        setResizable(false);
        add(new JLabel("   Введите пароль:"), BorderLayout.NORTH);
        add(pass = new JPasswordField(""), BorderLayout.CENTER);
        JPanel p = new JPanel();
        okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                ok = true;
                setVisible(false);
            }
        });

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        p.add(okButton);
        p.add(cancel);
        add(p, BorderLayout.SOUTH);
        final Toolkit kit = Toolkit.getDefaultToolkit();
        setBounds(Math.round(kit.getScreenSize().width / 2 - 125), Math.round(kit.getScreenSize().height / 2 - 51), 250, 102);

    }

    private static void setPassword() {
        Uses.log.logger.info("Ввод регистрационного пароля.");
        final Password dlg = new Password();
        dlg.setVisible(true);
        if (!dlg.ok) {
            System.exit(0);
        }
        password = new String(dlg.pass.getPassword(), 0, dlg.pass.getPassword().length);
    }

    public static String getPassword() {
        if (("".equals(password)) || (null == password)) {
            setPassword();
        }
        return password;

    }
}