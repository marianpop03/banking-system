package com.bank.bankcliclient.client;

import com.bank.common.dto.LoginRequest;
import com.bank.common.dto.LoginResponse;
import com.bank.common.dto.TransactionRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class BankGuiClient extends JFrame {

    private static final String BASE_URL = "http://localhost:8080/api";
    private final RestTemplate restTemplate = new RestTemplate();

    // Datele sesiunii
    private String jwtToken = null;
    private Long currentAccountId = null;

    // Componente grafice
    private JPanel mainPanel;

    public BankGuiClient() {
        setTitle("Sistem Bancar Distribuit - Client");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 500);
        setLocationRelativeTo(null); // Centrează fereastra

        mainPanel = new JPanel();
        mainPanel.setLayout(new CardLayout());
        add(mainPanel);

        // Inițializăm ecranul de start (Login)
        initLoginScreen();

        setVisible(true);
    }

    public static void main(String[] args) {
        // Rulăm interfața pe thread-ul dedicat Swing
        SwingUtilities.invokeLater(BankGuiClient::new);
    }

    // --- ECRAN 1: LOGIN & REGISTER ---
    private void initLoginScreen() {
        JPanel loginPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JLabel titleLabel = new JLabel("Autentificare ", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));

        JTextField userField = new JTextField();
        userField.setBorder(BorderFactory.createTitledBorder("Username"));

        JPasswordField passField = new JPasswordField();
        passField.setBorder(BorderFactory.createTitledBorder("Parola"));

        JButton loginButton = new JButton("Login");
        loginButton.setBackground(new Color(60, 179, 113)); // Verde
        loginButton.setForeground(Color.WHITE);

        JButton registerButton = new JButton("Creează cont");

        loginPanel.add(titleLabel);
        loginPanel.add(userField);
        loginPanel.add(passField);
        loginPanel.add(loginButton);
        loginPanel.add(new JLabel("- sau -", SwingConstants.CENTER));
        loginPanel.add(registerButton);

        // Acțiuni Butoane
        loginButton.addActionListener(e -> performLogin(userField.getText(), new String(passField.getPassword())));

        // MODIFICARE: Deschidem un dialog dedicat pentru înregistrare
        registerButton.addActionListener(e -> showRegisterDialog());

        // Schimbăm conținutul ferestrei
        setContentPane(loginPanel);
        revalidate();
    }

    // --- DIALOG NOU PENTRU ÎNREGISTRARE (CU ID MANUAL) ---
    private void showRegisterDialog() {
        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));

        JTextField idField = new JTextField();
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();

        panel.add(new JLabel("ID Cont (Cod Unic):"));
        panel.add(idField);
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Parola:"));
        panel.add(passField);

        int result = JOptionPane.showConfirmDialog(this, panel, "Înregistrare Cont Nou",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (result == JOptionPane.OK_OPTION) {
            String id = idField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());

            performRegister(id, user, pass);
        }
    }

    // --- ECRAN 2: DASHBOARD ---
    private void initDashboardScreen() {
        JPanel dashboardPanel = new JPanel(new GridLayout(6, 1, 10, 10));
        dashboardPanel.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        JLabel welcomeLabel = new JLabel("Salut! Cont ID: " + currentAccountId, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        dashboardPanel.add(welcomeLabel);

        JButton btnBalance = new JButton("💰 Verifică Sold");
        JButton btnDeposit = new JButton("📥 Depunere");
        JButton btnWithdraw = new JButton("📤 Retragere");
        JButton btnTransfer = new JButton("💸 Transfer");
        JButton btnLogout = new JButton("🚪 Logout");
        btnLogout.setBackground(Color.RED);
        btnLogout.setForeground(Color.WHITE);

        // Acțiuni
        btnBalance.addActionListener(e -> doCheckBalance());
        btnDeposit.addActionListener(e -> doTransaction("/deposit", "Depunere"));
        btnWithdraw.addActionListener(e -> doTransaction("/withdraw", "Retragere"));
        btnTransfer.addActionListener(e -> doTransfer());
        btnLogout.addActionListener(e -> logout());

        dashboardPanel.add(btnBalance);
        dashboardPanel.add(btnDeposit);
        dashboardPanel.add(btnWithdraw);
        dashboardPanel.add(btnTransfer);
        dashboardPanel.add(btnLogout);

        setContentPane(dashboardPanel);
        revalidate();
    }

    // --- LOGICA DE BUSINESS (REST API CALLS) ---

    private void performLogin(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Introduceți user și parolă!", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        LoginRequest request = new LoginRequest();
        request.setUsername(username);
        request.setPassword(password);

        try {
            ResponseEntity<LoginResponse> response = restTemplate.postForEntity(
                    BASE_URL + "/auth/login", request, LoginResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                this.jwtToken = response.getBody().getToken();

                if (response.getBody().getAccountId() != null) {
                    this.currentAccountId = response.getBody().getAccountId();
                } else {
                    String idStr = JOptionPane.showInputDialog(this, "Login reușit! Introduceți ID-ul contului:");
                    this.currentAccountId = Long.parseLong(idStr);
                }

                initDashboardScreen();
            }
        } catch (HttpClientErrorException e) {
            JOptionPane.showMessageDialog(this, "Login eșuat: " + e.getResponseBodyAsString(), "Eroare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare server: " + e.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    // MODIFICAT: Primește acum și ID-ul
    private void performRegister(String idStr, String username, String password) {
        if (idStr.isEmpty() || username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Toate câmpurile sunt obligatorii!", "Eroare", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Folosim un Map pentru a trimite JSON-ul corect, inclusiv ID-ul
            // Structura va fi: {"id": 123, "username": "...", "password": "..."}
            Map<String, Object> registerPayload = new HashMap<>();

            // Convertim ID-ul la Long sau îl lăsăm String, în funcție de baza de date
            // Aici îl convertim la Long pentru că ai folosit INT în SQL
            try {
                registerPayload.put("id", Long.parseLong(idStr));
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID-ul trebuie să fie un număr!", "Eroare", JOptionPane.ERROR_MESSAGE);
                return;
            }

            registerPayload.put("username", username);
            registerPayload.put("password", password);

            // Trimitem cererea
            restTemplate.postForEntity(BASE_URL + "/auth/register", registerPayload, String.class);

            JOptionPane.showMessageDialog(this, "Cont creat cu succes (ID: " + idStr + ")! Acum te poți loga.", "Succes", JOptionPane.INFORMATION_MESSAGE);
        } catch (HttpClientErrorException e) {
            JOptionPane.showMessageDialog(this, "Eroare server: " + e.getResponseBodyAsString(), "Eroare Înregistrare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Eroare: " + e.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void doCheckBalance() {
        try {
            HttpEntity<Void> entity = new HttpEntity<>(getAuthHeaders());
            ResponseEntity<BigDecimal> response = restTemplate.exchange(
                    BASE_URL + "/accounts/" + currentAccountId + "/balance",
                    HttpMethod.GET,
                    entity,
                    BigDecimal.class);

            JOptionPane.showMessageDialog(this, "Sold curent: " + response.getBody() + " RON", "Informație Sold", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void doTransaction(String endpointType, String operationName) {
        String amountStr = JOptionPane.showInputDialog(this, "Introduceți suma pentru " + operationName + ":");
        if (amountStr == null || amountStr.isEmpty()) return;

        try {
            BigDecimal amount = new BigDecimal(amountStr);
            TransactionRequest req = new TransactionRequest();
            req.setAmount(amount);

            HttpEntity<TransactionRequest> entity = new HttpEntity<>(req, getAuthHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/accounts" + endpointType + "/" + currentAccountId,
                    entity,
                    String.class);

            JOptionPane.showMessageDialog(this, operationName + " reușită!", "Succes", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Suma invalidă!", "Eroare", JOptionPane.ERROR_MESSAGE);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void doTransfer() {
        String targetIdStr = JOptionPane.showInputDialog(this, "ID Cont Destinatar:");
        if (targetIdStr == null || targetIdStr.isEmpty()) return;

        String amountStr = JOptionPane.showInputDialog(this, "Suma de transferat:");
        if (amountStr == null || amountStr.isEmpty()) return;

        try {
            TransactionRequest req = new TransactionRequest();
            req.setAmount(new BigDecimal(amountStr));
            req.setTargetAccountId(Long.parseLong(targetIdStr));

            HttpEntity<TransactionRequest> entity = new HttpEntity<>(req, getAuthHeaders());
            ResponseEntity<String> response = restTemplate.postForEntity(
                    BASE_URL + "/accounts/transfer/" + currentAccountId,
                    entity,
                    String.class);

            JOptionPane.showMessageDialog(this, "Transfer reușit!", "Succes", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            handleError(e);
        }
    }

    private void logout() {
        this.jwtToken = null;
        this.currentAccountId = null;
        initLoginScreen();
    }

    private HttpHeaders getAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(jwtToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    private void handleError(Exception e) {
        String msg = e.getMessage();
        if (e instanceof HttpClientErrorException) {
            msg = ((HttpClientErrorException) e).getResponseBodyAsString();
        }
        JOptionPane.showMessageDialog(this, "Operațiune eșuată: " + msg, "Eroare", JOptionPane.ERROR_MESSAGE);
    }
}