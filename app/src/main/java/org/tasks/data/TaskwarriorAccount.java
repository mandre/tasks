package org.tasks.data;

import static com.todoroo.astrid.data.Task.NO_UUID;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "taskwarrior_account")
public class TaskwarriorAccount implements Parcelable {

  public static Parcelable.Creator<TaskwarriorAccount> CREATOR =
      new Parcelable.Creator<TaskwarriorAccount>() {

        @Override
        public TaskwarriorAccount createFromParcel(Parcel source) {
          return new TaskwarriorAccount(source);
        }

        @Override
        public TaskwarriorAccount[] newArray(int size) {
          return new TaskwarriorAccount[size];
        }
      };

  @PrimaryKey(autoGenerate = true)
  @ColumnInfo(name = "_id")
  private long id;

  @ColumnInfo(name = "uuid")
  private String uuid = NO_UUID;

  @ColumnInfo(name = "name")
  private String name = "";

  @ColumnInfo(name = "server")
  private String server = "";

  @ColumnInfo(name = "credentials")
  private transient String credentials = "";

  @ColumnInfo(name = "certificate")
  private transient String certificate = "";

  @ColumnInfo(name = "key")
  private transient String key = "";

  @ColumnInfo(name = "ca")
  private transient String ca = "";

  @ColumnInfo(name = "trust")
  private String trust = "";

  @ColumnInfo(name = "error")
  private transient String error = "";

  public TaskwarriorAccount() {}

  @Ignore
  public TaskwarriorAccount(Parcel source) {
    id = source.readLong();
    uuid = source.readString();
    name = source.readString();
    server = source.readString();
    credentials = source.readString();
    certificate = source.readString();
    key = source.readString();
    ca = source.readString();
    trust = source.readString();
    error = source.readString();
  }

  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUuid() {
    return uuid;
  }

  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getServer() {
    return server;
  }

  public void setServer(String server) {
    this.server = server;
  }

  public String getCredentials() {
    return credentials;
  }

  public void setCredentials(String credentials) {
    this.credentials = credentials;
  }

  public String getCertificate() {
    return certificate;
  }

  public void setCertificate(String certificate) {
    this.certificate = certificate;
  }

  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getCa() {
    return ca;
  }

  public void setCa(String ca) {
    this.ca = ca;
  }

  public String getTrust() {
    return trust;
  }

  public void setTrust(String trust) {
    this.trust = trust;
  }

  public String getError() {
    return error;
  }

  public void setError(String error) {
    this.error = error;
  }

  @Override
  public String toString() {
    return "TaskwarriorAccount{"
        + "id="
        + id
        + ", uuid='"
        + uuid
        + '\''
        + ", name='"
        + name
        + '\''
        + ", server='"
        + server
        + '\''
        + ", credentials='"
        + credentials
        + '\''
        + ", certificate='"
        + certificate
        + '\''
        + ", key='"
        + key
        + '\''
        + ", ca='"
        + ca
        + '\''
        + ", trust='"
        + trust
        + '\''
        + ", error='"
        + error
        + '\''
        + '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof TaskwarriorAccount)) {
      return false;
    }

    TaskwarriorAccount that = (TaskwarriorAccount) o;

    if (id != that.id) {
      return false;
    }
    if (uuid != null ? !uuid.equals(that.uuid) : that.uuid != null) {
      return false;
    }
    if (name != null ? !name.equals(that.name) : that.name != null) {
      return false;
    }
    if (server != null ? !server.equals(that.server) : that.server != null) {
      return false;
    }
    if (credentials != null ? !credentials.equals(that.credentials) : that.credentials != null) {
      return false;
    }
    if (certificate != null ? !certificate.equals(that.certificate) : that.certificate != null) {
      return false;
    }
    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }
    if (ca != null ? !ca.equals(that.ca) : that.ca != null) {
      return false;
    }
    if (trust != null ? !trust.equals(that.trust) : that.trust != null) {
      return false;
    }
    return error != null ? error.equals(that.error) : that.error == null;
  }

  @Override
  public int hashCode() {
    int result = (int) (id ^ (id >>> 32));
    result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
    result = 31 * result + (name != null ? name.hashCode() : 0);
    result = 31 * result + (server != null ? server.hashCode() : 0);
    result = 31 * result + (credentials != null ? credentials.hashCode() : 0);
    result = 31 * result + (certificate != null ? certificate.hashCode() : 0);
    result = 31 * result + (key != null ? key.hashCode() : 0);
    result = 31 * result + (ca != null ? ca.hashCode() : 0);
    result = 31 * result + (trust != null ? trust.hashCode() : 0);
    result = 31 * result + (error != null ? error.hashCode() : 0);
    return result;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(id);
    dest.writeString(uuid);
    dest.writeString(name);
    dest.writeString(server);
    dest.writeString(credentials);
    dest.writeString(certificate);
    dest.writeString(key);
    dest.writeString(ca);
    dest.writeString(trust);
    dest.writeString(error);
  }
}
