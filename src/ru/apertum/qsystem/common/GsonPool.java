/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ru.apertum.qsystem.common;

import ru.apertum.qsystem.common.exceptions.ServerException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.SoftReferenceObjectPool;

/**
 *
 * @author egorov
 */
public class GsonPool extends SoftReferenceObjectPool {

    private GsonPool(BasePoolableObjectFactory basePoolableObjectFactory) {
        super(basePoolableObjectFactory);
    }
    private static GsonPool instance = null;

    public static GsonPool getInstance() {
        if (instance == null) {

            instance = new GsonPool(new BasePoolableObjectFactory() {

                @Override
                public Object makeObject() throws Exception {
                    //return new Gson();
                    return new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
                    //return new GsonBuilder().serializeNulls().excludeFieldsWithoutExposeAnnotation().create();
                }
            });

        }
        return instance;
    }

    public Gson borrowGson() {
        try {
            return (Gson) instance.borrowObject();
        } catch (Exception ex) {
            throw new ServerException("Проблемы с gson pool. ", ex);
        }
    }

    public void returnGson(Gson gson) {
        try {
            instance.returnObject(gson);
        } catch (Exception ex) {
            throw new ServerException("Проблемы с  gson pool. ", ex);
        }
    }
}
