package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.user.client.rpc.AsyncCallback;

import java.util.Date;
import java.util.List;

interface GoteFarmRPCAsync {
    public void getAccount(AsyncCallback<JSAccount> cb);
    public void setActiveGuild(String guild_key, AsyncCallback<JSGuild> cb);

    public void getGuildFromArmoryURL(String url, AsyncCallback<JSGuild> cb);

    public void newUser(String username, String email, String password, AsyncCallback<String> cb);
    public void validateSID(String sid, AsyncCallback<String> cb);

    public void getRegions(AsyncCallback<List<JSRegion>> cb);

    public void newCharacter(String guild_key, String character, AsyncCallback<JSCharacter> cb);

    public void getCharacters(String guild_key, AsyncCallback<List<JSCharacter>> cb);
    public void getCharacter(String sid, long cid, AsyncCallback<JSCharacter> cb);

    public void getRoles(String guild_key, AsyncCallback<List<JSRole>> cb);
    public void addRole(String guild_key, String name, boolean restricted,
                        AsyncCallback<JSRole> cb);
    // public void getCharacterRoles(long cid, AsyncCallback<List<JSCharRole>> cb);
    public void updateCharacterRole(String sid, long cid, String role_key,
                                    boolean adding,
                                    AsyncCallback<JSCharacter> cb);

    public void getBadges(String guild_key, AsyncCallback<List<JSBadge>> cb);
    public void addBadge(String guild_key, String name, int score,
                         AsyncCallback<JSBadge> cb);
    public void updateCharacterBadge(String sid, long cid, String badge_key,
                                     boolean adding,
                                     AsyncCallback<JSCharacter> cb);

    public void addInstance(String guild_key, String name, AsyncCallback<JSInstance> cb);
    public void addBoss(String instance_key, String name, AsyncCallback<JSBoss> cb);

    public void getInstances(String guild_key, AsyncCallback<List<JSInstance>> cb);
    public void getInstanceBosses(String instance_key,
                                  AsyncCallback<List<JSBoss>> cb);

    public void getEventTemplate(String sid, String name, AsyncCallback<JSEventTemplate> cb);
    public void getEventTemplates(String guild_key,
                                  AsyncCallback<List<JSEventTemplate>> cb);
    public void saveEventTemplate(String guild_key, JSEventTemplate et,
                                  AsyncCallback<JSEventTemplate> cb);

    public void getEventSchedules(String event_template_key,
                                  AsyncCallback<List<JSEventSchedule>> cb);
    public void saveEventSchedule(String sid, JSEventSchedule es, AsyncCallback<Boolean> cb);

    public void getEvents(String sid, AsyncCallback<List<JSEvent>> cb);
    public void getEventSignups(String sid, String event_key,
                                Date if_changed_since,
                                AsyncCallback<JSEventSignups> cb);
    public void signupForEvent(String sid, String event_key, long cid,
                               String role_key, int signup_type,
                               AsyncCallback<JSEventSignups> cb);
    public void changeEventSignup(String sid, String event_key, long eventsignupid,
                                  String new_role_key, int new_signup_type,
                                  AsyncCallback<JSEventSignups> cb);
    public void removeEventSignup(String sid, String event_key, long eventsignupid,
                                  AsyncCallback<JSEventSignups> cb);

    public void getTimeZones(AsyncCallback<String[]> cb);
    public void setTimeZone(String guild_key, String time_zone,
                            AsyncCallback<JSGuild> cb);
}
