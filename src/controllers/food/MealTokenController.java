package controllers.food;

import models.food.MealToken;
import models.food.TokenStatus; // Import the enum
import java.io.*;
import java.util.*;

public class MealTokenController {
    private final String TOKEN_FILE = "data/foods/tokens.txt";

    public String verifyAndUseToken(String inputTokenId) {
        List<MealToken> tokens = loadAllTokens();
        boolean found = false;
        String message = "Invalid Token ID.";

        for (MealToken t : tokens) {
            if (t.getTokenId().equals(inputTokenId)) {
                // Check using the Enum status instead of boolean
                if (t.getStatus() == TokenStatus.USED) {
                    return "Error: This token has already been used!";
                }
                if (t.getStatus() == TokenStatus.EXPIRED) {
                    return "Error: This token has expired!";
                }
                if (!t.getDate().equals(java.time.LocalDate.now())) {
                    return "Error: This token was for a different date (" + t.getDate() + ").";
                }

                // Update status to USED
                t.setStatus(TokenStatus.USED);
                found = true;
                message = "Success: Token verified! Enjoy your meal.";
                break;
            }
        }

        if (found) saveAllTokens(tokens);
        return message;
    }

    private List<MealToken> loadAllTokens() {
        List<MealToken> list = new ArrayList<>();
        File file = new File(TOKEN_FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
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
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE))) {
            for (MealToken t : tokens) {
                pw.println(t.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}