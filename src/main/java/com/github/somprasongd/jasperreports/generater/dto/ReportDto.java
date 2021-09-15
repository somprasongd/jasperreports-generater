package com.github.somprasongd.jasperreports.generater.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ReportDto {


    @NotBlank
    private String dbUrl;
//    @NotBlank
//    private String mainReportUrl;
//
//    private String[] subReportUrls;

    @NotBlank
    @NotNull
    private JasperDto mainReport;

    private List<JasperDto> subReports;

    @NotNull
    private List<ParameterDto> parameters;

}
