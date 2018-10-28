package org.tasks.taskwarrior;

import android.net.Uri;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

import de.aaschmid.taskwarrior.config.TaskwarriorAuthentication;
import de.aaschmid.taskwarrior.config.TaskwarriorConfiguration;

class TaskwarriorAccountAdapter implements TaskwarriorConfiguration {

    private final Uri server;
    private final String org;
    private final String user;
    private final UUID uuid;
    private final File certificate;
    private final File key;
    private final File ca;

    TaskwarriorAccountAdapter(String server, String credentials, String certificate, String key, String ca, String trust) {
        this.server = Uri.parse(server);
        String[] split_creds = credentials.split("/");
        this.org = split_creds[0];
        this.user = split_creds[1];
        this.uuid = UUID.fromString(split_creds[2]);
        // TODO(mandre) Check errors for missing files
        this.certificate = new File(certificate);
        this.key = new File(key);
        this.ca = new File(ca);
    }

    @Override
    public File getCaCertFile() {
        return ca;
    }

    @Override
    public File getPrivateKeyCertFile() {
        return certificate;
    }

    @Override
    public File getPrivateKeyFile() {
        return key;
    }

    @Override
    public InetAddress getServerHost() throws UnknownHostException {
        // TODO(mandre) Check errors for connection issue
        return InetAddress.getByName(server.getHost());
    }

    @Override
    public int getServerPort() {
        return server.getPort();
    }

    @Override
    public TaskwarriorAuthentication getAuthentication() {
        return new TaskwarriorAuthentication(org, user, uuid);
    }

}
