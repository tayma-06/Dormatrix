package controllers.food;

import utils.TimeManager;

public class OperationController {
    private final MealTokenController tokenData = new MealTokenController();
    private final CafeteriaController cafeteriaData = new CafeteriaController();

    public String processTokenVerification(String tokenId) {
        if (tokenId == null || tokenId.trim().isEmpty()) {
            return "[Error] Token ID cannot be empty.";
        }
        return tokenData.verifyAndUseToken(tokenId);
    }


    public String processRamadanModeToggle(boolean isRamadan) {
        TimeManager.setRamadanMode(isRamadan);
        cafeteriaData.setSystemMode(isRamadan);
        return "[System] Ramadan Mode successfully updated to: " + isRamadan;
    }
}