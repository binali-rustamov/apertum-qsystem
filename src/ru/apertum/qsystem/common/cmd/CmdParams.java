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
 *
 */
package ru.apertum.qsystem.common.cmd;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 *
 * @author Evgeniy Egorov
 */
public class CmdParams {

    public CmdParams() {
    }
    @Expose
    @SerializedName("service_id")
    public Long serviceId;
    @Expose
    @SerializedName("user_id")
    public Long userId;
    @Expose
    @SerializedName("pass")
    public String password;
    @Expose
    @SerializedName("priority")
    public int priority;
    @Expose
    @SerializedName("text_data")
    public String textData;
    @Expose
    @SerializedName("result_id")
    public Long resultId;
    @Expose
    @SerializedName("request_back")
    public Boolean requestBack;
    @Expose
    @SerializedName("coeff")
    public Integer coeff;
    @Expose
    @SerializedName("date")
    public Long date;
    @Expose
    @SerializedName("customer_id")
    public Long customerId;
    @Expose
    @SerializedName("response_id")
    public Long responseId;
    @Expose
    @SerializedName("client_auth_id")
    public String clientAuthId;
    @Expose
    @SerializedName("info_item_name")
    public String infoItemName;
}
