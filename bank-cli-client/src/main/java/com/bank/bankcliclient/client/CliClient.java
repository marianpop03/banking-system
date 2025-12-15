package com.bank.bankcliclient.client;


import com.bank.common.dto.LoginRequest;
import com.bank.common.dto.LoginResponse;
import com.bank.common.dto.TransactionRequest;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Scanner;

public class CliClient {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final RestTemplate restTemplate = new RestTemplate();
    private final Scanner scanner = new Scanner(System.in);

    // Stocăm token-ul aici după login
    private String jwtToken = null;
    // Stocăm ID-ul contului curent pentru a ușura operațiunile (opțional, dar util pt UX)
    private Long currentAccountId = null;

    public static void main(String[] args) {
        new CliClient().run();
    }

    public void run() {
        System.out.println("=== SISTEM BANCAR DISTRIBUIT ===");
        while (true) {
            if (jwtToken == null) {
                handleLogin();
            } else {
                showMenu();
            }
        }
    }

    private void handleLogin() {
        System.out.println("\n--- AUTENTIFICARE ---");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        try {
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    BASE_URL + "/auth/login", request, LoginResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                this.jwtToken = response.getBody().getToken();
                System.out.println("Login reușit! Token primit.");

                // NOTĂ: Într-o aplicație reală, am cere serverului lista de conturi.
                // Aici întrebăm utilizatorul pe ce ID de cont vrea să lucreze (pentru simplitate)
                System.out.print("Introduceți ID-ul contului dvs. pentru a opera: ");
                this.currentAccountId = Long.parseLong(scanner.nextLine());
            }
        } catch (HttpClientErrorException e) {
            System.err.println("Login eșuat: " + e.getResponseBodyAsString());
            // Opțiune de a crea cont nou rapid (pentru testare)
            System.out.println("Doriți să creați un cont nou? (da/nu)");
            if (scanner.nextLine().equalsIgnoreCase("da")) {
                handleRegister(username, password);
            }
        } catch (Exception e) {
            System.err.println("Eroare de conexiune: " + e.getMessage());
        }
    }

    private void handleRegister(String username, String password) {
        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);
        try {
            restTemplate.postForEntity(BASE_URL + "/auth/register", request, String.class);
            System.out.println("Înregistrare reușită! Vă rugăm să vă autentificați.");
        } catch (Exception e) {
            System.err.println("Înregistrare eșuată.");
        }
    }

    private void showMenu() {
        System.out.println("\n--- MENIU CONT " + currentAccountId + " ---");
        System.out.println("1. Verifică Sold");
        System.out.println("2. Depunere");
        System.out.println("3. Retragere");
        System.out.println("4. Transfer");
        System.out.println("5. Logout");
        System.out.print("Opțiune: ");

        String choice = scanner.nextLine();
        try {
            switch (choice) {
                case "1" -> checkBalance();
                case "2" -> doTransaction("/deposit", "Depunere");
                case "3" -> doTransaction("/withdraw", "Retragere");
                case "4" -> doTransfer();
                case "5" -> {
                    jwtToken = null;
                    currentAccountId = null;
                    System.out.println("Deconectat.");
                }
                default -> System.out.println("Opțiune invalidă.");
            }
        } catch (HttpClientErrorException.Forbidden | HttpClientErrorException.Unauthorized e) {
            System.err.println("Sesiune expirată sau acces interzis.");
            jwtToken = null;
        } catch (Exception e) {
            System.err.println("Eroare: " + e.getMessage());
        }
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void checkBalance() {
        HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
        ResponseEntity<BigDecimal> response = restTemplate.exchange(
                BASE_URL + "/accounts/" + currentAccountId + "/balance",
                HttpMethod.GET,
                entity,
                BigDecimal.class);
        System.out.println("💰 Sold curent: " + response.getBody() + " RON");
    }

    private void doTransaction(String endpointType, String operationName) {
        System.out.print("Suma: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        TransactionRequest req = new TransactionRequest();
        req.setAmount(amount);

        HttpEntity<TransactionRequest> entity = new HttpEntity<>(req, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/accounts" + endpointType + "/" + currentAccountId,
                entity,
                String.class);

        System.out.println("✅ " + operationName + " reușită: " + response.getBody());
    }

    private void doTransfer() {
        System.out.print("ID Cont Destinatar: ");
        Long targetId = Long.parseLong(scanner.nextLine());
        System.out.print("Suma: ");
        BigDecimal amount = new BigDecimal(scanner.nextLine());

        TransactionRequest req = new TransactionRequest();
        req.setAmount(amount);
        req.setTargetAccountId(targetId);

        HttpEntity<TransactionRequest> entity = new HttpEntity<>(req, getAuthHeaders());

        ResponseEntity<String> response = restTemplate.postForEntity(
                BASE_URL + "/accounts/transfer/" + currentAccountId,
                entity,
                String.class);

        System.out.println("✅ Transfer reușit: " + response.getBody());
    }
}