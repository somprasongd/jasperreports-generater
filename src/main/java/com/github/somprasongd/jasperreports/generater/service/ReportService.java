package com.github.somprasongd.jasperreports.generater.service;

import com.github.somprasongd.jasperreports.generater.dto.JasperDto;
import com.github.somprasongd.jasperreports.generater.dto.ParameterDto;
import com.github.somprasongd.jasperreports.generater.dto.ReportDto;
import com.github.somprasongd.jasperreports.generater.model.Report;
import com.github.somprasongd.jasperreports.generater.repo.ReportRepository;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class ReportService {
    private final String JASPER_DIR = System.getProperty("user.dir") + File.separator + "jaspers";

    private ReportRepository repository;


    @Autowired
    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public List<Report> retrieveReports() {
        return (List<Report>) repository.findAll();
    }

    public Optional<Report> retrieveReports(String id) {
        return repository.findById(id);
    }

    public List<Report> retrieveReportsByName(String name) {
        return repository.findByName(name);
    }

    public Report createReport(Report report) {
        return repository.save(report);
    }

    public Optional<Report> updateCustomer(String id, Report report) {
        Optional<Report> customerOpt = repository.findById(id);
        if (!customerOpt.isPresent()) {
            return customerOpt;
        }
        report.setId(id);
        return Optional.of(repository.save(report));
    }

    public boolean deleteCustomer(String id) {
        try {
            repository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String getReportNameFromUrl(String url) throws IOException {
        URL reportUrl = new URL(url);
        String reportName = reportUrl.getPath().substring(reportUrl.getPath().lastIndexOf("/") + 1, reportUrl.getPath().lastIndexOf("."));
        return reportName;
    }

    private JasperReport compileReport(String url) throws IOException, JRException {
        URL reportUrl = new URL(url);
        String reportName = getReportNameFromUrl(url);
        JasperReport jasperReport;
        try (InputStream employeeReportStream = reportUrl.openStream()) {
            jasperReport = JasperCompileManager.compileReport(employeeReportStream);

            JRSaver.saveObject(jasperReport, "jaspers" + File.separator + reportName + ".jasper");
        }
        return jasperReport;
    }

    private JasperReport loadJasperReport(String sourceFile) {
        File file = new File(sourceFile);
        if(!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            JasperReport  jasperReport = (JasperReport ) JRLoader.loadObject(file);
            return jasperReport;
        }catch (JRException ex) {
            return null;
        }

    }

    public JasperPrint generateReport(ReportDto reportDto) throws IOException, JRException {

        JasperDto mainReport = reportDto.getMainReport();

        String parentPath = JASPER_DIR + File.separator +  mainReport.getName();

        Optional<Report> mainReportCached = repository.findById(mainReport.getHash());

        JasperReport mainJasperReport = null;
        if (mainReportCached.isPresent()) {
            mainJasperReport = loadJasperReport(mainReportCached.get().getPath());
        }
        if (mainJasperReport == null) {
            // load .jrxml from url and compile to .jasper
            String mainJasperPath = parentPath + File.separator + mainReport.getName() + ".jasper";
            mainJasperReport = compileReport(mainReport.getUrl(), mainJasperPath);
            // save
            Report report = new Report();
            report.setId(mainReport.getHash());
            report.setName(mainReport.getName());
            report.setPath(mainJasperPath);
            repository.save(report);
        }

        // compile sub report to .jasper
        if (reportDto.getSubReports() != null){
            for (JasperDto subReport :
                    reportDto.getSubReports()) {
                Optional<Report> reportCached = repository.findById(subReport.getHash());
                if (reportCached.isPresent()) {
                    // check is exist
                    JasperReport jr = loadJasperReport(reportCached.get().getPath());
                    // skip
                    if (jr != null) {
                        continue;
                    }
                }
                // compile and save
                String jasperPath = parentPath+ File.separator + subReport.getName() + ".jasper";
                compileReport(subReport.getUrl(), jasperPath);
                Report report = new Report();
                report.setId(subReport.getHash());
                report.setName(subReport.getName());
                report.setPath(jasperPath);
                repository.save(report);
            }
        }

        // generate report
        JasperPrint jasperPrint;

        try (Connection conn = DriverManager.getConnection(reportDto.getDbUrl())) {
            Map<String, Object> params = new HashMap<>();
            for (ParameterDto param :
                    reportDto.getParameters()) {
                if (param.getName().equalsIgnoreCase("SUBREPORT_DIR")) {
                    continue;
                }
                params.put(param.getName(), param.getConvertedValue());
            }
            params.put("SUBREPORT_DIR", parentPath + File.separator);
            System.out.println("Parameters for " + mainReport.getName() + ":");
            for (String key :
                    params.keySet()) {
                System.out.println(key + ":" + params.get(key) + ":" + params.get(key).getClass());
            }
            jasperPrint = JasperFillManager.fillReport(
                    mainJasperReport, params, conn);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
        return jasperPrint;
    }

    private JasperReport compileReport(String url, String jasperPath) throws IOException, JRException {
        URL reportUrl = new URL(url);
        JasperReport jasperReport;
        try (InputStream employeeReportStream = reportUrl.openStream()) {
            jasperReport = JasperCompileManager.compileReport(employeeReportStream);
            File file = new File(jasperPath);
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
                parentFile.mkdirs();
            }

            JRSaver.saveObject(jasperReport, jasperPath);
        }
        return jasperReport;
    }
}
