package com.sportcenter.sportcenter.payment.repository;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;
import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.sportcenter.sportcenter.course.entity.CourseOrder;
import com.sportcenter.sportcenter.payment.util.DatetimeConverter;

import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;


@Repository
public class OrderDAOImpl implements OrderDAO {
	@PersistenceContext
	private Session session;

	public Session getSession() {
		return this.session;
	}

	@Override
	public List<CourseOrder> find(JSONObject obj) {
		Integer courseOrderId = obj.isNull("courseOrderId") ? null : obj.getInt("courseOrderId");
		Integer memberId = obj.isNull("memberId") ? null : obj.getInt("memberId");
		Integer courseId = obj.isNull("courseId") ? null : obj.getInt("courseId");
		String bookingId = obj.isNull("bookingId") ? null : obj.getString("bookingId");
		String bookingStatus = obj.isNull("bookingStatus") ? null : obj.getString("bookingStatus");
		Integer startPrice = obj.isNull("startPrice") ? null : obj.getInt("startPrice");
		Integer endPrice = obj.isNull("endPrice") ? null : obj.getInt("endPrice");
		String startCreateTime = obj.isNull("startCreateTime") ? null : obj.getString("startCreateTime");
		String endCreateTime = obj.isNull("endCreateTime") ? null : obj.getString("endCreateTime");

		Integer start = obj.isNull("start") ? null : obj.getInt("start");
		Integer max = obj.isNull("max") ? null : obj.getInt("max");
		String order = obj.isNull("order") ? "courseOrderId" : obj.getString("order");
		boolean dir = obj.isNull("dir") ? false : obj.getBoolean("dir");

		// select * from course_order where .... order by ...
		CriteriaBuilder criteriaBuilder = this.getSession().getCriteriaBuilder();
		CriteriaQuery<CourseOrder> criteriaQuery = criteriaBuilder.createQuery(CourseOrder.class);

		// from course_order
		Root<CourseOrder> table = criteriaQuery.from(CourseOrder.class);

		List<Predicate> predicates = new ArrayList<>();

		// courseOrderId = ?
		if (courseOrderId != null) {
			Predicate p = criteriaBuilder.equal(table.get("courseOrderId"), courseOrderId);
			predicates.add(p);
		}

		// memberId = ?
		if (memberId != null) {
			Predicate p = criteriaBuilder.equal(table.get("memberId"), memberId);
			predicates.add(p);
		}

		// courseId = ?
		if (courseId != null) {
			Predicate p = criteriaBuilder.equal(table.get("courseId"), courseId);
			predicates.add(p);
		}

		// bookingId like ?
		if (bookingId != null && bookingId.length() > 0) {
			Predicate p = criteriaBuilder.like(table.get("bookingId"), "%" + bookingId + "%");
			predicates.add(p);
		}

		// bookingStatus = ?
		if (bookingStatus != null && !bookingStatus.isEmpty()) {
			Predicate p = criteriaBuilder.equal(table.get("bookingStatus"), bookingStatus);
			predicates.add(p);
		}

		// totalPrice > ?
		if (startPrice != null) {
			Predicate p = criteriaBuilder.greaterThan(table.get("totalPrice"), startPrice);
			predicates.add(p);
		}

		// totalPrice < ?
		if (endPrice != null) {
			Predicate p = criteriaBuilder.lessThan(table.get("totalPrice"), endPrice);
			predicates.add(p);
		}

		// orderTime > ?
		if (startCreateTime != null && !startCreateTime.isEmpty()) {
			java.util.Date orderTime = DatetimeConverter.parse(startCreateTime, "yyyy-MM-dd");
			Predicate p = criteriaBuilder.greaterThan(table.get("orderTime"), orderTime);
			predicates.add(p);
		}

		// orderTime < ?
		if (endCreateTime != null && !endCreateTime.isEmpty()) {
			java.util.Date orderTime = DatetimeConverter.parse(endCreateTime, "yyyy-MM-dd");
			Predicate p = criteriaBuilder.lessThan(table.get("orderTime"), orderTime);
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

		TypedQuery<CourseOrder> typedQuery = this.getSession().createQuery(criteriaQuery);
		if (start != null) {
			typedQuery.setFirstResult(start);
		}
		if (max != null) {
			typedQuery.setMaxResults(max);
		}

		List<CourseOrder> result = typedQuery.getResultList();
		if (result != null && !result.isEmpty()) {
			return result;
		} else {
			return null;
		}
	}

	@Override
	public long count(JSONObject obj) {
		Integer courseOrderId = obj.isNull("courseOrderId") ? null : obj.getInt("courseOrderId");
		Integer memberId = obj.isNull("memberId") ? null : obj.getInt("memberId");
		Integer courseId = obj.isNull("courseId") ? null : obj.getInt("courseId");
		String bookingId = obj.isNull("bookingId") ? null : obj.getString("bookingId");
		String bookingStatus = obj.isNull("bookingStatus") ? null : obj.getString("bookingStatus");
		Integer startPrice = obj.isNull("startPrice") ? null : obj.getInt("startPrice");
		Integer endPrice = obj.isNull("endPrice") ? null : obj.getInt("endPrice");
		String startCreateTime = obj.isNull("startCreateTime") ? null : obj.getString("startCreateTime");
		String endCreateTime = obj.isNull("endCreateTime") ? null : obj.getString("endCreateTime");

		// select count(*) from course_order where ....
		CriteriaBuilder criteriaBuilder = this.getSession().getCriteriaBuilder();
		CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);

		// from course_order
		Root<CourseOrder> table = criteriaQuery.from(CourseOrder.class);

		// select count(*)
		criteriaQuery = criteriaQuery.select(criteriaBuilder.count(table));

		List<Predicate> predicates = new ArrayList<>();

		// courseOrderId = ?
		if (courseOrderId != null) {
			Predicate p = criteriaBuilder.equal(table.get("courseOrderId"), courseOrderId);
			predicates.add(p);
		}

		// memberId = ?
		if (memberId != null) {
			Predicate p = criteriaBuilder.equal(table.get("memberId"), memberId);
			predicates.add(p);
		}

		// courseId = ?
		if (courseId != null) {
			Predicate p = criteriaBuilder.equal(table.get("courseId"), courseId);
			predicates.add(p);
		}

		// bookingId like ?
		if (bookingId != null && bookingId.length() > 0) {
			Predicate p = criteriaBuilder.like(table.get("bookingId"), "%" + bookingId + "%");
			predicates.add(p);
		}

		// bookingStatus = ?
		if (bookingStatus != null && !bookingStatus.isEmpty()) {
			Predicate p = criteriaBuilder.equal(table.get("bookingStatus"), bookingStatus);
			predicates.add(p);
		}

		// totalPrice > ?
		if (startPrice != null) {
			Predicate p = criteriaBuilder.greaterThan(table.get("totalPrice"), startPrice);
			predicates.add(p);
		}

		// totalPrice < ?
		if (endPrice != null) {
			Predicate p = criteriaBuilder.lessThan(table.get("totalPrice"), endPrice);
			predicates.add(p);
		}

		// orderTime > ?
		if (startCreateTime != null && !startCreateTime.isEmpty()) {
			java.util.Date orderTime = DatetimeConverter.parse(startCreateTime, "yyyy-MM-dd");
			Predicate p = criteriaBuilder.greaterThan(table.get("orderTime"), orderTime);
			predicates.add(p);
		}

		// orderTime < ?
		if (endCreateTime != null && !endCreateTime.isEmpty()) {
			java.util.Date orderTime = DatetimeConverter.parse(endCreateTime, "yyyy-MM-dd");
			Predicate p = criteriaBuilder.lessThan(table.get("orderTime"), orderTime);
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
