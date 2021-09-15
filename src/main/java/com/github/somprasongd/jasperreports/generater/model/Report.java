package com.github.somprasongd.jasperreports.generater.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

import javax.validation.constraints.NotNull;

@Data
@RedisHash("Report")
public class Report {
    @Id
    private String id;
    @NotNull
    private String name;
    @NotNull
    private String path;
}
