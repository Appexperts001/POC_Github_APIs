package com.example.POC_Github_APIs;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@SpringBootApplication
public class POC_Github_APIsApplication {
	public static void main(String[] args) {
		SpringApplication.run(POC_Github_APIsApplication.class, args);
	}


	public static boolean isTokenExpired(String expiryDate) {
		expiryDate = expiryDate.replaceAll("[\\[\\]]", "").trim();
		DateTimeFormatter formatter = DateTimeFormatter.RFC_1123_DATE_TIME;
		// Parse the expiry date
		Instant expiryInstant = Instant.from(formatter.parse(expiryDate));
		// Get the current time
		Instant now = Instant.now();
		// Calculate the expiration threshold (5 minutes after expiry)
		Instant threshold = expiryInstant.plus(5, ChronoUnit.MINUTES);
		return now.isAfter(threshold);
	}

}
