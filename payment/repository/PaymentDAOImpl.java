package com.sportcenter.sportcenter.payment.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.sportcenter.sportcenter.payment.entity.Payment;
import com.sportcenter.sportcenter.payment.util.DatetimeConverter;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


@Repository
public class PaymentDAOImpl implements PaymentDAO {

    @PersistenceContext
    private Session session;

    public Session getSession() {
        return this.session;
    }

    @Override
    public List<Payment> find(JSONObject obj) {
        String orderId = obj.isNull("orderId") ? null : obj.getString("orderId");
        String merchantTradeNo = obj.isNull("merchantTradeNo") ? null : obj.getString("merchantTradeNo");
        Integer startAmount = obj.isNull("startAmount") ? null : obj.getInt("startAmount");
        Integer endAmount = obj.isNull("endAmount") ? null : obj.getInt("endAmount");
        String paymentType = obj.isNull("paymentType") ? null : obj.getString("paymentType");
        String paymentStatus = obj.isNull("paymentStatus") ? null : obj.getString("paymentStatus");
        String startPaymentTime = obj.isNull("startPaymentTime") ? null : obj.getString("startPaymentTime");
        String endPaymentTime = obj.isNull("endPaymentTime") ? null : obj.getString("endPaymentTime");

        Integer start = obj.isNull("start") ? null : obj.getInt("start");
        Integer max = obj.isNull("max") ? null : obj.getInt("max");
        String order = obj.isNull("order") ? "id" : obj.getString("order");
        boolean dir = obj.isNull("dir") ? false : obj.getBoolean("dir");

        CriteriaBuilder criteriaBuilder = this.getSession().getCriteriaBuilder();
        CriteriaQuery<Payment> criteriaQuery = criteriaBuilder.createQuery(Payment.class);
        Root<Payment> table = criteriaQuery.from(Payment.class);
        List<Predicate> predicates = new ArrayList<>();

        // orderId like ?
        if (orderId != null && orderId.length() != 0) {
            Predicate p = criteriaBuilder.like(table.get("orderId"), "%" + orderId + "%");
            predicates.add(p);
        }

        // merchantTradeNo like ?
        if (merchantTradeNo != null && merchantTradeNo.length() != 0) {
            Predicate p = criteriaBuilder.like(table.get("merchantTradeNo"), "%" + merchantTradeNo + "%");
            predicates.add(p);
        }

        // amount > ?
        if (startAmount != null) {
            Predicate p = criteriaBuilder.greaterThan(table.get("amount"), startAmount);
            predicates.add(p);
        }

        // amount < ?
        if (endAmount != null) {
            Predicate p = criteriaBuilder.lessThan(table.get("amount"), endAmount);
            predicates.add(p);
        }

        // paymentType = ?
        if (paymentType != null && !paymentType.isEmpty()) {
            Predicate p = criteriaBuilder.equal(table.get("paymentType"), paymentType);
            predicates.add(p);
        }

        // paymentStatus = ?
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            Predicate p = criteriaBuilder.equal(table.get("paymentStatus"), paymentStatus);
            predicates.add(p);
        }

        // paymentTime > ?
        if (startPaymentTime != null && !startPaymentTime.isEmpty()) {
            java.util.Date paymentTime = DatetimeConverter.parse(startPaymentTime, "yyyy-MM-dd");
            Predicate p = criteriaBuilder.greaterThan(table.get("paymentTime"), paymentTime);
            predicates.add(p);
        }

        // paymentTime < ?
        if (endPaymentTime != null && !endPaymentTime.isEmpty()) {
            java.util.Date paymentTime = DatetimeConverter.parse(endPaymentTime, "yyyy-MM-dd");
            Predicate p = criteriaBuilder.lessThan(table.get("paymentTime"), paymentTime);
            predicates.add(p);
        }

        // where
        if (predicates != null && !predicates.isEmpty()) {
            Predicate[] array = predicates.toArray(new Predicate[0]);
            criteriaQuery = criteriaQuery.where(array);
        }

        // order by
        if (dir) {
            criteriaQuery = criteriaQuery.orderBy(criteriaBuilder.desc(table.get(order)));
        } else {
            criteriaQuery = criteriaQuery.orderBy(criteriaBuilder.asc(table.get(order)));
        }

        TypedQuery<Payment> typedQuery = this.getSession().createQuery(criteriaQuery);
        if (start != null) {
            typedQuery.setFirstResult(start);
        }
        if (max != null) {
            typedQuery.setMaxResults(max);
        }

        List<Payment> result = typedQuery.getResultList();
        if (result != null && !result.isEmpty()) {
            return result;
        } else {
            return null;
        }
    }

    @Override
    public Payment insert(Payment bean) {
        if (bean != null) {
            this.getSession().persist(bean);
            return bean;
        }
        return null;
    }

    @Override
    public Payment update(Payment bean) {
        if (bean != null && bean.getId() != null) {
            Payment temp = this.getSession().get(Payment.class, bean.getId());
            if (temp != null) {
                return (Payment) this.getSession().merge(bean);
            }
        }
        return null;
    }

    @Override
    public long count(JSONObject obj) {
        String orderId = obj.isNull("orderId") ? null : obj.getString("orderId");
        String merchantTradeNo = obj.isNull("merchantTradeNo") ? null : obj.getString("merchantTradeNo");
        Integer startAmount = obj.isNull("startAmount") ? null : obj.getInt("startAmount");
        Integer endAmount = obj.isNull("endAmount") ? null : obj.getInt("endAmount");
        String paymentType = obj.isNull("paymentType") ? null : obj.getString("paymentType");
        String paymentStatus = obj.isNull("paymentStatus") ? null : obj.getString("paymentStatus");
        String startPaymentTime = obj.isNull("startPaymentTime") ? null : obj.getString("startPaymentTime");
        String endPaymentTime = obj.isNull("endPaymentTime") ? null : obj.getString("endPaymentTime");

        CriteriaBuilder criteriaBuilder = this.getSession().getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Payment> table = criteriaQuery.from(Payment.class);
        criteriaQuery = criteriaQuery.select(criteriaBuilder.count(table));
        List<Predicate> predicates = new ArrayList<>();

        // orderId like ?
        if (orderId != null && orderId.length() != 0) {
            Predicate p = criteriaBuilder.like(table.get("orderId"), "%" + orderId + "%");
            predicates.add(p);
        }

        // merchantTradeNo like ?
        if (merchantTradeNo != null && merchantTradeNo.length() != 0) {
            Predicate p = criteriaBuilder.like(table.get("merchantTradeNo"), "%" + merchantTradeNo + "%");
            predicates.add(p);
        }

        // amount > ?
        if (startAmount != null) {
            Predicate p = criteriaBuilder.greaterThan(table.get("amount"), startAmount);
            predicates.add(p);
        }

        // amount < ?
        if (endAmount != null) {
            Predicate p = criteriaBuilder.lessThan(table.get("amount"), endAmount);
            predicates.add(p);
        }

        // paymentType = ?
        if (paymentType != null && !paymentType.isEmpty()) {
            Predicate p = criteriaBuilder.equal(table.get("paymentType"), paymentType);
            predicates.add(p);
        }

        // paymentStatus = ?
        if (paymentStatus != null && !paymentStatus.isEmpty()) {
            Predicate p = criteriaBuilder.equal(table.get("paymentStatus"), paymentStatus);
            predicates.add(p);
        }

        // paymentTime > ?
        if (startPaymentTime != null && !startPaymentTime.isEmpty()) {
            java.util.Date paymentTime = DatetimeConverter.parse(startPaymentTime, "yyyy-MM-dd");
            Predicate p = criteriaBuilder.greaterThan(table.get("paymentTime"), paymentTime);
            predicates.add(p);
        }

        // paymentTime < ?
        if (endPaymentTime != null && !endPaymentTime.isEmpty()) {
            java.util.Date paymentTime = DatetimeConverter.parse(endPaymentTime, "yyyy-MM-dd");
            Predicate p = criteriaBuilder.lessThan(table.get("paymentTime"), paymentTime);
            predicates.add(p);
        }

        // where
        if (predicates != null && !predicates.isEmpty()) {
            Predicate[] array = predicates.toArray(new Predicate[0]);
            criteriaQuery = criteriaQuery.where(array);
        }

        TypedQuery<Long> typedQuery = this.getSession().createQuery(criteriaQuery);
        Long result = typedQuery.getSingleResult();
        if (result != null) {
            return result.longValue();
        } else {
            return 0;
        }
    }
}