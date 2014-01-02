/*
 * Copyright (C) 2013 Evgeniy Egorov
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ru.apertum.qsystem.common;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Set;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.embed.swing.JFXPanel;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javax.swing.JPanel;
import ru.apertum.qsystem.common.exceptions.ClientException;

/**
 *
 * @author Evgeniy Egorov
 */
public class BrowserFX extends JPanel {

    private final JFXPanel javafxPanel;
    private Browser bro;

    public BrowserFX() {
        javafxPanel = new JFXPanel();
        GridLayout gl = new GridLayout(1, 1);
        setLayout(gl);
        add(javafxPanel, BorderLayout.CENTER);

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                bro = new Browser();
                Scene scene = new Scene(bro, 750, 500, Color.web("#666970"));
                javafxPanel.setScene(scene);
            }
        });
    }

    public void load(final String url) {
        int k = 0;
        while ((bro == null || bro.getWebEngine() == null) && k < 50) {
            k++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        if (bro == null) {
            throw new ClientException("Browser = NULL");
        }
        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                bro.load(url);
            }
        });
    }

    class Browser extends Region {

        final private WebView browser = new WebView();
        final private WebEngine webEngine = browser.getEngine();

        private WebEngine getWebEngine() {
            return webEngine;
        }

        public Browser() {
            browser.getChildrenUnmodifiable().addListener(new ListChangeListener<Node>() {

                @Override
                public void onChanged(Change<? extends Node> change) {
                    final Set<Node> deadSeaScrolls = browser.lookupAll(".scroll-bar");
                    for (Node scroll : deadSeaScrolls) {
                        scroll.setVisible(false);
                    }
                }
            });
            getChildren().add(browser);
        }

        public void load(String url) {
            webEngine.load(url);
        }

        @Override
        protected void layoutChildren() {
            layoutInArea(browser, 0, 0, getWidth() + 10, getHeight(), 0, HPos.CENTER, VPos.CENTER);
        }
    }
}
