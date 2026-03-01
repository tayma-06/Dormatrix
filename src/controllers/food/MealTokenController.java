package controllers.food;

import models.food.MealToken;
import models.food.TokenStatus;
import models.food.MealType;
import models.store.StudentBalance;
import utils.TimeManager;

import java.io.*;
import java.time.LocalDate;
import java.util.*;

public class MealTokenController {

    private final String TOKEN_FILE = "data/foods/tokens.txt";
    private static final Object VERIFICATION_LOCK = new Object();

    public MealTokenController() {
        ensureFile();
    }

    private void ensureFile() {
        try {
            File f = new File(TOKEN_FILE);
            File parent = f.getParentFile();
            if (parent != null) {
                parent.mkdirs();
            }
            if (!f.exists()) {
                f.createNewFile();
            }
        } catch (IOException e) {
            System.err.println("Token file init error: " + e.getMessage());
        }
    }

    public String verifyAndUseToken(String inputTokenId) {
        // Synchronize to prevent double-usage of the same token
        synchronized (VERIFICATION_LOCK) {
            String tokenId = (inputTokenId == null) ? "" : inputTokenId.trim();
            if (tokenId.isEmpty()) {
                return "Error: Token ID cannot be empty.";
            }

            List<MealToken> tokens = loadAllTokens();
            boolean found = false;

            for (MealToken t : tokens) {
                if (t.getTokenId().equalsIgnoreCase(tokenId)) {

                    if (t.getStatus() == TokenStatus.USED) {
                        return "Error: This token has already been used!";
                    }
                    if (t.getStatus() == TokenStatus.EXPIRED) {
                        return "Error: This token has expired!";
                    }

                    if (!t.getDate().equals(TimeManager.nowDate())) {
                        return "Error: This token was for a different date (" + t.getDate() + ").";
                    }

                    t.setStatus(TokenStatus.USED);
                    found = true;
                    break;
                }
            }

            if (found) {
                saveAllTokens(tokens);
                return "Success: Token verified! Enjoy your meal.";
            }
            return "Invalid Token ID.";
        }
    }

    private List<MealToken> loadAllTokens() {
        ensureFile();
        List<MealToken> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(TOKEN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    list.add(MealToken.fromString(line));
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading tokens: " + e.getMessage());
        }
        return list;
    }

    private void saveAllTokens(List<MealToken> tokens) {
        ensureFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE))) {
            for (MealToken t : tokens) {
                pw.println(t.toString());
            }
        } catch (IOException e) {
            System.err.println("Error writing tokens: " + e.getMessage());
        }
    }

    public String purchaseTokenForDay(String username, LocalDate day, MealType mealType) {
        if (hasActiveToken(username, mealType, day)) {
            return "You already have an ACTIVE token for " + mealType + " on " + day;
        }

        double price = getPriceForMeal(mealType);
        StudentBalance student = loadStudentBalance(username);

        if (student == null) {
            return "Transaction Failed: User '" + username + "' not found.";
        }
        if (!student.deductBalance(price)) {
            return "Transaction Failed: Insufficient funds.";
        }

        String uniqueID = "MT-" + day.getYear() + "-"
                + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        saveTokenToDatabase(new MealToken(
                uniqueID, username, mealType, day, TokenStatus.ACTIVE
        ));

        return "Success! Token ID: " + uniqueID + " | Cost: " + price + " BDT";
    }

    private boolean hasActiveToken(String username, MealType type, LocalDate date) {
        for (MealToken t : getStudentTokens(username)) {
            if (t.getType() == type && t.getDate().equals(date) && t.getStatus() == TokenStatus.ACTIVE) {
                return true;
            }
        }
        return false;
    }

    private StudentBalance loadStudentBalance(String username) {
        return null;
    }

    private void saveTokenToDatabase(MealToken token) {
        ensureFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE, true))) {
            pw.println(token.toString());
        } catch (IOException e) {
            System.err.println("Error saving token: " + e.getMessage());
        }
    }

    public List<MealToken> getStudentTokens(String username) {
        ensureFile();

        List<MealToken> studentTokens = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(TOKEN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) {
                    continue;
                }

                MealToken token = MealToken.fromString(line);
                if (token.getStudentId().equals(username)) {
                    studentTokens.add(token);
                }
            }
        } catch (IOException e) {
            System.err.println("Token read error: " + e.getMessage());
        }
        return studentTokens;
    }

    private double getPriceForMeal(MealType type) {
        return switch (type) {
            case BREAKFAST ->
                30.0;
            case LUNCH, DINNER ->
                60.0;
            case SUHOOR ->
                40.0;
            case IFTAR ->
                50.0;
            default ->
                0.0;
        };
    }
}
