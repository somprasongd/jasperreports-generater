package com.github.somprasongd.jasperreports.generater.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;

@Getter
@Setter
@NoArgsConstructor
public class JasperDto {

    @NotBlank
    private String name;
    @NotBlank
    private String url;

    public String getHash() {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(url.getBytes());
            byte[] digest = md.digest();
            String myHash = DatatypeConverter
                    .printHexBinary(digest).toUpperCase();
            return myHash;
        } catch (Exception ex) {
            return null;
        }
    }


}
