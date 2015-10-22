/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.mss.examples.petstore.store.view;

import org.wso2.carbon.mss.examples.petstore.store.dao.PaymentService;
import org.wso2.carbon.mss.examples.petstore.store.model.Cart;
import org.wso2.carbon.mss.examples.petstore.store.model.OrderServiceException;
import org.wso2.carbon.mss.examples.petstore.util.model.CreditCard;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.RequestScoped;
import java.io.Serializable;

@ManagedBean
@RequestScoped
public class CheckoutBean implements Serializable {

    private String ccNumber;
    private String name;
    private String cvc;
    private String orderConfirmationNo;

    @ManagedProperty("#{cart}")
    private Cart cart;

    @ManagedProperty("#{paymentService}")
    private PaymentService paymentService;

    public String checkout() throws OrderServiceException {
        CreditCard creditCard = new CreditCard();
        creditCard.setNumber(ccNumber);
        creditCard.setName(name);
        creditCard.setCvc(cvc);
        orderConfirmationNo = paymentService.order(cart, creditCard);
        cart.clear();
        return "confirmation";
    }

    public String getCcNumber() {
        return ccNumber;
    }

    public void setCcNumber(String ccNumber) {
        this.ccNumber = ccNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCvc() {
        return cvc;
    }

    public void setCvc(String cvc) {
        this.cvc = cvc;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public String getOrderConfirmationNo() {
        return orderConfirmationNo;
    }

    public void setOrderConfirmationNo(String orderConfirmationNo) {
        this.orderConfirmationNo = orderConfirmationNo;
    }

    public PaymentService getPaymentService() {
        return paymentService;
    }

    public void setPaymentService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }
}
