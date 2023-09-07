package com.fpt.h2s.services.commands.user.utils;

import com.fpt.h2s.services.AmazonS3Service;
import com.lowagie.text.pdf.BaseFont;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.xhtmlrenderer.pdf.ITextRenderer;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;


@UtilityClass
public class PdfUtils {
    public static final String URL = "https://h2s-s3.s3.ap-northeast-1.amazonaws.com/Arial.ttf";
    public static final String FileURL = "https://h2s-s3.s3.ap-northeast-1.amazonaws.com/term.html";

    public static byte[] generatePdfFromHtml(String html, String content) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            String result = html.replace("{0}",content);
            Document document = Jsoup.parse(result, "", Parser.xmlParser());
            ITextRenderer renderer = new ITextRenderer();
            renderer.getFontResolver().addFont(URL, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            renderer.setDocumentFromString(document.html());
            renderer.layout();
            renderer.createPDF(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static String ConvertFileContentToString(AmazonS3Service amazonS3Service) {
            try {
                InputStream inputStream = amazonS3Service.downloadFile(FileURL).getInputStream();
                String htmlContent = new BufferedReader(new InputStreamReader(inputStream))
                        .lines().collect(Collectors.joining("\n"));
                Document document = Jsoup.parse(htmlContent);
                return document.outerHtml();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
    }
}
