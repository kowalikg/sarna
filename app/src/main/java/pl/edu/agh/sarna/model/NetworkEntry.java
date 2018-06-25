package pl.edu.agh.sarna.model;

public class NetworkEntry {

    private String ssid;

    private String password;

    public NetworkEntry(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }

    public String getSsid() {
        return ssid;
    }

    public String getPassword() {
        return password;
    }

}

