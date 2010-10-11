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
package ru.apertum.qsystem.server.observation;

import java.util.Date;
import ru.apertum.qsystem.common.Uses;
import ru.apertum.qsystem.common.model.ICustomer;
import ru.apertum.qsystem.server.model.IServiceProperty;
import ru.apertum.qsystem.server.model.IUserProperty;
import ru.apertum.qsystem.server.model.QAdvanceCustomer;

/**
 * Кдасс взаимодействия с системой видеонаблюдения PosIntelect через протокол UDP.
 * @author Evgeniy Egorov
 */
public class PosIntelectUDP extends SenderUDP implements IObservationEvent {

    private static String FUNCTION_NUMBER = "#FunctionNumber#";
    private static String FUNCTION_NANE = "#FunctionName#";
    private static String TRANSACTION_TIME = "#TransactionTime#";
    private static String CUSTOPMER_ID = "#CustomerID#";
    private static String CUSTOMER_NUMBER = "#CustomerNumber#";
    private static String CUSTOMER_NAME = "#CustomerName#";
    private static String CUSTOMER_STAND_TIME = "#CustomerStandTime#";
    private static String CUSTOMER_SURNAME = "#CustomerSurname#";
    private static String CUSTOMER_OTCHESTVO = "#CustomerOtchestvo#";
    private static String SERVICE_ID = "#ServiceID#";
    private static String SERVICE_NAME = "#ServiceName#";
    private static String USER_ID = "#UserID#";
    private static String USER_NAME = "#UserName#";
    private static String USER_POINT = "#UserPOINT#";
    private static String OLD_SERVICE_ID = "#OldServiceID#";
    private static String OLD_SERVICE_NAME = "#OldServiceName#";
    private static String NEW_SERVICE_ID = "#NewServiceID#";
    private static String NEW_SERVICE_NAME = "#NewServiceName#";
    private static String ADV_CUSTOMER_ID = "#AdvCustomerID#";
    private static String ADVANCE_TIME = "#AdvanceTime#";
    private static String MESS = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
            "<TransactionBlock>" +
            "<FunctionNumber>" + FUNCTION_NUMBER + "</FunctionNumber>" +
            "<FunctionName>" + FUNCTION_NANE + "</FunctionName>" +
            "<TransactionTime>" + TRANSACTION_TIME + "</TransactionTime>" +
            "<CustomerID>" + CUSTOPMER_ID + "</CustomerID>" +
            "<CustomerNumber>" + CUSTOMER_NUMBER + "</CustomerNumber>" +
            "<CustomerName>" + CUSTOMER_NAME + "</CustomerName>" +
            "<CustomerStandTime>" + CUSTOMER_STAND_TIME + "</CustomerStandTime>" +
            "<CustomerSurname>" + CUSTOMER_SURNAME + "</CustomerSurname>" +
            "<CustomerOtchestvo>" + CUSTOMER_OTCHESTVO + "</CustomerOtchestvo>" +
            "<ServiceID>" + SERVICE_ID + "</ServiceID>" +
            "<ServiceName>" + SERVICE_NAME + "</ServiceName>" +
            "<UserID>" + USER_ID + "</UserID>" +
            "<UserName>" + USER_NAME + "</UserName>" +
            "<UserPoint>" + USER_POINT + "</UserPoint>" +
            "<OldServiceID>" + OLD_SERVICE_ID + "</OldServiceID>" +
            "<OldServiceName>" + OLD_SERVICE_NAME + "</OldServiceName>" +
            "<NewServiceID>" + NEW_SERVICE_ID + "</NewServiceID>" +
            "<NewServiceName>" + NEW_SERVICE_NAME + "</NewServiceName>" +
            "<AdvCustomerID>" + ADV_CUSTOMER_ID + "</AdvCustomerID>" +
            "<AdvanceTime>" + ADVANCE_TIME + "</AdvanceTime>" +
            "</TransactionBlock>";

    private String injectParams(String mess, ICustomer customer, IUserProperty user, IServiceProperty oldService, IServiceProperty newService, QAdvanceCustomer advCustomer) {
        String res = mess;
        res = res.replaceFirst(TRANSACTION_TIME, Uses.format_dd_MM_yyyy_time.format(new Date()));
        if (customer != null) {
            res = res.replaceFirst(CUSTOPMER_ID, customer.getId().toString());
            res = res.replaceFirst(CUSTOMER_NUMBER, customer.getPrefix() + customer.getNumber());
            res = res.replaceFirst(CUSTOMER_NAME, "");
            res = res.replaceFirst(CUSTOMER_STAND_TIME, Uses.format_dd_MM_yyyy_time.format(customer.getStandTime()));
            res = res.replaceFirst(CUSTOMER_SURNAME, "");
            res = res.replaceFirst(CUSTOMER_OTCHESTVO, "");
            res = res.replaceFirst(SERVICE_ID, "" + customer.getState());
            res = res.replaceFirst(SERVICE_NAME, customer.getServiceName());
        }
        if (user != null) {
            res = res.replaceFirst(USER_ID, user.getId().toString());
            res = res.replaceFirst(USER_NAME, user.getName());
            res = res.replaceFirst(USER_POINT, user.getPoint());
        }
        if (oldService != null) {
            res = res.replaceFirst(OLD_SERVICE_ID, oldService.getId().toString());
            res = res.replaceFirst(OLD_SERVICE_NAME, oldService.getName());
        }
        if (newService != null) {
            res = res.replaceFirst(NEW_SERVICE_ID, newService.getId().toString());
            res = res.replaceFirst(NEW_SERVICE_NAME, newService.getName());
        }
        if (advCustomer != null) {
            res = res.replaceFirst(ADV_CUSTOMER_ID, advCustomer.getId().toString());
            res = res.replaceFirst(ADVANCE_TIME, Uses.format_dd_MM_yyyy_time.format(advCustomer.getAdvanceTime()));
            res = res.replaceFirst(SERVICE_ID, "" + advCustomer.getService().getId().toString());
            res = res.replaceFirst(SERVICE_NAME, advCustomer.getService().getName());
        }
        /*
        // в темповый файл
        final FileOutputStream fos;
        try {
        fos = new FileOutputStream("d:/video.xml");
        } catch (FileNotFoundException ex) {
        throw new Uses.ServerException("Не возможно создать временный файл состояния. " + ex.getMessage());
        }
        try {
        fos.write(res.getBytes());
        fos.flush();
        fos.close();
        } catch (IOException ex) {
        throw new Uses.ServerException("Не возможно сохранить изменения в поток." + ex.getMessage());
        }
         * */
        return res;
    }

    /**
     * Регистрация клиента в очереди
     * <TransactionBlock>
     *  <FunctionNumber>1</FunctionNumber>
     *  <FunctionName>Регистрация клиента в очереди</FunctionName>
     *  <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *  <CustomerID>1122334455</CustomerID>
     *  <CustomerNumber>В123</CustomerNumber>
     *  <CustomerName>Вася</CustomerName>
     *  <CustomerSurname>Пупкин</CustomerSurname>
     *  <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *  <ServiceID>112233445566</ServiceID>
     *  <ServiceName>Прием заявлений</ServiceName>
     *  </TransactionBlock>
     * @param customer
     */
    @Override
    public void standCustomer(ICustomer customer) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "1");
        mes = mes.replaceFirst(FUNCTION_NANE, "Регистрация клиента в очереди");
        mes = injectParams(mes, customer, null, null, null, null);
        send(mes, getPort());
    }

    /**
     * Вызов клиента для оказание услуги
     * <TransactionBlock>
     *  <FunctionNumber>2</FunctionNumber>
     *  <FunctionName>Вызов клиента для оказание услуги</FunctionName>
     *  <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *  <CustomerID>1122334455</CustomerID>
     *  <CustomerNumber>А111</CustomerNumber>
     *  <CustomerName>Вася</CustomerName>
     *  <CustomerStandTime>Вася</CustomerStandTime>
     *  <CustomerSurname>Пупкин</CustomerSurname>
     *  <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *  <UserID>111222333444</UserID>
     *  <UserName>Петров И.И.</UserName>
     *  <UserPoint>окно 3</UserPoint>
     *  <ServiceID>112233445566</ServiceID>
     *  <ServiceName>Прием заявлений</ServiceName>
     *</TransactionBlock>
     * @param customer
     * @param user
     */
    @Override
    public void inviteCustomer(ICustomer customer, IUserProperty user) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "2");
        mes = mes.replaceFirst(FUNCTION_NANE, "Вызов клиента для оказание услуги");
        mes = injectParams(mes, customer, user, null, null, null);
        send(mes, user.getAdressRS());
    }

    /**
     * Начало работы с вызванным клиентом
     * <TransactionBlock>
     *   <FunctionNumber>3</FunctionNumber>
     *   <FunctionName>Начало работы с вызванным клиентом</FunctionName>
     *   <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *   <CustomerID>1122334455</CustomerID>
     *   <CustomerNumber>А111</CustomerNumber>
     *   <CustomerName>Вася</CustomerName>
     *   <CustomerStandTime>10.10.2009 23:18:56</CustomerStandTime>
     *   <CustomerSurname>Пупкин</CustomerSurname>
     *   <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *   <UserID>111222333444</UserID>
     *   <UserName>Петров И.И.</UserName>
     *   <UserPoint>окно 3</UserPoint>
     *   <ServiceID>112233445566</ServiceID>
     *   <ServiceName>Прием заявлений</ServiceName>
     *</TransactionBlock>
     * @param customer
     * @param user
     */
    @Override
    public void startCustomer(ICustomer customer, IUserProperty user) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "3");
        mes = mes.replaceFirst(FUNCTION_NANE, "Начало работы с вызванным клиентом");
        mes = injectParams(mes, customer, user, null, null, null);
        send(mes, user.getAdressRS());
    }

    /**
     * Завершение работы с вызванным клиентом
     * <TransactionBlock>
     *   <FunctionNumber>4</FunctionNumber>
     *  <FunctionName>Завершение работы с вызванным клиентом</FunctionName>
     *  <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *  <CustomerID>1122334455</CustomerID>
     *  <CustomerNumber>А111</CustomerNumber>
     *  <CustomerName>Вася</CustomerName>
     *  <CustomerStandTime>10.10.2009 23:18:56</CustomerStandTime>
     *  <CustomerSurname>Пупкин</CustomerSurname>
     *  <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *  <UserID>111222333444</UserID>
     *  <UserName>Петров И.И.</UserName>
     *  <UserPoint>окно 3</UserPoint>
     *  <ServiceID>112233445566</ServiceID>
     *  <ServiceName>Прием заявлений</ServiceName>
     *</TransactionBlock>
     * @param customer
     * @param user
     */
    @Override
    public void finishCustomer(ICustomer customer, IUserProperty user) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "4");
        mes = mes.replaceFirst(FUNCTION_NANE, "Завершение работы с вызванным клиентом");
        mes = injectParams(mes, customer, user, null, null, null);
        send(mes, user.getAdressRS());
    }

    /**
     * Удаление вызванного клиента по неявке
     * <TransactionBlock>
     *  <FunctionNumber>5</FunctionNumber>
     * <FunctionName>Удаление вызванного клиента по неявке</FunctionName>
     *  <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *  <CustomerID>1122334455</CustomerID>
     *  <CustomerNumber>А111</CustomerNumber>
     *  <CustomerName>Вася</CustomerName>
     *  <CustomerStandTime>10.10.2009 23:18:56</CustomerStandTime>
     *  <CustomerSurname>Пупкин</CustomerSurname>
     *  <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *  <UserID>111222333444</UserID>
     *  <UserName>Петров И.И.</UserName>
     *  <UserPoint>окно 3</UserPoint>
     *  <ServiceID>112233445566</ServiceID>
     *  <ServiceName>Прием заявлений</ServiceName>
     *</TransactionBlock>

     * @param customer
     * @param user
     */
    @Override
    public void deleteCustomer(ICustomer customer, IUserProperty user) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "5");
        mes = mes.replaceFirst(FUNCTION_NANE, "Удаление вызванного клиента по неявке");
        mes = injectParams(mes, customer, user, null, null, null);
        send(mes, user.getAdressRS());
    }

    /**
     * Переадресация клиента в другую услугу
     * <TransactionBlock>
     * <FunctionNumber>6</FunctionNumber>
     * <FunctionName>Переадресация клиента в другую услугу</FunctionName>
     *  <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *  <CustomerID>1122334455</CustomerID>
     *  <CustomerNumber>А111</CustomerNumber>
     *  <CustomerName>Вася</CustomerName>
     *  <CustomerStandTime>10.10.2009 23:18:56</CustomerStandTime>
     *  <CustomerSurname>Пупкин</CustomerSurname>
     *  <CustomerOtchestvo>#Евлампиевич#</CustomerOtchestvo>
     *  <UserID>111222333444</UserID>
     *  <UserName>Петров И.И.</UserName>
     *  <UserPoint>окно 3</UserPoint>
     *  <OldServiceID>112233445566</OldServiceID>
     *  <OldServiceName>Прием заявлений</OldServiceName>
     *  <NewServiceID>112224352345</NewServiceID>
     *  <NewServiceName>Выдача заявлений</NewServiceName>
     *</TransactionBlock>
     * @param customer
     * @param user
     * @param oldService
     * @param newService
     */
    @Override
    public void redirectCustomer(ICustomer customer, IUserProperty user, IServiceProperty oldService, IServiceProperty newService) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "6");
        mes = mes.replaceFirst(FUNCTION_NANE, "Переадресация клиента в другую услугу");
        mes = injectParams(mes, customer, user, oldService, newService, null);
        send(mes, user.getAdressRS());
    }

    /**
     * Предварительная регистрация клиента
     * <TransactionBlock>
     *   <FunctionNumber>7</FunctionNumber>
     *   <FunctionName>Предварительная регистрация клиента</FunctionName>
     *   <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *   <AdvCustomerID>1122334455</AdvCustomerID>
     *   <AdvanceTime>10.10.2009 23:18:56</AdvanceTime>
     *   <CustomerName>Вася</CustomerName>
     *   <CustomerSurname>Пупкин</CustomerSurname>
     *   <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *   <ServiceID>112233445566</ServiceID>
     *   <ServiceName>Прием заявлений</ServiceName>
     *</TransactionBlock>
     * @param advCustomer
     */
    @Override
    public void advanceCustomer(QAdvanceCustomer advCustomer) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "7");
        mes = mes.replaceFirst(FUNCTION_NANE, "Предварительная регистрация клиента");
        mes = injectParams(mes, null, null, null, null, advCustomer);
        send(mes, getPort());
    }

    /**
     * Регистрация клиента по предварительной записи
     * <TransactionBlock>
     *   <FunctionNumber>8</FunctionNumber>
     *   <FunctionName>Регистрация клиента по предварительной записи</FunctionName>
     *   <TransactionTime>10.10.2009 23:18:56</TransactionTime>
     *   <CustomerID>1122334455</CustomerID>
     *   <CustomerNumber>А112</CustomerNumber>
     *   <CustomerName>Вася</CustomerName>
     *   <CustomerSurname>Пупкин</CustomerSurname>
     *   <CustomerOtchestvo>Евлампиевич</CustomerOtchestvo>
     *   <ServiceID>112233445566</ServiceID>
     *   <ServiceName>Прием заявлений</ServiceName>
     *</TransactionBlock>
     * @param advCustomer
     * @param customer
     */
    @Override
    public void asvanceStandCustomer(QAdvanceCustomer advCustomer, ICustomer customer) {
        String mes = MESS;
        mes = mes.replaceFirst(FUNCTION_NUMBER, "8");
        mes = mes.replaceFirst(FUNCTION_NANE, "Регистрация клиента по предварительной записи");
        mes = injectParams(mes, customer, null, null, null, null);
        send(mes, getPort());
    }
}
