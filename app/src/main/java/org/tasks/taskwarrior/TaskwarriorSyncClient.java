package org.tasks.taskwarrior;

import org.tasks.R;
import org.tasks.data.TaskwarriorAccount;
import org.tasks.security.Encryption;
import org.tasks.ui.DisplayableException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.aaschmid.taskwarrior.TaskwarriorClient;
import de.aaschmid.taskwarrior.internal.ManifestHelper;
import de.aaschmid.taskwarrior.message.TaskwarriorMessage;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

class TaskwarriorSyncClient {

  private final TaskwarriorClient client;

    TaskwarriorSyncClient(TaskwarriorAccount taskwarriorAccount, Encryption encryption) {
    this(taskwarriorAccount.getServer(),
            encryption.decrypt(taskwarriorAccount.getCredentials()),
            taskwarriorAccount.getCertificate(),
            taskwarriorAccount.getKey(),
            taskwarriorAccount.getCa(),
            taskwarriorAccount.getTrust());
  }

    TaskwarriorSyncClient(String server, String credentials, String certificate, String key, String ca, String trust) {
    TaskwarriorAccountAdapter taskwarriorAccountAdapter = new TaskwarriorAccountAdapter(server, credentials, certificate, key, ca, trust);
    this.client = new TaskwarriorClient(taskwarriorAccountAdapter);
  }

  Single<String> testConnection() {
    // TODO(mandre) Add try/catch for exceptions
    return Single.fromCallable(
            () -> {
              return sendMessage();
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());
  }

    private String sendMessage() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("type", "statistics");
        headers.put("protocol", "v1");
        headers.put("client", "taskwarrior-java-client " + ManifestHelper.getImplementationVersionFromManifest("local-dev"));

        TaskwarriorMessage response = client.sendAndReceive(new TaskwarriorMessage(headers));
        if (Integer.parseInt(response.getHeaders().get("code")) != 200) {
            throw new DisplayableException(R.string.taskwarrior_failed_to_connect);
        }
        // TODO(mandre) implement a way to get useful infos from client
        return client.toString();
    }

}