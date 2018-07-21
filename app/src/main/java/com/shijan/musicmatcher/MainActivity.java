package com.shijan.musicmatcher;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.AudioProcessor;
import be.tarsos.dsp.io.android.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;


public class MainActivity extends AppCompatActivity {
    EditText mEditTextName;
    EditText mEditTextArtist;
    Spinner mSpinnerGenres;
    TextView mSizeTextView;

    int count = 0;

    String TAG = "MusicMatch";

    ArrayList<Integer> mylist = new ArrayList<>(5);
    DatabaseReference mSongDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditTextName = (EditText) findViewById(R.id.editTextName);
        mEditTextArtist = findViewById(R.id.editTextArtist);
        mSpinnerGenres = findViewById(R.id.spinnerGeneres);
        mSizeTextView = findViewById(R.id.sizeView);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.generes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerGenres.setAdapter(adapter);


        mSongDatabase = FirebaseDatabase.getInstance().getReference("songs");


        final AudioDispatcher dispatcher =
                AudioDispatcherFactory.fromDefaultMicrophone(22050, 1024, 0);


        final Button mRecordButton = findViewById(R.id.recordButton);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                Toast.makeText(getApplicationContext(), "Recording...", Toast.LENGTH_SHORT).show();

                PitchDetectionHandler pdh = new PitchDetectionHandler() {
                    @Override
                    public void handlePitch(PitchDetectionResult res, AudioEvent e) {

                        final float pitchInHz = res.getPitch();

                        mylist.add((int) res.getPitch());
                        Log.d(TAG, "handlePitch: " + pitchInHz);

//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//
//                            }
//                        });
                    }
                };

                AudioProcessor pitchProcessor = new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.FFT_YIN, 22050, 1024, pdh);
                dispatcher.addAudioProcessor(pitchProcessor);

                Thread audioThread = new Thread(dispatcher, "Audio Thread");
                audioThread.start();
            }


        });

        final Button mRecord = findViewById(R.id.storeButton);
        mRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatcher.stop();
                calculate();


            }
        });

    }

    private void calculate() {
        Log.d(TAG, "calculate: " + mylist.getClass());

        Set<Integer> set = new LinkedHashSet<Integer>(mylist);
        Integer[] data = new Integer[set.size()];
        set.toArray(data);
        Arrays.sort(data, Collections.reverseOrder());
        addSong(data);


    }

    public void addSong(Integer[] data) {

        Log.d(TAG, "addSongs: " + Arrays.toString(data));


        String name = mEditTextName.getText().toString().trim();
        String artist = mEditTextArtist.getText().toString().trim();

        String genre = mSpinnerGenres.getSelectedItem().toString();

        if (!TextUtils.isEmpty(name) || !TextUtils.isEmpty(artist)) {
            Log.d(TAG, "addSong: inside the if statement");

            String id = mSongDatabase.push().getKey();


            StoreData storeData = new StoreData(name, genre, artist, Arrays.toString(data));
            Log.d(TAG, "addSong: after creating the store data object");

            mSongDatabase.child(id).setValue(storeData);

            Toast.makeText(this, "Song Added", Toast.LENGTH_SHORT).show();


        } else {
            Toast.makeText(this, "Fill the text fields", Toast.LENGTH_SHORT).show();
        }
    }
}

