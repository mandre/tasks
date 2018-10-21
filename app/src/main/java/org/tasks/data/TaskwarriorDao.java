package org.tasks.data;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import java.util.List;

@Dao
public interface TaskwarriorDao {

  @Query("SELECT * FROM taskwarrior_account WHERE uuid = :uuid LIMIT 1")
  TaskwarriorAccount getAccountByUuid(String uuid);

  @Query("SELECT * FROM taskwarrior_account WHERE name = :name COLLATE NOCASE LIMIT 1")
  TaskwarriorAccount getAccountByName(String name);

  @Query("SELECT * FROM taskwarrior_account ORDER BY UPPER(name) ASC")
  List<TaskwarriorAccount> getAccounts();

  @Insert
  long insert(TaskwarriorAccount taskwarriorAccount);

  @Update
  void update(TaskwarriorAccount taskwarriorAccount);
}
