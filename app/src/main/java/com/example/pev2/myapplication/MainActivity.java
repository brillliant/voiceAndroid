package com.example.pev2.myapplication;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private SpeechRecognizer mRecognizer;

    private static final String KWS_SEARCH = "hotword";
    private static final String COMMAND_SEARCH = "command";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.speak).setOnClickListener(this);
        Log.d("myTag", "---onCreate");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d("myTag", "---onCreateOptionsMenu");
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d("myTag", "---onOptionsItemSelected ");
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void listen() throws IOException {
        Log.d("MainActivity", "speaklog listen start");
        String[] names = (String[]) Arrays.asList("да", "нет").toArray();
        final String hotword = "умный дом";

        PhonMapper phonMapper = new PhonMapper(getAssets().open("dict/ru/hotwords"));
        Grammar grammar = new Grammar(names, phonMapper);
        grammar.addWords(hotword);
        DataFiles dataFiles = new DataFiles(getPackageName(), "ru");
        File hmmDir = new File(dataFiles.getHmm());
        File dict = new File(dataFiles.getDict());
        File jsgf = new File(dataFiles.getJsgf());
        copyAssets(hmmDir);
        saveFile(jsgf, grammar.getJsgf());
        saveFile(dict, grammar.getDict());
        mRecognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(hmmDir)
                .setDictionary(dict)
                .setBoolean("-remove_noise", false)
                .setKeywordThreshold(1e-7f)
                .getRecognizer();
        mRecognizer.addKeyphraseSearch(KWS_SEARCH, hotword);
        mRecognizer.addGrammarSearch(COMMAND_SEARCH, jsgf);
        Log.d("MainActivity", "speaklog listen end");
    }

    private void copyAssets(File baseDir) throws IOException {
        String[] files = getAssets().list("hmm/ru");

        for (String fromFile : files) {
            File toFile = new File(baseDir.getAbsolutePath() + "/" + fromFile);
            InputStream in = getAssets().open("hmm/ru/" + fromFile);
            FileUtils.copyInputStreamToFile(in, toFile);
        }
    }

    private void saveFile(File f, String content) throws IOException {
        File dir = f.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            throw new IOException("Cannot create directory: " + dir);
        }
        FileUtils.writeStringToFile(f, content, "UTF8");
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.speak:
                Log.d("MainActivity", "speaklog onClick");
                try {
                    listen();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
