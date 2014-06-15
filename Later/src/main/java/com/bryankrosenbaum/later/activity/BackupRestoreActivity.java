package com.bryankrosenbaum.later.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bryankrosenbaum.later.R;
import com.bryankrosenbaum.later.data.LaterListSQLiteHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupRestoreActivity extends ActionBarActivity {

    private Button backupButton;
    private Button restoreButton;
    private TextView lastSavedText;
    private Context context;

    private final String TAG = BackupRestoreActivity.class.getSimpleName();

    private final String BACKUP_DATABASE_FOLDER = "//later";
    private final String BACKUP_DATABASE_PATH = "//later//later_backup.db";
    private final File sd = Environment.getExternalStorageDirectory();

    private boolean backupFileExists;

    public void setBackupFileExists(boolean isBackupFileExist) {
        backupFileExists = isBackupFileExist;
        // enable or disable the button based on whether the file exists or not
        if (restoreButton != null) {
            restoreButton.setEnabled(backupFileExists);
        }
        updateLastSavedText();
    }

    public boolean getBackupFileExists() {
        return backupFileExists;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup_restore);

        context = this;

        backupButton = (Button) findViewById(R.id.backuprestore_backup);
        restoreButton = (Button) findViewById(R.id.backuprestore_restore);
        lastSavedText = (TextView) findViewById(R.id.backuprestore_lastsaved);

        backupButton.setOnClickListener(backupOnClickListener);
        restoreButton.setOnClickListener(restoreClickListener);

        if (sd.canWrite()) {
            Log.d(TAG, "Able to write to sd card, so enable backup button");
            backupButton.setEnabled(true);
        }
        else {
            Log.w(TAG, "Not able to write to sd card, so disable backup button");
            backupButton.setEnabled(false);
        }
        File backupDB = new File(sd, BACKUP_DATABASE_PATH);
        setBackupFileExists(backupDB.exists());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.backup_restore, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    final View.OnClickListener backupOnClickListener = new View.OnClickListener() {
        public void onClick(final View v) {

            if (getBackupFileExists()) {
                Log.d(TAG, "Show alert indicating the file already exists and asking the user to confirm overwriting the file");

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Backup file already exists.\n\nAre you sure you want to overwrite it?");
                builder.setPositiveButton(R.string.overwrite_backup, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "User clicked OK - replace existing backup file with a new file");
                        backupDatabase();
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Log.d(TAG, "User clicked cancel - do not replace existing file");
                        return;
                    }
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                backupDatabase();
            }
        }
    };

    private void backupDatabase() {
        createDirIfNotExists(BACKUP_DATABASE_FOLDER);

        File backupDB = new File(sd, BACKUP_DATABASE_PATH);
        File currentDB = getDatabasePath(LaterListSQLiteHelper.DATABASE_NAME);

        if (copyDatabase(currentDB, backupDB)) {
            Log.i(TAG, "Successfully backed up database");
            Toast.makeText(context, "Successfully backed up database", Toast.LENGTH_SHORT).show();
            setBackupFileExists(true);
        } else {
            Toast.makeText(context, "Error backing up database", Toast.LENGTH_SHORT).show();
        }
    }

    final View.OnClickListener restoreClickListener = new View.OnClickListener() {
        public void onClick(final View v) {
            restoreDatabase();
        }
    };

    private void restoreDatabase() {
        createDirIfNotExists(BACKUP_DATABASE_FOLDER);

        File backupDB = new File(sd, BACKUP_DATABASE_PATH);
        File currentDB = getDatabasePath(LaterListSQLiteHelper.DATABASE_NAME);

        if (copyDatabase(backupDB, currentDB)) {
            Log.i(TAG, "Successfully restored database");
            Toast.makeText(context, "Successfully restored database", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Error restoring database", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean createDirIfNotExists(String path) {
        boolean ret = true;

        File file = new File(Environment.getExternalStorageDirectory(), path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                Log.e(TAG, "Problem creating folder");
                ret = false;
            }
        }
        return ret;
    }

    private boolean copyDatabase(File source, File destination) {
        try {
            if (source.exists()) {
                FileChannel src = new FileInputStream(source).getChannel();
                FileChannel dst = new FileOutputStream(destination).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                Log.d(TAG, "Successfully copied database");
                return true;
            }
            else {
                Log.e(TAG, "source.exists() is false in CopyDatabase");
                return false;
            }
        }
        catch (FileNotFoundException fileNotFoundEx) {
            Log.e(TAG, "FileNotFoundException trying to copy database: " + fileNotFoundEx.getMessage());
            return false;
        }
        catch (IOException ioEx) {
            Log.e(TAG, "IOException trying to copy database: " + ioEx.getMessage());
            return false;
        }
    }

    private void updateLastSavedText() {
        File backupDB = new File(sd, BACKUP_DATABASE_PATH);
        if (backupDB.exists()) {
            Date lastModifiedDate = new Date(backupDB.lastModified());
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            String strLastModifiedDate = formatter.format(lastModifiedDate);
            lastSavedText.setText("Backup Exists from: " + strLastModifiedDate);
        }
        else {
            lastSavedText.setText("");
        }
    }
}
