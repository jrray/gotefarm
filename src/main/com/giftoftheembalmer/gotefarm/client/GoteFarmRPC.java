package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.RemoteService;

import java.util.Date;
import java.util.List;

public interface GoteFarmRPC extends RemoteService {
    public JSAccount getAccount() throws UserNotLoggedInError;
    public JSGuild setActiveGuild(String guild_key)
        throws UserNotLoggedInError, NotFoundError;

    public JSGuild getGuildFromArmoryURL(String url)
        throws UserNotLoggedInError, AlreadyExistsError, NotFoundError,
               IllegalArgumentException;

    public String newUser(String username, String email, String password) throws AlreadyExistsError;
    public String validateSID(String sid);

    public List<JSRegion> getRegions();

    public JSCharacter newCharacter(String guild_key, String character) throws UserNotLoggedInError, AlreadyExistsError, NotFoundError;

    public List<JSCharacter> getCharacters(String guild_key) throws UserNotLoggedInError;
    public JSCharacter getCharacter(String character_key)
        throws UserNotLoggedInError, NotFoundError;

    public void setMainCharacter(String guild_key, String character_key)
        throws UserNotLoggedInError, NotFoundError;

    public List<JSRole> getRoles(String guild_key) throws NotFoundError;
    public JSRole addRole(String guild_key, String name, boolean restricted)
        throws UserNotLoggedInError, NotFoundError;
    // public List<JSCharRole> getCharacterRoles(long cid);
    public JSCharacter updateCharacterRole(String character_key,
                                           String role_key, boolean adding)
        throws UserNotLoggedInError, NotFoundError, IllegalArgumentException;

    public List<JSBadge> getBadges(String guild_key);
    public JSBadge addBadge(String guild_key, String name, int score)
        throws UserNotLoggedInError;
    public JSCharacter updateCharacterBadge(String character_key,
                                            String badge_key, boolean adding)
        throws UserNotLoggedInError, NotFoundError, IllegalArgumentException;

    public JSInstance addInstance(String guild_key, String name)
        throws UserNotLoggedInError, NotFoundError;
    public JSBoss addBoss(String instance_key, String name)
        throws UserNotLoggedInError, NotFoundError;

    public List<JSInstance> getInstances(String guild_key)
        throws NotFoundError;
    public List<JSBoss> getInstanceBosses(String instance_key)
        throws NotFoundError;

    public JSEventTemplate getEventTemplate(String sid, String name) throws UserNotLoggedInError, NotFoundError;
    public List<JSEventTemplate> getEventTemplates(String guild_key)
        throws UserNotLoggedInError, NotFoundError;
    public JSEventTemplate saveEventTemplate(String guild_key,
                                             JSEventTemplate et)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
            IllegalArgumentException;

    public List<JSEventSchedule> getEventSchedules(String event_template_key)
        throws UserNotLoggedInError;
    public void saveEventSchedule(String guild_key, JSEventSchedule es)
        throws UserNotLoggedInError, NotFoundError, IllegalArgumentException;

    public List<JSEvent> getEvents(String guild_key) throws UserNotLoggedInError;
    /* Return null if signups are unchanged */
    public JSEventSignups getEventSignups(String sid, String event_key,
                                          Date if_changed_since)
        throws UserNotLoggedInError, NotFoundError;
    public JSEventSignups signupForEvent(String event_key,
                                         String character_key, String role_key,
                                         int signup_type)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
               IllegalArgumentException;
    public JSEventSignups changeEventSignup(String sid, String event_key,
                                            String event_signup_key,
                                            String new_role_key,
                                            int new_signup_type)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
               IllegalArgumentException;
    public JSEventSignups removeEventSignup(String sid, String event_key,
                                            String event_signup_key)
        throws UserNotLoggedInError, NotFoundError, AlreadyExistsError,
               IllegalArgumentException;

    public String[] getTimeZones();
    public JSGuild setTimeZone(String guild_key, String time_zone)
        throws IllegalArgumentException, NotAuthorizedException,
               NotFoundError, UserNotLoggedInError;
}
