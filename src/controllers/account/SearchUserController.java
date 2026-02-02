package controllers.account;

import controllers.authentication.AccountManager;
import libraries.collections.MyString;

public class SearchUserController {
    private final AccountManager manager;

    public SearchUserController(AccountManager manager) {
        this.manager = manager;
    }

    public String searchById(String id) {
        return manager.findUserDetails(new MyString(id));
    }
}
