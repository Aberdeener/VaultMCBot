package me.aberdeener.vaultmcbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.security.auth.login.LoginException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Timer;
import java.util.TimerTask;

public class VaultMCBot extends ListenerAdapter {

    public static String getConfig(String request) throws FileNotFoundException {
        String resourceName = "/srv/vaultmcbot/config.json";
        InputStream is = new FileInputStream(resourceName);
        if (is == null) {
            throw new NullPointerException("Cannot find resource file " + resourceName);
        }

        JSONTokener tokener = new JSONTokener(is);
        JSONObject object = new JSONObject(tokener);
        if (object.getString(request) == null) {
            return "That string does not exist";
        }
        return object.getString(request);

    }

    public static void main(String[] args) throws InterruptedException, LoginException, FileNotFoundException {

        JDA jda = new JDABuilder(getConfig("token"))
                .addEventListeners(new TokenValidator(), new Commands())
                .setActivity(Activity.playing("on VaultMC.net"))
                .build();
        jda.awaitReady();
        connect();
        long minute = 1200L;
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    if (VaultMCBot.connection.isValid(10)) {
                        return;
                    }
                    else {
                        VaultMCBot.connection.close();
                        System.out.println("database connection invalid, reconnecting");
                        VaultMCBot.connect();
                    }
                } catch (SQLException e) {
                    System.out.println("error checking if sql connection is valid :(");
                    e.printStackTrace();
                }
            }
        }, minute, minute * 2);
    }

    // mysql info
    static Connection connection;
    public Connection getConn() {
        return connection;
    }

    public static void connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(getConfig("host"), getConfig("username"), getConfig("password"));
            System.out.println("Connected to database!");
        } catch (SQLException | ClassNotFoundException | FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}