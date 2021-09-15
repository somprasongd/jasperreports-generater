package com.github.somprasongd.jasperreports.generater.repo;

import com.github.somprasongd.jasperreports.generater.model.Report;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

@EnableRedisRepositories
public interface ReportRepository extends CrudRepository<Report, String> {
    List<Report> findByName(String name);
}
