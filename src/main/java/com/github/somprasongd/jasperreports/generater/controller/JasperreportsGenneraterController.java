package com.github.somprasongd.jasperreports.generater.controller;

import com.github.somprasongd.jasperreports.generater.dto.ParameterDto;
import com.github.somprasongd.jasperreports.generater.dto.ReportDto;
import com.github.somprasongd.jasperreports.generater.service.ReportService;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("v1/jasper")
public class JasperreportsGenneraterController {

    @Autowired
    private ReportService reportService;
//    @Autowired
//    private ResourceLoader resourceLoader;

//    private final String JASPER_DIR = System.getProperty("user.dir") + File.separator + "jaspers";
//
//    private String getReportNameFromUrl(String url) throws IOException{
//        URL reportUrl = new URL(url);
//        String reportName = reportUrl.getPath().substring(reportUrl.getPath().lastIndexOf("/") + 1, reportUrl.getPath().lastIndexOf("."));
//        return reportName;
//    }
//    private JasperReport compileReport(String url) throws IOException, JRException {
//        URL reportUrl = new URL(url);
//        String reportName = getReportNameFromUrl(url);
//        JasperReport jasperReport;
//        try (InputStream employeeReportStream = reportUrl.openStream()) {
//            jasperReport = JasperCompileManager.compileReport(employeeReportStream);
//
//            JRSaver.saveObject(jasperReport, "jaspers" + File.separator + reportName + ".jasper");
//        }
//        return jasperReport;
//    }

    @PostMapping("/generate")
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public void createPost(@RequestBody ReportDto reportDto, HttpServletResponse response) throws IOException, JRException, SQLException {
//        // main report
//        String mainReportUrl = reportDto.getMainReportUrl();
//        String reportName = getReportNameFromUrl(mainReportUrl);
//        JasperReport jasperReport = compileReport(mainReportUrl);
//
//        // compile sub report to .jasper
//        for (String subReportUrl :
//                reportDto.getSubReportUrls()) {
//            compileReport(subReportUrl);
//        }
//
//        JasperPrint jasperPrint;
//
//        try (Connection conn = DriverManager.getConnection(reportDto.getDbUrl())) {
//            Map<String, Object> params = new HashMap<>();
//            params.put("SUBREPORT_DIR", JASPER_DIR);
//            for (ParameterDto param :
//                    reportDto.getParameters()) {
//                params.put(param.getName(), param.getConvertedValue());
//            }
//            for (String key :
//                    params.keySet()) {
//                System.out.println(key + ":" + params.get(key) + ":" + params.get(key).getClass());
//            }
//            jasperPrint = JasperFillManager.fillReport(
//                    jasperReport, params, conn);
//        }

        JasperPrint jasperPrint = reportService.generateReport(reportDto);

        JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
        response.setContentType("application/pdf");
        response.addHeader("Content-Disposition", "inline; filename=" + reportDto.getMainReport().getName() +".pdf;");
        return;
    }

//    @GetMapping()
//    public void getDocument(HttpServletResponse response) throws IOException, JRException {
//
//
//        URL mainJrxml = new URL("http://localhost:8080/medical_certificate.jrxml");
//        JasperReport jasperReport;
//        try (InputStream employeeReportStream = mainJrxml.openStream()) {
//            jasperReport = JasperCompileManager.compileReport(employeeReportStream);
//        }
//
////        String path = resourceLoader.getResource("classpath:medical_certificate.jrxml").getURI().getPath();
////        System.out.println("Path: " + path);
////        JasperReport jasperReport = JasperCompileManager.compileReport(path);
//
//        String url = "jdbc:postgresql://localhost:5432/11968?user=postgres&password=postgres&ssl=false";
//        try {
//            Connection conn = DriverManager.getConnection(url);
////            Statement statement = conn.createStatement();
////            try (ResultSet resultSet = statement.executeQuery("select * from b_employee")) {
////                while (resultSet.next()) {
////                    System.out.println(resultSet.getString(1));
////                }
////            }
//            Map parameters = new HashMap();
//            parameters.put("medical_certificate_no", "MC6400001");
//            JasperPrint jasperPrint = JasperFillManager.fillReport(
//                    jasperReport, parameters, conn);
//            JasperExportManager.exportReportToPdfStream(jasperPrint, response.getOutputStream());
//            response.setContentType("application/pdf");
//            response.addHeader("Content-Disposition", "inline; filename=jasper.pdf;");
//        } catch (SQLException throwables) {
//            throwables.printStackTrace();
//        }
//
//
//    }
}
