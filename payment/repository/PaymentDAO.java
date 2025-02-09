package com.sportcenter.sportcenter.payment.repository;

import java.util.List;

import org.json.JSONObject;

import com.sportcenter.sportcenter.payment.entity.Payment;
;

public interface PaymentDAO {
    public abstract long count(JSONObject obj);

    public abstract List<Payment> find(JSONObject obj);

    public abstract Payment insert(Payment bean);

    public abstract Payment update(Payment bean);

}