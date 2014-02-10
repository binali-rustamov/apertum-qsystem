/*
 *  Copyright (C) 2010 {Apertum}Projects. web: www.apertum.ru email: info@apertum.ru
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
import java.awt.GridLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javax.swing.JPanel;
import ru.apertum.qsystem.common.exceptions.ClientException;

/**
 * Может проигрывать фидеофайлы *.mpg, *.jpg. Для этого используется установленная предварительно на компьютере среда JMF. По умолчанию показ ролика бесконечно
 * в цикле.
 *
 * @author Evgeniy Egorov
 */
public class VideoPlayer extends JPanel {

    private MediaView medView = null;

    public VideoPlayer() {
        javafxPanel = new JFXPanel();
        javafxPanel.setOpaque(false);
        setOpaque(false);
        GridLayout gl = new GridLayout(1, 1);
        setLayout(gl);
        add(javafxPanel, BorderLayout.CENTER);

        Platform.runLater(new Runnable() {

            @Override
            public void run() {
                final Group root = new Group();
                final Scene scene = new Scene(root);
                createJavaFXContent(root);
                javafxPanel.setScene(scene);
                scene.setFill(new Color(0, 0, 0, 0));
            }

            private void createJavaFXContent(Group root) {
                final MediaView view = new MediaView();
                medView = view;

                javafxPanel.addComponentListener(new ComponentListener() {

                    @Override
                    public void componentResized(ComponentEvent e) {
                        if (view.getMediaPlayer() != null && view.getMediaPlayer().getMedia() != null) {
                            double sx = (double) javafxPanel.getWidth() / (double) view.getMediaPlayer().getMedia().widthProperty().getValue();
                            double dxy = sx;
                            if (view.getMediaPlayer().getMedia().heightProperty().getValue() * sx > javafxPanel.getHeight()) {
                                dxy = (double) javafxPanel.getHeight() / (double) view.getMediaPlayer().getMedia().heightProperty().getValue();
                            }
                            view.setScaleX(dxy);
                            view.setScaleY(dxy);
                            view.setX((javafxPanel.getWidth() - view.getMediaPlayer().getMedia().widthProperty().getValue()) / 2);
                            view.setY((javafxPanel.getHeight() - view.getMediaPlayer().getMedia().heightProperty().getValue()) / 2);
                        }
                    }

                    @Override
                    public void componentMoved(ComponentEvent e) {
                    }

                    @Override
                    public void componentShown(ComponentEvent e) {
                    }

                    @Override
                    public void componentHidden(ComponentEvent e) {
                    }
                });
                root.getChildren().add(view);
            }
        });

    }
    private static JFXPanel javafxPanel;

    /**
     * Доступ к медиаплееру для детельной настройки параметров.
     *
     * @return медиаплеер
     */
    public MediaView getMediaView() {
        int k = 0;
        while ((medView == null || medView.getMediaPlayer() == null) && k < 30) {
            k++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
        }
        if (medView == null) {
            throw new ClientException("MediaPlayer = NULL");
        }
        return medView;
    }
    private String[] videoFiles;
    private String videoResourcePath;
    private int pos = -1;

    private String getNextVideoFile() {
        if (videoFiles == null || pos >= videoFiles.length - 1) {
            if (new File(videoResourcePath).isDirectory()) {
                // ролики в папке
                videoFiles = new File(videoResourcePath).list();
                int i = 0;
                for (String string : videoFiles) {
                    videoFiles[i++] = videoResourcePath + (videoResourcePath.substring(videoResourcePath.length() - 1).equals("/") ? "" : "/") + string;
                }
            } else {
                // список роликов в текстовом файле построчно
                try (FileInputStream fis = new FileInputStream(videoResourcePath); Scanner s = new Scanner(fis)) {
                    final LinkedList<String> l = new LinkedList<>();
                    while (s.hasNextLine()) {
                        final String line = s.nextLine().trim();
                        if (!line.startsWith("#") && new File(line).isFile()) {
                            l.add(line);
                        }
                    }
                    videoFiles = l.toArray(new String[0]);
                } catch (IOException ex) {
                    System.err.println(ex);
                    throw new RuntimeException(ex);
                }
            }

            pos = -1;
        }
        //выберем файл
        String fl = videoFiles[++pos];
        // проверим его на существование или найдем существующий далее по списку
        while (pos < videoFiles.length - 1 && !new File(fl).exists()) {
            fl = videoFiles[++pos];
        }
        if (new File(fl).exists()) {
            // если нашли существующий то ок
            return fl;
        } else {
            // перечитаем набор файлов
            if (new File(videoResourcePath).isDirectory()) {
                // ролики в папке
                videoFiles = new File(videoResourcePath).list();
                int i = 0;
                for (String string : videoFiles) {
                    videoFiles[i++] = videoResourcePath + (videoResourcePath.substring(videoResourcePath.length() - 1).equals("/") ? "" : "/") + string;
                }
            } else {
                // список роликов в текстовом файле построчно
                try (FileInputStream fis = new FileInputStream(videoResourcePath); Scanner s = new Scanner(fis)) {
                    final LinkedList<String> l = new LinkedList<>();
                    while (s.hasNextLine()) {
                        final String line = s.nextLine().trim();
                        if (!line.startsWith("#") && new File(line).isFile()) {
                            l.add(line);
                        }
                    }
                    videoFiles = l.toArray(new String[0]);
                } catch (IOException ex) {
                    System.err.println(ex);
                    throw new RuntimeException(ex);
                }
            }
            pos = -1;
            fl = videoFiles[++pos];
            while (pos < videoFiles.length && !new File(fl).exists()) {
                fl = videoFiles[++pos];
            }
            if (new File(fl).exists()) {
                return fl;
            } else {
                return null;
            }
        }

    }

    public boolean setVideoResource(String videoResourcePath) {
        cnt++;
        videoFiles = null;
        this.videoResourcePath = videoResourcePath;
        File f = new File(videoResourcePath);
        if (f.isDirectory() || f.getName().endsWith(".txt")) {
            final String vf = getNextVideoFile();
            if (vf != null) {
                if (!setVideoFile(vf, 1)) {
                    return setVideoFile(getNextVideoFile(), 1);
                } else {
                    return true;
                }
            }
        } else {
            return setVideoFile(videoResourcePath, 90000000);
        }
        return false;
    }

    private final static HashMap<String, MediaPlayer> vids = new HashMap<>();

    private synchronized static MediaPlayer getMediaPlayer(String videoFilePath) {
        if (vids.get(videoFilePath) == null) {
            vids.put(videoFilePath, new MediaPlayer(new Media(new File(videoFilePath).toURI().toString())));
        }
        return vids.get(videoFilePath);
    }

    /**
     * Учтановить ресурс для проигрывания
     *
     * @param videoFilePath полный путь к видеофайлу
     */
    private boolean setVideoFile(String videoFilePath, int cycles) {
        try {
            getMediaView().setMediaPlayer(getMediaPlayer(videoFilePath));
            getMediaView().getMediaPlayer().setCycleCount(cycles);
            javafxPanel.getComponentListeners()[0].componentResized(null);

            if (cycles == 1) {
                getMediaView().getMediaPlayer().setOnEndOfMedia(new Runnable() {

                    @Override
                    public void run() {
                        boolean mute = getMediaView().getMediaPlayer().isMute();
                        final String vf = getNextVideoFile();
                        if (vf != null) {
                            getMediaView().getMediaPlayer().stop();
                            getMediaView().setMediaPlayer(null);
                            if (!setVideoFile(vf, 1)) {
                                setVideoFile(getNextVideoFile(), 1);
                            }
                            getMediaView().getMediaPlayer().setMute(mute);
                            //removeAll();
                            //setLayout(new GridLayout(1, 1));
                            //add(javafxPanel);
                            final Timer t = new Timer(true);
                            t.schedule(new TimerTask() {

                                @Override
                                public void run() {
                                    start();
                                }
                            }, 500);
                        }
                    }
                });
            }
        } catch (Throwable th) {
            QLog.l().logger().error("Проблема проигрывания видео при старте ролика.", th);
            return false;
        }
        return true;
    }
    private int cnt = 0;

    /**
     * Сначала установи ресурс
     *
     * @param nativePosition если false, то по всему контролу парента
     */
    public void setVideoSize(boolean nativePosition) {
        if (javafxPanel.getComponentListeners().length > 0) {
            javafxPanel.getComponentListeners()[0].componentResized(null);
        }
    }

    public void start() {
        final Timer t = new Timer(true);
        t.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    getMediaView().getMediaPlayer().play();
                    javafxPanel.getComponentListeners()[0].componentResized(null);
                } catch (Exception npe) {
                    QLog.l().logger().error("Кодак не поддерживается. Codak not supported. Видео должно быть в формате H.264.", npe);
                }
            }
        }, 1500);
    }

    public void pause() {
        getMediaView().getMediaPlayer().pause();
    }

    public void close() {
        getMediaView().getMediaPlayer().stop();
        getMediaView().getMediaPlayer().dispose();
        vids.clear();
        videoFiles = null;
    }
}
