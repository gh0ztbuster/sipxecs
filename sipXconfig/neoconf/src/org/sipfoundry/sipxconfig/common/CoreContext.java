/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 */
package org.sipfoundry.sipxconfig.common;

import java.util.Collection;
import java.util.List;

import org.sipfoundry.sipxconfig.alias.AliasOwner;
import org.sipfoundry.sipxconfig.branch.Branch;
import org.sipfoundry.sipxconfig.common.SpecialUser.SpecialUserType;
import org.sipfoundry.sipxconfig.permission.PermissionName;
import org.sipfoundry.sipxconfig.setting.Group;
import org.sipfoundry.sipxconfig.setup.SetupManager;

/**
 * CoreContext
 */
public interface CoreContext extends DataObjectSource<User>, AliasOwner, ReplicableProvider {

    static final String USER_GROUP_RESOURCE_ID = "user";
    static final String CONTEXT_BEAN_NAME = "coreContext";

    /**
     * Instantiates a user complete w/setting model
     */
    User newUser();

    /**
     * Instantiates an internal user complete w/setting model
     */
    InternalUser newInternalUser();

    /** Instantiates special user that represents sipx service */
    User getSpecialUser(SpecialUserType specialUserType);

    SpecialUser getSpecialUserAsSpecialUser(SpecialUserType specialUserType);

    /**
     * Creates (if necessary) initialize instances of special users
     */
    void initializeSpecialUsers();

    /**
     * @return true if username has changed, false if it's an update to existing user without
     *         username change
     */
    boolean saveUser(User user);

    String getOriginalUserName(User user);

    void deleteUser(User user);

    boolean deleteUsers(Collection<Integer> usersIds);

    void deleteUsersByUserName(Collection<String> usersName);

    User loadUser(Integer id);

    User getUser(Integer id);

    /**
     * Loads all users.
     *
     * Instead of loading all users, use DaoUtils.forAllUsersDo - it works regardless of how many
     * users we have. This method only works if all users can actually fit in available memory.
     */
    @Deprecated
    List<User> loadUsers();

    int getUsersCount();

    int getAllUsersCount();

    int getUsersInGroupCount(Integer groupId);

    /**
     * Return the number of users who are both in the group and found by search. Search matches
     * the searchString against user names, first names, last names, and aliases. Matching is
     * case-insensitive and also matches substrings. For example, the search string "cn" will
     * match the last name "McNamara".
     *
     * @param groupId ID of a group, or null to match all groups
     * @param searchString string to search with, or null to not search
     * @return number of users
     */
    int getUsersInGroupWithSearchCount(Integer groupId, String searchString);

    List<User> loadUsersByPage(String search, Integer groupId, Integer branchId, int page, int pageSize,
            String orderBy, boolean orderAscending);

    List<User> loadUsersByPage(int first, int pageSize);

    List<InternalUser> loadInternalUsers();

    List<User> loadUserByTemplateUser(User template);

    User loadUserByUserName(String userName);

    User loadUserByAlias(String userName);

    User loadUserByConfiguredImId(String userName);

    User loadUserByUserNameOrAlias(String userNameOrAlias);

    List<User> loadUsersByAuthAccountName(String authAccountName);

    List<User> loadUsersByEmail(String email);

    List<User> loadUsersContainsEmail(String email);

    List<User> getSharedUsers();

    /**
     * Check whether the user has a username or alias that collides with an existing username or
     * alias. Check for internal collisions as well, for example, the user has an alias that is
     * the same as the username. (Duplication within the aliases is not possible because the
     * aliases are stored as a Set.) If there is a collision, then return the bad name (username
     * or alias). Otherwise return null. If there are multiple collisions, then it's arbitrary
     * which name is returned.
     *
     * @param user user to test
     * @return name that collides
     */
    DuplicateEntity checkForDuplicateNameOrAlias(User user);

    /**
     * Determines whether or not the application is running in debug mode.
     *
     * @return true if we are running a DEBUG build; false if not.
     */
    boolean getDebug();

    String getAuthorizationRealm();

    String getDomainName();

    void clear();

    List<Group> getGroups();

    /**
     * This method is meant to be intercepted when user group saving permissions are effective
     * @see spring 3 security guidelines
     * @param group
     */
    void storeGroup(Group group);

    /**
     * This method is meant to be intercepted when user group saving permissions are effective
     * @see spring 3 security guidelines
     * @param group
     */
    boolean deleteGroups(Collection<Integer> groupIds);

    /**
     * Retrieves user group by name.
     *
     * @param userGroupName name of the group
     * @param createIfNotFound if true a new group with this name will be created, if false null
     *        is returned if group with a phoneGroupName is not found
     * @return user group or null if group not found and createIfNotFound is false
     */
    Group getGroupByName(String userGroupName, boolean createIfNotFound);

    Group getGroupById(Integer groupId);

    Collection<User> getGroupMembers(Group group);

    Collection<String> getGroupMembersNames(Group group);

    /**
     * Called to create a superadmin user with an empty password, to recover from a situation
     * where there are no admin users in the DB
     */
    void createAdminGroupAndInitialUserTask();

    /**
     * Called by the bootstrap page to create the superadmin user, giving it the specified pin
     *
     * @param pin
     */
    void createAdminGroupAndInitialUser(String pin);

    void addToGroup(Integer groupId, Collection<Integer> ids);

    void removeFromGroup(Integer groupId, Collection<Integer> ids);

    List<User> getGroupSupervisors(Group group);

    List<User> getUsersThatISupervise(User uservisor);

    void checkForValidExtensions(Collection<String> aliases, PermissionName permission);

    List<Group> getAvailableGroups(User user);
    Collection<User> getUsersForBranch(Branch b);

    List<User> loadUserByAdmin();

    /**
     * Loads user ids in chunks. Uses plain SQL to do that.
     * @param first
     * @param pageSize
     * @return
     */
    List<Integer> loadUserIdsByPage(int first, int pageSize);

    Collection<Integer> getGroupMembersByPage(int gid, int first, int pageSize);

    int getGroupMembersCount(int groupId);

    Collection<Integer> getGroupMembersIds(Group group);

    int getBranchMembersCount(int branchId);

    Collection<Integer> getBranchMembersByPage(int bid, int first, int pageSize);

    public boolean isAliasInUseForOthers(String alias, String username);

    public boolean setup(SetupManager manager);

    int getEnabledUsersCount();

    int getDisabledUsersCount();

    int getPhantomUsersCount();

    int getPhantomUsersWithoutSuperadminCount();
    
    boolean isAliasInUseExceptDid(String alias);
}
