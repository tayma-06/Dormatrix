package controllers.profile;

import controllers.authentication.AuthController;
import libraries.collections.MyString;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileController {

    private final AuthController authController = new AuthController();

    public String changePassword(
            MyString currentUserNameOrId,
            MyString role,
            MyString oldPassword,
            MyString newPassword,
            MyString confirmPassword
    ) {
        if (currentUserNameOrId == null || currentUserNameOrId.getValue().trim().isEmpty()) {
            return "Invalid user.";
        }

        if (role == null || role.getValue().trim().isEmpty()) {
            return "Invalid role.";
        }

        if (oldPassword == null || oldPassword.getValue().trim().isEmpty()) {
            return "Current password is required.";
        }

        if (newPassword == null || newPassword.getValue().trim().isEmpty()) {
            return "New password is required.";
        }

        if (confirmPassword == null || confirmPassword.getValue().trim().isEmpty()) {
            return "Confirm password is required.";
        }

        if (!newPassword.getValue().equals(confirmPassword.getValue())) {
            return "New password and confirm password do not match.";
        }

        if (!isValidPassword(newPassword.getValue())) {
            return "Password must be at least 6 characters long and contain at least one number.";
        }

        MyString resolvedUserId = authController.resolveUserId(currentUserNameOrId, role);
        if (resolvedUserId.getValue().trim().isEmpty()) {
            return "Could not map this user to a valid user ID.";
        }

        boolean oldOk = authController.verifyPassword(resolvedUserId, oldPassword, role);
        if (!oldOk) {
            return "Current password is incorrect.";
        }

        boolean changed = authController.changePassword(resolvedUserId, role, oldPassword, newPassword);
        return changed ? "Password changed successfully!" : "Password update failed.";
    }

    public String updatePhoneNumber(MyString currentUserNameOrId, MyString role, MyString newPhone) {
        if (currentUserNameOrId == null || currentUserNameOrId.getValue().trim().isEmpty()) {
            return "Invalid user.";
        }

        if (role == null || role.getValue().trim().isEmpty()) {
            return "Invalid role.";
        }

        if (newPhone == null || newPhone.getValue().trim().isEmpty()) {
            return "Phone number is required.";
        }

        if (!isValidPhone(newPhone.getValue())) {
            return "Invalid phone number format.";
        }

        MyString resolvedUserId = authController.resolveUserId(currentUserNameOrId, role);
        if (resolvedUserId.getValue().trim().isEmpty()) {
            return "Could not map this user to a valid user ID.";
        }

        boolean updated = authController.updatePhoneNumber(resolvedUserId, role, newPhone);
        return updated ? "Phone number updated successfully!" : "Could not update phone number.";
    }

    private boolean isValidPhone(String phone) {
        String phoneRegex = "^\\+8801[1-9][0-9]{8}$|^017[0-9]{8}$";
        Pattern pattern = Pattern.compile(phoneRegex);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    private boolean isValidPassword(String password) {
        return password != null
                && password.length() >= 6
                && password.matches(".*\\d.*");
    }
}
