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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import javax.media.ControllerEvent;
import javax.media.ControllerListener;
import javax.media.EndOfMediaEvent;
import javax.media.Manager;
import javax.media.NoPlayerException;
import javax.media.bean.playerbean.MediaPlayer;

/**
 * Может проигрывать фидеофайлы *.mpg, *.jpg.
 * Для этого используется установленная предварительно на компьютере среда JMF.
 * По умолчанию показ ролика бесконечно в цикле.
 * @author Evgeniy Egorov
 */
public class VideoPlayer {

    private final ControllerListener cntr = new ControllerListener() {

        boolean mute = true;

        @Override
        public void controllerUpdate(ControllerEvent arg0) {
            if (arg0 instanceof EndOfMediaEvent && videoFiles != null) {
                if (mp != null && mp.getGainControl() != null) {
                    mute = mp.getGainControl().getMute();
                }
                mp.close();
                boolean flag = true;
                while (flag) {
                    flag = false;
                    final String vf = getNextVideoFile();
                    if (vf != null) {
                        //setVideoFile(vf);
                        if (setVideoFile(vf) == false || mp.getPlayer() == null) {
                            controllerUpdate(arg0);
                        } else {
                            mp.setVisible(true);
                            try {
                                mp.start();
                            } catch (Exception e) {
                            }
                        }
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ex) {
                        System.out.println(ex);
                    }
                    flag = mp.getPlayer() == null;
                    if (flag) {
                    }

                }
                mp.getGainControl().setMute(mute);
            }
        }
    };

    public VideoPlayer() {
        mp.addControllerListener(cntr);
        mp.setPlaybackLoop(true);
        mp.setControlPanelVisible(false);
        mp.setPlayer(null);
    }
    private MediaPlayer mp = new MediaPlayer();

    /**
     * Доступ к медиаплееру для детельной настройки параметров.
     * @return медиаплеер
     */
    public MediaPlayer getMediaPlayer() {
        return mp;
    }
    private String[] videoFiles;
    private String videoResourcePath;
    private int pos = -1;

    private String getNextVideoFile() {
        if (videoFiles == null || pos >= videoFiles.length - 1) {
            videoFiles = new File(videoResourcePath).list();
            pos = -1;
        }
        //выберем файл
        String fl = videoResourcePath + (videoResourcePath.substring(videoResourcePath.length() - 1).equals("/") ? "" : "/") + videoFiles[++pos];
        // проверим его на существование или найдем существующий далее по списку
        while (pos < videoFiles.length - 1 && !new File(fl).exists()) {
            fl = videoResourcePath + (videoResourcePath.substring(videoResourcePath.length() - 1).equals("/") ? "" : "/") + videoFiles[++pos];
        }
        if (new File(fl).exists()) {
            // если нашли существующий то ок
            return fl;
        } else {
            // перечитаем набор файлов из папки
            videoFiles = new File(videoResourcePath).list();
            pos = -1;
            fl = videoResourcePath + (videoResourcePath.substring(videoResourcePath.length() - 1).equals("/") ? "" : "/") + videoFiles[++pos];
            while (pos < videoFiles.length && !new File(fl).exists()) {
                fl = videoResourcePath + (videoResourcePath.substring(videoResourcePath.length() - 1).equals("/") ? "" : "/") + videoFiles[++pos];
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
        if (f.isDirectory()) {
            mp.setPlaybackLoop(false);
            final String vf = getNextVideoFile();
            if (vf != null) {
                if (!setVideoFile(vf)) {
                    return setVideoFile(getNextVideoFile());
                } else {
                    return true;
                }
            }
        } else {
            mp.setPlaybackLoop(true);
            return setVideoFile(videoResourcePath);
        }
        return false;
    }

    /**
     * Учтановить ресурс для проигрывания
     * @param videoFilePath полный путь к видеофайлу
     */
    private boolean setVideoFile(String videoFilePath) {
        try {
            //mp.setPlayer(Manager.createPlayer(new URL("file:///" + videoFilePath)));
            mp.setPlayer(Manager.createPlayer(new URL("file:///" + new File(videoFilePath).getAbsolutePath())));
            return true;
        } catch (IOException ex) {
            QLog.l().logger().error("Невозможно открыть видеофайл " + videoFilePath + ": " + ex);
            return false;
        } catch (NoPlayerException ex) {
            QLog.l().logger().error("Проигрыватель не может воспроизвести файл " + videoFilePath + ": " + ex);
            return false;
        }
    }
    private int cnt = 0;

    /**
     * Сначала установи ресурс
     * @param nativePosition если false, то по всему контролу парента
     */
    public void setVideoSize(boolean nativePosition) {
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
        }
        if (nativePosition && mp.getPreferredSize().width == 0 && cnt < 50) {
            mp.start();
            mp.close();
            setVideoResource(videoResourcePath);
            setVideoSize(nativePosition);
        } else {
            mp.setBounds(0, 0, (nativePosition ? mp.getPreferredSize().width : mp.getParent().getWidth()), (nativePosition ? mp.getPreferredSize().height : mp.getParent().getHeight()));
        }
        cnt = 0;
    }

    public void start() {
        if (mp.getPlayer() == null) {
            setVideoResource(videoResourcePath);
        }
        mp.setVisible(true);
        mp.start();
    }

    public void pouse() {
        mp.stop();
    }

    public void close() {
        mp.close();
        videoFiles = null;
    }
}
