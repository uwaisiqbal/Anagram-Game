package uk.ac.lims.anagramgame;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.example.uwais_000.anagramgame.R;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.plus.Plus;

import java.util.ArrayList;


public class StartActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener {

    Spinner playersSpinner, timeSpinner, roundsSpinner;
    int[] playerArray, timeArray, roundsArray;
    int playerSelected, timeSelected, roundsSelected;
    Button playButton;
    int numberOfPlayers;
    private GoogleApiClient mGoogleApiClient;
    private static int RC_SIGN_IN = 9001;
    final static int RC_SELECT_PLAYERS = 10000;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        numberOfPlayers = getIntent().getIntExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, 2);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API).addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();
        mGoogleApiClient.connect();

        //Integer arrays of the equivalent data used in string arrays to populate spinners
        playerArray = new int[] {2,3,4,5};
        timeArray = new int[] {30,45,60,90,120};
        roundsArray = new int[]{3,5,7,10};

        //Default values of the spinner selections - Do I really need these???
        playerSelected = playerArray[0];
        timeSelected = timeArray[0];
        roundsSelected = roundsArray[0];

        SetupSpinners();

        //If single player or online multiplayer remove Players Spinner
        if(numberOfPlayers < 2){
            LinearLayout llPlayers = (LinearLayout) findViewById(R.id.llPlayers);
            llPlayers.setVisibility(View.GONE);
        }

        //If single Player mode
        if(numberOfPlayers == 1){
            playerSelected = 1;
        }

        playButton = (Button) findViewById(R.id.btnPlay);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //On Play Button Clicked
                //If single player or local multiplayer mode
                if(numberOfPlayers > 0) {
                    startGame();
                }else{
                    //Start google play UI to choose players
                    Intent intent =
                            Games.TurnBasedMultiplayer.getSelectOpponentsIntent(mGoogleApiClient, 1, 4, false);
                    startActivityForResult(intent, RC_SELECT_PLAYERS);

                }
            }
        });
    }

    private void startGame() {
        Intent intent = new Intent(getApplicationContext(), LocalAnagramGameActivity.class);
        intent.putExtra(GameMetaData.NUMBER_OF_PLAYERS_KEY, playerSelected);
        intent.putExtra(GameMetaData.TURN_TIME_KEY, timeSelected);
        intent.putExtra(GameMetaData.NUMBER_OF_ROUNDS_KEY, roundsSelected);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SELECT_PLAYERS) {
            if (resultCode != Activity.RESULT_OK) {
                // user canceled
                return;
            }

            // Get the invitee list.
            final ArrayList<String> invitees =
                    data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);
            numberOfPlayers = invitees.size();
            Log.v("Start Activity", "Number of Players: " + numberOfPlayers);
            startGame();

            // Get auto-match criteria.
            /**Bundle autoMatchCriteria = null;
            int minAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
            int maxAutoMatchPlayers = data.getIntExtra(
                    Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
            if (minAutoMatchPlayers > 0) {
                autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                        minAutoMatchPlayers, maxAutoMatchPlayers, 0);
            } else {
                autoMatchCriteria = null;
            }*/

            TurnBasedMatchConfig tbmc = TurnBasedMatchConfig.builder()
                    .addInvitedPlayers(invitees)
                    .build();

            // Create and start the match.
            //Games.TurnBasedMultiplayer
            //        .createMatch(mGoogleApiClient, tbmc)
             //       .setResultCallback(new MatchInitiatedCallback());
        }
    }

    private void SetupSpinners() {
        playersSpinner = (Spinner) findViewById(R.id.playersSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.players_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        playersSpinner.setAdapter(adapter);
        playersSpinner.setOnItemSelectedListener(this);


        timeSpinner = (Spinner) findViewById(R.id.timeSpinner);
        ArrayAdapter<CharSequence> timeAdapter = ArrayAdapter.createFromResource(this, R.array.turnTime_array, android.R.layout.simple_spinner_item);
        timeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        timeSpinner.setAdapter(timeAdapter);
        timeSpinner.setOnItemSelectedListener(this);

        roundsSpinner = (Spinner) findViewById(R.id.roundsSpinner);
        ArrayAdapter<CharSequence> roundsAdapter = ArrayAdapter.createFromResource(this, R.array.rounds_array, android.R.layout.simple_spinner_item);
        roundsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        roundsSpinner.setAdapter(roundsAdapter);
        roundsSpinner.setOnItemSelectedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Spinner spinner = (Spinner) parent;
        switch (spinner.getId()){
            case R.id.playersSpinner:
                playerSelected = playerArray[position];
                Log.v("TAG", String.valueOf(playerSelected));
                break;

            case R.id.timeSpinner:
                timeSelected = timeArray[position];
                Log.v("TAG", String.valueOf(timeSelected));
                break;

            case R.id.roundsSpinner:
                roundsSelected = roundsArray[position];
                Log.v("TAG", String.valueOf(roundsSelected));
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}