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
package ru.apertum.qsystem.common.cmd;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.LinkedList;
import ru.apertum.qsystem.server.model.QService;

/**
 *
 * @author Evgeniy Egorov
 */
public class RpcGetServerState extends JsonRPC20 {

    public RpcGetServerState() {
    }

    public RpcGetServerState(LinkedList<ServiceInfo> result) {
        this.result = result;
    }

    public static class ServiceInfo {

        public ServiceInfo() {
        }

        /**
         *
         * @param service услуга по которой данная статистика
         * @param countWait количество ожидающих в этой услуге
         * @param firstNumber номер первого
         */
        public ServiceInfo(QService service, int countWait, String firstNumber) {
            this.serviceName = service.getName();
            this.countWait = countWait;
            this.firstNumber = firstNumber;
        }
        @Expose
        @SerializedName("service_name")
        private String serviceName;

        public int getCountWait() {
            return countWait;
        }

        public void setCountWait(int countWait) {
            this.countWait = countWait;
        }

        public String getFirstNumber() {
            return firstNumber;
        }

        public void setFirstNumber(String firstNumber) {
            this.firstNumber = firstNumber;
        }

        public String getServiceName() {
            return serviceName;
        }

        public void setServiceName(String serviceName) {
            this.serviceName = serviceName;
        }
        @Expose
        @SerializedName("waiting")
        private int countWait;
        @Expose
        @SerializedName("first")
        private String firstNumber;
    }
    @Expose
    @SerializedName("result")
    private LinkedList<ServiceInfo> result;

    public void setResult(LinkedList<ServiceInfo> result) {
        this.result = result;
    }

    public LinkedList<ServiceInfo> getResult() {
        return result;
    }
}
