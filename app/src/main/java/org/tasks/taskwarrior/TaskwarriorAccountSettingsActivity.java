package org.tasks.taskwarrior;

import static android.text.TextUtils.isEmpty;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import com.todoroo.astrid.helper.UUIDHelper;
import com.todoroo.astrid.service.TaskDeleter;
import java.net.IDN;
import java.net.URI;
import java.net.URISyntaxException;
import javax.inject.Inject;
import org.tasks.R;
import org.tasks.analytics.Tracker;
import org.tasks.analytics.Tracking.Events;
import org.tasks.data.TaskwarriorAccount;
import org.tasks.data.TaskwarriorDao;
import org.tasks.dialogs.DialogBuilder;
import org.tasks.files.FileExplore;
import org.tasks.injection.ActivityComponent;
import org.tasks.injection.ThemedInjectingAppCompatActivity;
import org.tasks.preferences.Preferences;
import org.tasks.security.Encryption;
import org.tasks.sync.SyncAdapters;
import org.tasks.ui.MenuColorizer;
import timber.log.Timber;

public class TaskwarriorAccountSettingsActivity extends ThemedInjectingAppCompatActivity
    implements Toolbar.OnMenuItemClickListener {

  public static final String EXTRA_TASKWARRIOR_DATA = "taskwarriorData"; // $NON-NLS-1$
  private static final String PASSWORD_MASK = "\u2022\u2022\u2022\u2022\u2022\u2022\u2022\u2022";
  private static final int REQUEST_CERTIFICATE_PICKER = 10001;
  private static final int REQUEST_KEY_PICKER = 10002;
  private static final int REQUEST_CA_PICKER = 10003;

  @Inject DialogBuilder dialogBuilder;
  @Inject Preferences preferences;
  @Inject Tracker tracker;
  @Inject TaskwarriorDao taskwarriorDao;
  @Inject SyncAdapters syncAdapters;
  @Inject TaskDeleter taskDeleter;
  @Inject Encryption encryption;

  @BindView(R.id.root_layout)
  LinearLayout root;

  @BindView(R.id.name)
  TextInputEditText name;

  @BindView(R.id.server)
  TextInputEditText server;

  @BindView(R.id.credentials)
  TextInputEditText credentials;

  @BindView(R.id.certificate)
  TextView certificate;

  @BindView(R.id.key)
  TextView key;

  @BindView(R.id.ca)
  TextView ca;

  @BindView(R.id.trust)
  TextInputEditText trust;

  @BindView(R.id.name_layout)
  TextInputLayout nameLayout;

  @BindView(R.id.server_layout)
  TextInputLayout serverLayout;

  @BindView(R.id.credentials_layout)
  TextInputLayout credentialsLayout;

  @BindView(R.id.certificate_layout)
  TextInputLayout certificateLayout;

  @BindView(R.id.key_layout)
  TextInputLayout keyLayout;

  @BindView(R.id.ca_layout)
  TextInputLayout caLayout;

  @BindView(R.id.trust_layout)
  TextInputLayout trustLayout;

  @BindView(R.id.toolbar)
  Toolbar toolbar;

  private TaskwarriorAccount taskwarriorAccount;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_taskwarrior_account_settings);

    ButterKnife.bind(this);

    taskwarriorAccount = getIntent().getParcelableExtra(EXTRA_TASKWARRIOR_DATA);

    if (savedInstanceState == null) {
      if (taskwarriorAccount != null) {
        name.setText(taskwarriorAccount.getName());
        server.setText(taskwarriorAccount.getServer());
        if (!isEmpty(taskwarriorAccount.getCredentials())) {
          credentials.setText(PASSWORD_MASK);
        }
        certificate.setText(taskwarriorAccount.getCertificate());
        key.setText(taskwarriorAccount.getKey());
        ca.setText(taskwarriorAccount.getCa());
        trust.setText(taskwarriorAccount.getTrust());
      }
      certificate.setInputType(InputType.TYPE_NULL);
      key.setInputType(InputType.TYPE_NULL);
      ca.setInputType(InputType.TYPE_NULL);
    }

    final boolean backButtonSavesTask = preferences.backButtonSavesTask();
    toolbar.setTitle(
        taskwarriorAccount == null ? getString(R.string.add_account) : taskwarriorAccount.getName());
    toolbar.setNavigationIcon(
        ContextCompat.getDrawable(
            this, backButtonSavesTask ? R.drawable.ic_close_24dp : R.drawable.ic_save_24dp));
    toolbar.setNavigationOnClickListener(
        v -> {
          if (backButtonSavesTask) {
            discard();
          } else {
            save();
          }
        });
    toolbar.inflateMenu(R.menu.menu_taskwarrior_account_settings);
    toolbar.setOnMenuItemClickListener(this);
    toolbar.showOverflowMenu();
    MenuColorizer.colorToolbar(this, toolbar);

    if (taskwarriorAccount == null) {
      toolbar.getMenu().findItem(R.id.remove).setVisible(false);
      name.requestFocus();
      InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.showSoftInput(name, InputMethodManager.SHOW_IMPLICIT);
    }
  }

  @OnTextChanged(R.id.name)
  void onNameChanged(CharSequence text) {
    nameLayout.setError(null);
  }

  @OnTextChanged(R.id.server)
  void onServerChanged(CharSequence text) {
    serverLayout.setError(null);
  }

  @OnTextChanged(R.id.credentials)
  void onCredentialsChanged(CharSequence text) {
    credentialsLayout.setError(null);
  }

  @OnFocusChange(R.id.credentials)
  void onCredentialsFocused(boolean hasFocus) {
    if (hasFocus) {
      if (PASSWORD_MASK.equals(credentials.getText().toString())) {
        credentials.setText("");
      }
    } else {
      if (isEmpty(credentials.getText()) && taskwarriorAccount != null) {
        credentials.setText(PASSWORD_MASK);
      }
    }
  }

  @OnClick(R.id.certificate)
  void onCertificateClicked() {
    filePicker(certificate.getText().toString(), REQUEST_CERTIFICATE_PICKER);
  }

  @OnClick(R.id.key)
  void onKeyClicked() {
    filePicker(key.getText().toString(), REQUEST_KEY_PICKER);
  }

  @OnClick(R.id.ca)
  void onCAClicked() {
    filePicker(ca.getText().toString(), REQUEST_CA_PICKER);
  }

  @OnTextChanged(R.id.trust)
  void onTrustChanged(CharSequence text) {
    trustLayout.setError(null);
  }


  void filePicker(String startPath, int request) {
    Intent intent = new Intent(TaskwarriorAccountSettingsActivity.this, FileExplore.class);
    intent.putExtra(FileExplore.EXTRA_START_PATH, startPath);
    startActivityForResult(intent, request);
  }

  @Override
  public void inject(ActivityComponent component) {
    component.inject(this);
  }

  private String getNewName() {
    return name.getText().toString().trim();
  }

  private String getNewServer() {
    return server.getText().toString().trim();
  }

  private boolean credentialsChanged() {
    return taskwarriorAccount == null || !PASSWORD_MASK.equals(credentials.getText().toString().trim());
  }

  private String getNewCredentials() {
    String input = credentials.getText().toString().trim();
    return PASSWORD_MASK.equals(input) ? encryption.decrypt(taskwarriorAccount.getCredentials()) : input;
  }

  private String getNewCertificate() {
    return certificate.getText().toString().trim();
  }

  private String getNewKey() {
    return key.getText().toString().trim();
  }

  private String getNewCa() {
    return ca.getText().toString().trim();
  }

  private String getNewTrust() {
    return trust.getText().toString().trim();
  }

  private void save() {
    String name = getNewName();
    String server = getNewServer();
    String credentials = getNewCredentials();
    String certificate = getNewCertificate();
    String key = getNewKey();
    String ca = getNewCa();
    String trust = getNewTrust();

    boolean failed = false;

    if (isEmpty(name)) {
      nameLayout.setError(getString(R.string.name_cannot_be_empty));
      failed = true;
    } else {
      TaskwarriorAccount accountByName = taskwarriorDao.getAccountByName(name);
      if (accountByName != null && !accountByName.equals(taskwarriorAccount)) {
        nameLayout.setError(getString(R.string.duplicate_name));
        failed = true;
      }
    }

    if (isEmpty(server)) {
      serverLayout.setError(getString(R.string.url_required));
      failed = true;
    } else {
      // TODO(mandre) Update validation to match what TW expects for server
      Uri baseURL = Uri.parse(server);
      String scheme = baseURL.getScheme();
      if ("https".equalsIgnoreCase(scheme) || "http".equalsIgnoreCase(scheme)) {
        String host = baseURL.getHost();
        if (isEmpty(host)) {
          serverLayout.setError(getString(R.string.url_host_name_required));
          failed = true;
        } else {
          try {
            host = IDN.toASCII(host);
          } catch (Exception e) {
            Timber.e(e);
          }
          String path = baseURL.getEncodedPath();
          int port = baseURL.getPort();
          try {
            new URI(scheme, null, host, port, path, null, null);
          } catch (URISyntaxException e) {
            serverLayout.setError(e.getLocalizedMessage());
            failed = true;
          }
        }
      } else {
        serverLayout.setError(getString(R.string.url_invalid_scheme));
        failed = true;
      }
    }

    if (isEmpty(credentials)) {
      credentialsLayout.setError(getString(R.string.credentials_required));
      failed = true;
    }

    if (isEmpty(certificate)) {
      certificateLayout.setError(getString(R.string.certificate_required));
      failed = true;
    }

    if (isEmpty(key)) {
      keyLayout.setError(getString(R.string.key_required));
      failed = true;
    }

    if (isEmpty(ca)) {
      caLayout.setError(getString(R.string.ca_required));
      failed = true;
    }

    // TODO(mandre) Validate trust against valid choices
    if (isEmpty(trust)) {
      trustLayout.setError(getString(R.string.trust_required));
      failed = true;
    }

    if (failed) {
      return;
    }

    if (taskwarriorAccount == null) {
      // TODO(mandre) Try to connect to server
      addAccount(server);
    } else if (needsValidation()) {
      // TODO(mandre) Try to connect to server
      updateAccount(server);
    } else if (hasChanges()) {
      updateAccount(taskwarriorAccount.getServer());
    } else {
      finish();
    }
  }

  private void addAccount(String server) {
    Timber.d("Found server: %s", server);

    TaskwarriorAccount newAccount = new TaskwarriorAccount();
    newAccount.setName(getNewName());
    newAccount.setServer(server);
    newAccount.setCredentials(encryption.encrypt(getNewCredentials()));
    newAccount.setCertificate(getNewCertificate());
    newAccount.setKey(getNewKey());
    newAccount.setCa(getNewCa());
    newAccount.setTrust(getNewTrust());
    newAccount.setUuid(UUIDHelper.newUUID());
    newAccount.setId(taskwarriorDao.insert(newAccount));

    tracker.reportEvent(Events.TASKWARRIOR_ACCOUNT_ADDED);

    setResult(RESULT_OK);
    finish();
  }

  private void updateAccount(String server) {
    taskwarriorAccount.setName(getNewName());
    taskwarriorAccount.setServer(server);
    taskwarriorAccount.setError("");
    if (credentialsChanged()) {
      taskwarriorAccount.setCredentials(encryption.encrypt(getNewCredentials()));
    }
    taskwarriorAccount.setCertificate(getNewCertificate());
    taskwarriorAccount.setKey(getNewKey());
    taskwarriorAccount.setCa(getNewCa());
    taskwarriorAccount.setTrust(getNewTrust());

    taskwarriorDao.update(taskwarriorAccount);

    setResult(RESULT_OK);
    finish();
  }

  private void showSnackbar(int resId) {
    showSnackbar(getString(resId));
  }

  private void showSnackbar(String message) {
    Snackbar snackbar =
        Snackbar.make(root, message, 8000)
            .setActionTextColor(ContextCompat.getColor(this, R.color.snackbar_text_color));
    snackbar
        .getView()
        .setBackgroundColor(ContextCompat.getColor(this, R.color.snackbar_background));
    snackbar.show();
  }

  // TODO(mandre) Update to all fields
  private boolean hasChanges() {
    if (taskwarriorAccount == null) {
      return !isEmpty(getNewName())
          || !isEmpty(getNewServer())
          || !isEmpty(getNewCredentials())
          || !isEmpty(getNewCertificate())
          || !isEmpty(getNewKey())
          || !isEmpty(getNewCa())
          || !isEmpty(getNewTrust());
    }
    return needsValidation() || !getNewName().equals(taskwarriorAccount.getName());
  }

  private boolean needsValidation() {
    return !getNewServer().equals(taskwarriorAccount.getServer())
        || credentialsChanged();
  }

  @Override
  public void finish() {
    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.hideSoftInputFromWindow(name.getWindowToken(), 0);
    super.finish();
  }

  @Override
  public void onBackPressed() {
    if (preferences.backButtonSavesTask()) {
      save();
    } else {
      discard();
    }
  }

  private void removeAccount() {
    dialogBuilder
        .newMessageDialog(R.string.logout_warning, taskwarriorAccount.getName())
        .setPositiveButton(
            R.string.remove,
            (dialog, which) -> {
              taskDeleter.delete(taskwarriorAccount);
              tracker.reportEvent(Events.TASKWARRIOR_ACCOUNT_REMOVED);
              setResult(RESULT_OK);
              finish();
            })
        .setNegativeButton(android.R.string.cancel, null)
        .show();
  }

  private void discard() {
    if (!hasChanges()) {
      finish();
    } else {
      dialogBuilder
          .newMessageDialog(R.string.discard_changes)
          .setPositiveButton(R.string.discard, (dialog, which) -> finish())
          .setNegativeButton(android.R.string.cancel, null)
          .show();
    }
  }

  @Override
  public boolean onMenuItemClick(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.help:
        // TODO(mandre) Create a help page for taskwarrior integration
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://tasks.org/taskwarrior")));
        break;
      case R.id.remove:
        removeAccount();
        break;
    }
    return onOptionsItemSelected(item);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CERTIFICATE_PICKER) {
      if (resultCode == RESULT_OK) {
        certificate.setText(data.getStringExtra(FileExplore.EXTRA_FILE));
      }
    } else if (requestCode == REQUEST_KEY_PICKER) {
      if (resultCode == RESULT_OK) {
        key.setText(data.getStringExtra(FileExplore.EXTRA_FILE));
      }
    } else if (requestCode == REQUEST_CA_PICKER) {
      if (resultCode == RESULT_OK) {
        ca.setText(data.getStringExtra(FileExplore.EXTRA_FILE));
      }
    } else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }
}
