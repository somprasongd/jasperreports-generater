package com.github.somprasongd.jasperreports.generater.service;

import com.github.somprasongd.jasperreports.generater.dto.JasperDto;
import com.github.somprasongd.jasperreports.generater.dto.ParameterDto;
import com.github.somprasongd.jasperreports.generater.dto.ReportDto;
import com.github.somprasongd.jasperreports.generater.exception.ReportGenerationException;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.util.JRLoader;
import net.sf.jasperreports.engine.util.JRSaver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    private final String JASPER_DIR = System.getProperty("user.dir") + File.separator + "jaspers";

    @Autowired
    public ReportService() {

    }

    private JasperReport loadJasperReport(String sourceFile) {
        File file = new File(sourceFile);
        if(!file.exists() || !file.isFile()) {
            return null;
        }
        try {
            return (JasperReport ) JRLoader.loadObject(file);
        }catch (JRException ex) {
            return null;
        }
    }

    public JasperPrint generateReport(ReportDto reportDto)  {
        JasperDto mainReport = reportDto.getMainReport();

        String parentPath = JASPER_DIR + File.separator +  mainReport.getName();

        String mainJasperPath = parentPath + File.separator + mainReport.getName() + "." + mainReport.getHash() + ".jasper";

        JasperReport mainJasperReport = null;
        if (new File(mainJasperPath).exists()) {
            mainJasperReport = loadJasperReport(mainJasperPath);
        } else {
            try {
                logger.info("Load main report: " + mainReport.getUrl());
                mainJasperReport = compileReport(mainReport.getUrl(), mainJasperPath);
            } catch (Exception e) {
                logger.error("Failed to generate the report", e);
                throw new ReportGenerationException("Failed to generate the report", e);
            }
        }

        // compile sub report to .jasper
        if (reportDto.getSubReports() != null){
            for (JasperDto subReport :
                    reportDto.getSubReports()) {
                String subJasperPath = parentPath + File.separator + subReport.getName() + "." + subReport.getHash() + ".jasper";
                if (new File(subJasperPath).exists()) {
                    // check is exist
                    JasperReport jr = loadJasperReport(subJasperPath);
                    // skip
                    if (jr != null) {
                        continue;
                    }
                } else {
                    // compile and save
                    try {
                        logger.info("Load sub report: " + subReport.getUrl());
                        compileReport(subReport.getUrl(), subJasperPath);
                    } catch (Exception e) {
                        logger.error("Failed to generate the sub-report", e);
                        throw new ReportGenerationException("Failed to generate the sub-report", e);
                    }
                }
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
            logger.info("Parameters for " + mainReport.getName() + ":");
            for (String key :
                    params.keySet()) {
                logger.info(key + ":" + params.get(key) + ":" + params.get(key).getClass());
            }
            jasperPrint = JasperFillManager.fillReport(
                    mainJasperReport, params, conn);
        } catch (Exception e) {
            logger.error("Generate PDF failed", e);
            throw new ReportGenerationException("Failed to generate pdf", e);
        }
        return jasperPrint;
    }

    private JasperReport compileReport(String url, String jasperPath) throws IOException, JRException {
        URL reportUrl = new URL(url);
        JasperReport jasperReport;
        try (InputStream reportStream = reportUrl.openStream()) {
            jasperReport = JasperCompileManager.compileReport(reportStream);
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
