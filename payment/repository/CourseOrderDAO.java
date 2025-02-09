package com.sportcenter.sportcenter.payment.repository;

import java.util.List;

import org.json.JSONObject;

import com.sportcenter.sportcenter.course.entity.CourseOrder;

public interface CourseOrderDAO {

    public abstract long count(JSONObject obj);

    public abstract List<CourseOrder> find(JSONObject obj);
}
