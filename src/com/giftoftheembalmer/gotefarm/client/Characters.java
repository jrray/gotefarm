package com.giftoftheembalmer.gotefarm.client;

import java.util.Date;
import java.util.List;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.ClickListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable.CellFormatter;
import com.google.gwt.user.client.ui.KeyboardListenerAdapter;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.NamedNodeMap;
import com.google.gwt.xml.client.Node;
import com.google.gwt.xml.client.NodeList;
import com.google.gwt.xml.client.XMLParser;

public class Characters extends Composite {

    VerticalPanel vpanel = new VerticalPanel();
    VerticalPanel chrpanel = new VerticalPanel();

    public class Character extends Composite {

        JSCharacter chr = null;
        Document xml = null;

        HorizontalPanel hpanel = new HorizontalPanel();
        VerticalPanel vpanel = new VerticalPanel();

        public Character(JSCharacter chr) {
            this.chr = chr;

            hpanel.setSpacing(2);
            hpanel.add(vpanel);

            vpanel.add(new Label(chr.name));
            vpanel.add(new Label(chr.realm));
            vpanel.add(new Label(chr.race));
            vpanel.add(new Label(chr.clazz));

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

                Node level = attribs.getNamedItem("level");
                vpanel.add(new Label("Level " + level));

                Node updated = attribs.getNamedItem("lastModified");
                vpanel.add(new Label("Armory updated: " + updated));
            }

            initWidget(hpanel);

            setStyleName("Character");
        }
    }

    public class NewCharPanel extends PopupPanel {
        TextBox realm = new TextBox();
        TextBox character = new TextBox();
        Label errmsg = new Label();

        public NewCharPanel() {
            super(false);

            VerticalPanel vpanel = new VerticalPanel();

            Grid grid = new Grid(2, 2);
            grid.setWidth("100%");

            CellFormatter cf = grid.getCellFormatter();
            // right align field lables
            cf.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_RIGHT);
            cf.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_RIGHT);

            grid.setText(0, 0, "Realm:");
            grid.setText(1, 0, "Character Name:");

            realm.setText("Boulderfist");

            grid.setWidget(0, 1, realm);
            grid.setWidget(1, 1, character);

            vpanel.add(grid);

            vpanel.add(errmsg);
            errmsg.addStyleName(errmsg.getStylePrimaryName() + "-error");

            HorizontalPanel hpanel = new HorizontalPanel();
            hpanel.setWidth("100%");

            character.addKeyboardListener(new KeyboardListenerAdapter() {
                public void onKeyPress(Widget sender, char keyCode, int modifiers) {
                    if (keyCode == (char)KEY_ENTER) {
                        addCharacter();
                    }
                }
            });

            hpanel.add(new Button("Submit", new ClickListener() {
                public void onClick(Widget sender) {
                    addCharacter();
                }
            }));

            hpanel.add(new Button("Cancel", new ClickListener() {
                public void onClick(Widget sender) {
                    hide();
                }
            }));

            vpanel.add(hpanel);

            DeferredCommand dc = new DeferredCommand();
            dc.addCommand(new Command() {
                public void execute() {
                    character.setFocus(true);
                }
            });

            setWidget(vpanel);
        }

        public void addCharacter() {
            errmsg.setText("");

            GoteFarm.testService.newCharacter(GoteFarm.sessionID, realm.getText(), character.getText(), new AsyncCallback<Long>() {
                public void onSuccess(Long result) {
                    GoteFarm.testService.getCharacter(GoteFarm.sessionID, result.longValue(), new AsyncCallback<JSCharacter>() {
                        public void onSuccess(JSCharacter chr) {
                            chrpanel.add(new Character(chr));
                        }

                        public void onFailure(Throwable caught) {
                        }
                    });

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

        enrollbtn = new Button("Enroll Character", new ClickListener() {
            public void onClick(Widget sender) {
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

    public void loadCharacters() {
        chrpanel.clear();

        if (GoteFarm.sessionID == null) {
            chrpanel.add(new Label("You are not signed in."));
            enrollbtn.setEnabled(false);
            return;
        }

        enrollbtn.setEnabled(true);

        GoteFarm.testService.getCharacters(GoteFarm.sessionID, new AsyncCallback<List<JSCharacter>>() {
            public void onSuccess(List<JSCharacter> result) {
                for (JSCharacter c : result) {
                    chrpanel.add(new Character(c));
                }
            }

            public void onFailure(Throwable caught) {
            }
        });
    }
}
