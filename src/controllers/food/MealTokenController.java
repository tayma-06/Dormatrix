package controllers.food;

import models.food.MealToken;
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
                if (t.isUsed()) {
                    return "Error: This token has already been used!";
                }
                if (!t.getDate().equals(java.time.LocalDate.now())) {
                    return "Error: This token was for a different date (" + t.getDate() + ").";
                }

                t.setUsed(true);
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
        try (BufferedReader br = new BufferedReader(new FileReader(TOKEN_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                list.add(MealToken.fromString(line));
            }
        } catch (IOException e) { /* Handle empty file */ }
        return list;
    }

    private void saveAllTokens(List<MealToken> tokens) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(TOKEN_FILE))) {
            for (MealToken t : tokens) pw.println(t.toString());
        } catch (IOException e) { e.printStackTrace(); }
    }
}