package io.nem.symbol.automationHelpers.common;

import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.network.NetworkType;

import java.util.HashMap;
import java.util.Map;

public class UserAccounts {
    private final Map<String, Account> userAccounts = new HashMap();

    /**
     * Adds a user to the test user list.
     *
     * @param name Name of the user.
     * @param account Account.
     */
    public void addUser(final String name, final Account account) {
        if (!accountExist(name)) {
            userAccounts.put(name, account);
        }
    }

    /**
     * Gets an account.
     *
     * @param name Name of the account.
     * @param networkType Network type.
     * @return User account.
     */
    public Account getAccount(final String name, final NetworkType networkType) {
        if (!accountExist(name)) {
            addUser(name, Account.generateNewAccount(networkType));
        }
        return userAccounts.get(name);
    }

    /**
     * Adds a user to the test user list.
     *
     * @param users Map of user names and accounts.
     */
    public void addAllUser(final Map<String, Account> users) {
        userAccounts.putAll(users);
    }

    /** Clear test user list. */
    public void clearUsers() {
        userAccounts.clear();
    }

    /**
     * Account exist.
     *
     * @param name Name of the user.
     */
    public boolean accountExist(final String name) {
        return userAccounts.containsKey(name);
    }

}
