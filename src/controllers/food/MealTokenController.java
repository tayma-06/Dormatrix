package controllers.food;

import models.food.MealToken;
import exceptions.InvalidTokenException;
import java.io.*;
import java.util.*;

public class MealTokenController {
    private final String FILE = "data/foods/mealTokens.txt";

    public MealToken buyToken(String studentId) {
        String prettyId = generateId();
        MealToken t = new MealToken(prettyId, studentId);
        save(t);
        return t;
    }

    public void validate(String tokenId) throws InvalidTokenException {
        List<MealToken> tokens = loadAll();
        for (MealToken t : tokens) {
            if (t.getTokenId().trim().equals(tokenId.trim())) {
                if (t.isExpired()) {
                    throw new InvalidTokenException("Token has expired");
                }
                return;
            }
        }
        throw new InvalidTokenException("Token ID not found in system.");
    }

    private String generateId() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        StringBuilder sb = new StringBuilder("MT-");
        Random rnd = new Random();
        for (int i = 0; i < 4; i++) sb.append(chars.charAt(rnd.nextInt(chars.length())));
        return sb.toString();
    }

    private void save(MealToken t) {
        new File("data/foods").mkdirs();
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE, true))) {
            pw.println(t.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<MealToken> loadAll() {
        List<MealToken> list = new ArrayList<>();
        File file = new File(FILE);
        if (!file.exists()) return list;

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if(line.trim().isEmpty()) continue;
                try {
                    MealToken t = MealToken.fromString(line);
                    if (t != null) list.add(t);
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}