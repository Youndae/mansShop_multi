package com.example.moduletest.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeUtility;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class MailHogUtils {

    private final WebClient mailHogWebClient = WebClient.create("http://localhost:8025");

    public String getCertificationNumberByMailHog() throws Exception {
        ObjectMapper om = new ObjectMapper();
        String jsonResponse = mailHogWebClient.get()
                .uri("/api/v2/messages")
                .retrieve()
                .bodyToMono(String.class)
                .block();

        try {
            JsonNode root = om.readTree(jsonResponse);
            JsonNode items = root.path("items");

            if(items.isArray() && !items.isEmpty()) {
                JsonNode firstMessage = items.get(0);
                String body = firstMessage
                        .path("Content")
                        .path("Body")
                        .asText();
                String encoding = "";

                if (firstMessage.path("Content")
                        .path("Headers")
                        .path("Content-Transfer-Encoding")
                        .isArray()
                ) {
                    encoding = firstMessage
                            .path("Content")
                            .path("Headers")
                            .path("Content-Transfer-Encoding")
                            .get(0)
                            .asText();
                }

                if ("quoted-printable".equalsIgnoreCase(encoding))
                    body = decodeQuotedPrintable(body);

                Pattern pattern = Pattern.compile("<strong>(\\d+)</strong>");
                Matcher matcher = pattern.matcher(body);

                if (matcher.find())
                    return matcher.group(1);
            }
        }catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }

        throw new RuntimeException();
    }

    private String decodeQuotedPrintable(String input) throws Exception {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(input.getBytes());
             InputStreamReader isr = new InputStreamReader(MimeUtility.decode(bais, "quoted-printable"), "UTF-8");
             BufferedReader br = new BufferedReader(isr)) {

            StringBuilder sb = new StringBuilder();
            String line;
            while((line = br.readLine()) != null) {
                sb.append(line).append("\n");
            }
            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void deleteMailHog() {
        mailHogWebClient.delete()
                .uri("/api/v1/messages")
                .retrieve()
                .bodyToMono(Void.class)
                .block();
    }
}
