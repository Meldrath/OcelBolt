package com.lsd.umc.nodeka.plugin;

import com.lsd.umc.script.ScriptInterface;
import com.lsd.umc.util.AnsiTable;
import com.lsd.umc.util.StringUtility;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.lsd.umc.Client;
import java.util.List;

public class iWitch {

    private ScriptInterface script;
    private Client client;
    private String mapText;
    boolean grabbedMap = false;
    private List<Integer> mobCountInCardinalDirections = new ArrayList<>();

    public interface NodekaMap {

        int indexPosition();
    }

    public enum cardinalDirections implements NodekaMap {

        NORTH {
                    @Override
                    public int indexPosition() {
                        return 2;
                    }

                    @Override
                    public String toString() {
                        return "north";
                    }
                },
        SOUTH {
                    @Override
                    public int indexPosition() {
                        return 3;
                    }

                    @Override
                    public String toString() {
                        return "south";
                    }
                },
        WEST {
                    @Override
                    public int indexPosition() {
                        return 0;
                    }

                    @Override
                    public String toString() {
                        return "west";
                    }
                },
        EAST {
                    @Override
                    public int indexPosition() {
                        return 1;
                    }

                    @Override
                    public String toString() {
                        return "east";
                    }
                }
    }

    /*
     [L:0] R:Draalik X:2000000000 G:205023444 A:-372
     [Lag: 0] [Reply: guide] [Align: 797]
     L:0 R: X:442159262
     [L:0] [Ocellaris H:43028/43028 M:8884/8884 S:5001/5001 E:21049/21049] [A:-1000] []
     */
    //private static final Pattern nonCombatPrompt = Pattern.compile("^(?:\\[?(?:Lag|L):\\s?\\d+\\]?)\\s\\[?(?:R|Reply|.+ H):?\\s?");
    private static final Pattern nonCombatPrompt = Pattern.compile("(?:.+) flies by you\\.|(?:.+) is dead\\!");
    /*
     [L:0] Ocellaris: (perfect condition) vs. roadrunner: (badly wounded)
     [Lag: 2000] [Coil: (perfect condition)] [novice healer: (covered in blood)]

     [L:0] [Darth H:61211/63111 M:16074/16074 S:15390/15888 E:52997/54001] [A:1000] []
     [Ocellaris](perfect condition) [Bayeshi guard](near death)
     */
    private static final Pattern combatPrompt = Pattern.compile("^(?:\\[?(?:Lag|L):\\s?\\\\d+\\]?)?\\s?(?:.+):\\s?\\((?:.+)\\)|\\[.+]\\(.+\\)\\s");

    private boolean spiritLightning = true;
    private boolean chainLightning = true;
    private boolean holdCast = true;

    public void init(ScriptInterface script) {
        this.script = script;

        script.print(AnsiTable.getCode("yellow") + "OcelRange Plugin loaded.\001");
        script.print(AnsiTable.getCode("yellow") + "Developed by Ocellaris");
        script.print(AnsiTable.getCode("yellow") + "Theorycrafting, pizza and soda provided by Onaolas");
        script.registerCommand("OcelRange", "com.lsd.umc.nodeka.plugin.iWitch", "rangeUI");
        script.registerCommand("RangeOn", "com.lsd.umc.nodeka.plugin.iWitch", "rangeOn");
        script.registerCommand("RangeOff", "com.lsd.umc.nodeka.plugin.iWitch", "rangeOff");
    }

    public void MapEvent(ScriptInterface event) {

    }

    public void IncomingEvent(ScriptInterface event) {
        Matcher inCombat = combatPrompt.matcher(event.getText());
        Matcher outOfCombat = nonCombatPrompt.matcher(event.getText());

        if (outOfCombat.find()) {
            mapText = script.getVariable("UMC_MAPTEXT");

            mobCountInCardinalDirections = parseCharacterMap(mapText);

            int largestCount = 0;
            int count = 0;
            int direction = 0;

            for (int i : mobCountInCardinalDirections) {
                //script.capture(String.valueOf(i));
                if (i >= largestCount) {
                    largestCount = i;
                    direction = count;
                }
                count++;
            }

            for (cardinalDirections s : cardinalDirections.values()) {
                if (s.indexPosition() == direction) {
                    if (spiritLightning && largestCount >= 3) {
                        script.parse("invo 'merc' " + s.toString());
                        script.parse("cast 'chain lightning' " + s.toString());
                        holdCast = true;
                    }
                }
            }
        }

        if (event.getText().equals("You may again perform spirit lightning abilities.")) {
            spiritLightning = true;
        }

        if (event.getText().equals("You may again perform ranged attack - basic level abilities.")) {
            chainLightning = true;
        }

        if (event.getText().equals("You cannot perform spirit lightning abilities again yet (type 'prevention').")) {
            spiritLightning = false;
        }

        if (event.getText().equals("You cannot perform ranged attack - basic level abilities again yet (type 'prevention').")) {
            chainLightning = false;
        }

        if (event.getText().equals("You assemble the incantation of, 'lgrluroamy oroemokcebobk.'")) {
            spiritLightning = false;
            holdCast = false;
        }

        if (event.getText().equals("You chant the words, 'drhrgcdhmrhxce.'")) {
            chainLightning = false;
            holdCast = false;
        }

        outOfCombat.reset();

        grabbedMap = false;
    }

    public String rangeOn(String args) {
        holdCast = false;

        return "";
    }

    public String rangeOff(String args) {
        holdCast = true;

        return "";
    }

    private List<Integer> parseCharacterMap(String mapText) {
        int onLine = 0;

        int westCount = 0;
        int eastCount = 0;
        int northCount = 0;
        int southCount = 0;

        List<Integer> mobCount = new ArrayList();
        List<String> line = new ArrayList();
        char[] mapArray = mapText.toCharArray();
        List<Character> goodMap = new ArrayList();

        //script.capture(String.valueOf(mapText.length()));
        if (mapArray.length > 648) {
            for (int i = 0; i < 216; i++) {
                goodMap.add(mapArray[i]);
            }
            for (int i = 216; i < mapArray.length - 216; i++) {
                if (mapArray[i] == '|') {
                } else {
                    goodMap.add(mapArray[i]);
                }
            }
            for (int i = mapArray.length - 216; i < mapArray.length; i++) {
                goodMap.add(mapArray[i]);
            }
        } else {
            for (int i = 0; i < mapArray.length; i++) {
                goodMap.add(mapArray[i]);
            }
        }

        for (int i = 0; i < goodMap.size(); i += 36) {
            line.add(mapText.substring(i, i + 36));
        }

        for (String s : line) {
            //script.capture(String.valueOf(s.length()));
            if (s.contains("#")) {
                for (char c : s.substring(0, 18).toCharArray()) {
                    if (c == '!') {
                        westCount++;
                    }
                }
                for (char c : s.substring(19, 36).toCharArray()) {
                    if (c == '!') {
                        eastCount++;
                    }
                }
            }
            if (s.substring(18, 19).equalsIgnoreCase("!") && onLine < 8) {
                northCount++;
            }
            if (s.substring(18, 19).equalsIgnoreCase("!") && onLine > 8) {
                southCount++;
            }
            onLine++;
        }

        mobCount.add(westCount);
        mobCount.add(eastCount);
        mobCount.add(northCount);
        mobCount.add(southCount);

        return mobCount;
    }
}
