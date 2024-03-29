package pl.edu.agh.sarna.wifiPasswords.parsers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import pl.edu.agh.sarna.model.NetworkEntry;

public class WPAParser {
    private String content = "";

    private static String netEntryPattern = "network=\\{([^\\}]*\n)*\\}";
    private static String SSIDPattern = "ssid=\"[^\"]*\"";
    private static String passwdPattern = "psk=\"[^\"]*\"";

    private Pattern entryPattern = Pattern.compile(netEntryPattern);
    private Pattern ssidPattern = Pattern.compile(SSIDPattern);
    private Pattern passwordPattern = Pattern.compile(passwdPattern);

    public WPAParser(String pathToFile) throws IOException {

        File file = new File(pathToFile);
        FileInputStream in = new FileInputStream(file);

        byte[] data = new byte[8192];
        in.read(data);
        in.close();

        this.content = new String(data, "UTF-8");
    }

    public List<NetworkEntry> parse() {
        List<NetworkEntry> entries = new LinkedList<>();

        Matcher matcher = entryPattern.matcher(content);

        while(matcher.find()) {

            String tmp = matcher.group();
            Matcher ssidMatcher = ssidPattern.matcher(tmp);
            if( ssidMatcher.find() ) {

                String ssid = ssidMatcher.group();
                String passwd = "no password";

                Matcher passwordMatcher = passwordPattern.matcher(tmp);
                if(passwordMatcher.find()) {
                    passwd = passwordMatcher.group();
                }

                entries.add(new NetworkEntry(
                        ssid.replaceAll("\"", "").replaceAll("ssid=", ""),
                        passwd.replaceAll("\"", "").replaceAll("psk=", "")));
            }
        }

        return  entries;
    }
}
