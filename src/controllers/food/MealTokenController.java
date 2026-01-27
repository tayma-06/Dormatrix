package controllers.food;

import models.food.MealToken;
import models.food.TokenStatus;
import exceptions.InvalidTokenException;

import java.io.*;
import java.util.*;

public class MealTokenController {
    private final String FILE = "data/mealTokens.txt";

    public MealToken buyToken(String studentId) {
        MealToken t = new MealToken(UUID.randomUUID().toString(), studentId);
        save(t);
        return t;
    }

    public void validate(String tokenId) throws InvalidTokenException {
        List<MealToken> tokens = loadAll();
        for (MealToken t : tokens) {
            if (t.toString().startsWith(tokenId)) {
                if (t.isExpired())
                    throw new InvalidTokenException("Token expired");
                return;
            }
        }
        throw new InvalidTokenException("Invalid token");
    }

    public void expireOldTokens() {
        List<MealToken> tokens = loadAll();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE))) {
            for (MealToken t : tokens) {
                if (t.isExpired()) t.expire();
                pw.println(t);
            }
        } catch (IOException ignored) {}
    }

    private void save(MealToken t) {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {
            pw.println(t);
        } catch (IOException ignored) {}
    }

    private List<MealToken> loadAll() {
        List<MealToken> list = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(FILE))) {
            String line;
            while ((line = br.readLine()) != null)
                list.add(MealToken.fromString(line));
        } catch (IOException ignored) {}
        return list;
    }
}
