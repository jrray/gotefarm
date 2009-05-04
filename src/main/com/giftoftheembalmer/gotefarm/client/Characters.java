package com.giftoftheembalmer.gotefarm.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

import java.util.ArrayList;
import java.util.List;

public class Characters
    extends Composite
    implements HasValue<List<JSCharacter>>, ValueChangeHandler<JSGuild> {

    VerticalPanel vpanel = new VerticalPanel();
    VerticalPanel chrpanel = new VerticalPanel();

    private List<JSCharacter> characters = new ArrayList<JSCharacter>();
    private List<JSRole> roles = new ArrayList<JSRole>();
    private List<JSBadge> badges = new ArrayList<JSBadge>();

    private JSGuild current_guild;

    public void addCharacter(JSCharacter chr) {
        characters.add(chr);
        chrpanel.add(new Character(chr));
        fireCharacterChangedEvent();
    }

    public void replaceCharacter(JSCharacter oldchr, JSCharacter newchr) {
        int idx = characters.indexOf(oldchr);
        if (idx > -1) {
            characters.set(idx, newchr);
            fireCharacterChangedEvent();
        }
    }

    private void fireCharacterChangedEvent() {
        // Send out a copy of the list so our copy is still
        // safe to change.
        List<JSCharacter> c = new ArrayList<JSCharacter>(characters.size());
        c.addAll(characters);
        ValueChangeEvent.fire(this, c);
    }

    public class Character extends Composite {

        JSCharacter chr = null;
        Document xml = null;

        VerticalPanel vpanel = new VerticalPanel();
        HorizontalPanel hpanel = new HorizontalPanel();
        VerticalPanel attr_vpanel = new VerticalPanel();
        HorizontalPanel role_and_badge_panel = new HorizontalPanel();
        RoleAndBadgeEditor role_editor = new RoleAndBadgeEditor("Roles");
        RoleAndBadgeEditor badge_editor = new RoleAndBadgeEditor("Badges");

        public Character(JSCharacter chr) {
            this.chr = chr;

            vpanel.setSpacing(10);

            hpanel.setSpacing(2);
            hpanel.add(attr_vpanel);

            attr_vpanel.add(new Label(chr.name));
            attr_vpanel.add(new Label(chr.race));
            attr_vpanel.add(new Label(chr.clazz));
            attr_vpanel.add(new Label("Level " + chr.level));

            xml = XMLParser.parse(chr.characterxml);
            NodeList items = xml.getElementsByTagName("items");
            if (items != null) {
                for (int i = 0; i < items.getLength(); ++i) {
                    Node node = items.item(i);
                    NodeList subitems = node.getChildNodes();
                    for (int j = 0; j < subitems.getLength(); ++j) {
                        Node subitem = subitems.item(j);
                        if (!subitem.getNodeName().equals("item")) {
                            continue;
                        }

                        NamedNodeMap attribs = subitem.getAttributes();
                        if (attribs != null) {
                            Node id = attribs.getNamedItem("id");
                            Node icon = attribs.getNamedItem("icon");
                            HTML link = new HTML("<a target=\"_blank\" href=\"http://www.wowhead.com/?item=" + id + "\"><img src=\"http://static.wowhead.com/images/icons/medium/" + icon + ".jpg\"/></a>");
                            hpanel.add(link);
                        }
                    }
                }
            }

            NodeList characters = xml.getElementsByTagName("character");
            if (characters != null) {
                Node character = characters.item(0);
                NamedNodeMap attribs = character.getAttributes();

                Node updated = attribs.getNamedItem("lastModified");
                attr_vpanel.add(new Label("Armory updated: " + updated));
            }

            updateRoles();
            updateBadges();

            vpanel.add(hpanel);

            role_and_badge_panel.setWidth("100%");
            role_and_badge_panel.add(role_editor);
            role_and_badge_panel.add(badge_editor);
            vpanel.add(role_and_badge_panel);

            initWidget(vpanel);

            setStyleName("Character");
        }

        class RoleClickHandler extends BadgeAndRoleClickHandler {
            public RoleClickHandler(String flavor1, FlexTable flex, int row, ChrBadgeAndRole role) {
                super(flavor1, flex, row, role);
            }

            public void updateCharacterRole(boolean adding) {
                GoteFarm.goteService.updateCharacterRole(GoteFarm.sessionID,
                                                         chr.cid,
                                                         role.getKey(),
                                                         adding,
                                                         new AsyncCallback<JSCharacter>() {
                    public void onSuccess(JSCharacter result) {
                        replaceCharacter(Character.this.chr, result);
                        Character.this.chr = result;
                    }

                    public void onFailure(Throwable caught) {
                    }
                });
            }
        }

        class RoleClickHandlerFactory extends BadgeAndRoleClickHandlerFactory {
            public BadgeAndRoleClickHandler newClickHandler(FlexTable flex,
                                                              int row,
                                                              ChrBadgeAndRole role) {
                return new RoleClickHandler("role", flex, row, role);
            }
        }

        class BadgeClickHandler extends BadgeAndRoleClickHandler {
            public BadgeClickHandler(String flavor1, FlexTable flex, int row, ChrBadgeAndRole role) {
                super(flavor1, flex, row, role);
            }

            public void updateCharacterRole(boolean adding) {
                GoteFarm.goteService.updateCharacterBadge(GoteFarm.sessionID,
                                                          chr.cid,
                                                          role.getKey(),
                                                          adding,
                                                          new AsyncCallback<JSCharacter>() {
                    public void onSuccess(JSCharacter result) {
                        replaceCharacter(Character.this.chr, result);
                        Character.this.chr = result;
                    }

                    public void onFailure(Throwable caught) {
                    }
                });
            }
        }

        class BadgeClickHandlerFactory extends BadgeAndRoleClickHandlerFactory {
            public BadgeAndRoleClickHandler newClickHandler(FlexTable flex,
                                                              int row,
                                                              ChrBadgeAndRole role) {
                return new BadgeClickHandler("badge", flex, row, role);
            }
        }

        public void updateRoles() {
            role_editor.update(roles, chr.roles, new RoleClickHandlerFactory());
        }

        public void updateBadges() {
            badge_editor.update(badges, chr.badges, new BadgeClickHandlerFactory());
        }
    }

    public class NewCharPanel extends PopupPanel {
        TextBox character = new TextBox();
        Label errmsg = new Label();

        public NewCharPanel() {
            super(false);

            VerticalPanel vpanel = new VerticalPanel();

            Grid grid = new Grid(1, 2);
            grid.setWidth("100%");

            CellFormatter cf = grid.getCellFormatter();
            // right align field labels
            cf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);

            grid.setText(0, 0, "Character Name:");

            grid.setWidget(0, 1, character);

            vpanel.add(grid);

            vpanel.add(errmsg);
            errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");

            HorizontalPanel hpanel = new HorizontalPanel();
            hpanel.setWidth("100%");

            character.addKeyPressHandler(new KeyPressHandler() {
                public void onKeyPress(KeyPressEvent event) {
                    if (event.getCharCode() == KeyCodes.KEY_ENTER) {
                        addCharacter();
                    }
                }
            });

            hpanel.add(new Button("Submit", new ClickHandler() {
                public void onClick(ClickEvent event) {
                    addCharacter();
                }
            }));

            hpanel.add(new Button("Cancel", new ClickHandler() {
                public void onClick(ClickEvent event) {
                    hide();
                }
            }));

            vpanel.add(hpanel);

            DeferredCommand.addCommand(new Command() {
                public void execute() {
                    character.setFocus(true);
                }
            });

            setWidget(vpanel);
        }

        public void addCharacter() {
            errmsg.setText("");

            GoteFarm.goteService.newCharacter(
                                            current_guild.key,
                                            character.getText(),
                                            new AsyncCallback<JSCharacter>() {
                public void onSuccess(JSCharacter chr) {
                    Characters.this.addCharacter(chr);
                    hide();
                }

                public void onFailure(Throwable caught) {
                    try {
                        throw caught;
                    }
                    catch (AlreadyExistsError e) {
                        errmsg.setText("Character already enrolled.");
                    }
                    catch (NotFoundError e) {
                        errmsg.setText("Character not found on Armory.");
                    }
                    catch (Throwable e) {
                        errmsg.setText(e.toString());
                    }
                }
            });
        }
    }

    Button enrollbtn = null;

    public Characters() {
        vpanel.setWidth("100%");

        chrpanel.setSpacing(20);

        vpanel.add(chrpanel);

        enrollbtn = new Button("Enroll Character", new ClickHandler() {
            public void onClick(ClickEvent event) {
                final NewCharPanel popup = new NewCharPanel();
                popup.setPopupPositionAndShow(new NewCharPanel.PositionCallback() {
                public void setPosition(int offsetWidth, int offsetHeight) {
                    int left = (Window.getClientWidth() - offsetWidth) / 3;
                    int top = (Window.getClientHeight() - offsetHeight) / 3;
                    popup.setPopupPosition(left, top);
                }
                });
            }
        });

        vpanel.add(enrollbtn);

        loadCharacters();

        initWidget(vpanel);

        setStyleName("Characters");
    }

    private List<Character> character_widgets = new ArrayList<Character>();

    public void loadCharacters() {
        chrpanel.clear();

        if (current_guild == null) {
            chrpanel.add(new Label("Select a guild before managing characters."));
            enrollbtn.setEnabled(false);
            return;
        }

        enrollbtn.setEnabled(true);

        GoteFarm.goteService.getCharacters(
                                    current_guild.key,
                                    new AsyncCallback<List<JSCharacter>>() {
            public void onSuccess(List<JSCharacter> result) {
                setValue(result, true);

                character_widgets.clear();

                for (JSCharacter c : result) {
                    Character nc = new Character(c);
                    character_widgets.add(nc);
                    chrpanel.add(nc);
                }
            }

            public void onFailure(Throwable caught) {
            }
        });

        GoteFarm.goteService.getRoles(current_guild.key,
                                      new AsyncCallback<List<JSRole>>() {
            public void onSuccess(List<JSRole> result) {
                roles = result;
                for (Character c : character_widgets) {
                    c.updateRoles();
                }
            }

            public void onFailure(Throwable caught) {
            }
        });

        GoteFarm.goteService.getBadges(current_guild.key,
                                       new AsyncCallback<List<JSBadge>>() {
            public void onSuccess(List<JSBadge> result) {
                badges = result;
                for (Character c : character_widgets) {
                    c.updateBadges();
                }
            }

            public void onFailure(Throwable caught) {
            }
        });
    }

    public List<JSCharacter> getValue() {
        return characters;
    }

    public void setValue(List<JSCharacter> value) {
        setValue(value, false);
    }

    public void setValue(List<JSCharacter> value, boolean fireEvents) {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
        if (value.equals(characters)) {
            return;
        }

        characters = value;

        if (fireEvents) {
            fireCharacterChangedEvent();
        }
    }

    public HandlerRegistration addValueChangeHandler(
            ValueChangeHandler<List<JSCharacter>> handler) {
        return addHandler(handler, ValueChangeEvent.getType());
    }

    public void onValueChange(ValueChangeEvent<JSGuild> event) {
        // need to fetch the characters enrolled for this guild
        current_guild = event.getValue();
        loadCharacters();
    }
}
