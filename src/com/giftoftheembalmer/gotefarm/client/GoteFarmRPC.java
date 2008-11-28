package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.List;

public interface GoteFarmRPC extends RemoteService {
    public String login(String username, String password) throws InvalidCredentialsError;
    public String newUser(String username, String email, String password) throws AlreadyExistsError;
    public String validateSID(String sid);
    public Integer newCharacter(String sid, String realm, String character) throws UserNotLoggedInError, AlreadyExistsError, NotFoundError;

    public List<JSCharacter> getCharacters(String sid) throws UserNotLoggedInError;
    public JSCharacter getCharacter(String sid, int cid) throws UserNotLoggedInError, NotFoundError;

    public List<JSRole> getRoles();
    public Boolean addRole(String sid, String name, boolean restricted) throws UserNotLoggedInError, AlreadyExistsError;
    // public List<JSCharRole> getCharacterRoles(int cid);

    public List<String> getBadges();
    public Boolean addBadge(String sid, String name, int score) throws UserNotLoggedInError, AlreadyExistsError;

    public Boolean addInstance(String sid, String name) throws UserNotLoggedInError, AlreadyExistsError;
    public Boolean addBoss(String sid, String instance, String boss) throws UserNotLoggedInError, NotFoundError, AlreadyExistsError;

    public List<String> getInstances();
    public List<String> getInstanceBosses(String instance);

    public JSEventTemplate getEventTemplate(String sid, String name) throws UserNotLoggedInError, NotFoundError;
    public List<String> getEventTemplates(String sid) throws UserNotLoggedInError;
    public Boolean saveEventTemplate(String sid, JSEventTemplate et) throws UserNotLoggedInError, NotFoundError, AlreadyExistsError;

    public List<JSEventSchedule> getEventSchedules(String sid, String name) throws UserNotLoggedInError;
}
